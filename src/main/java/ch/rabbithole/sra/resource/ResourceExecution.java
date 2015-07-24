package ch.rabbithole.sra.resource;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.internal.Streams;

import com.sun.ws.rs.ext.ResponseImpl;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

public final class ResourceExecution {

  private final Resource resource;
  private final ObjectFactory objectFactory;
  private final ParameterMap parameterMap;

  public ResourceExecution(final Resource resource, final ObjectFactory objectFactory, final ParameterMap parameterMap) {
    this.resource = resource;
    this.objectFactory = objectFactory;
    this.parameterMap = parameterMap;
  }

  public void execute(final HttpServletRequest req, final HttpServletResponse resp) {
    Class<?> declaringClass = resource.getMethod().getDeclaringClass();
    Object instance = objectFactory.createInstance(declaringClass);

    Object[] params = buildMethodParams(req.getParameterMap(), req);

    Response response = executeResourceMethod(instance, params);
    writeAnswer(resp, response);
  }

  private Response executeResourceMethod(final Object instance, final Object[] params) {
    try {
      Object resultObject = resource.getMethod().invoke(instance, params);
      if (resultObject instanceof Response) {
        return (Response) resultObject;
      }
      return buildResponseObject(resultObject);
    } catch (InvocationTargetException e) {
      return handleError(e.getCause());
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Method must be public: " + resource.getMethod());
    }
  }

  private Response buildResponseObject(final Object resultObject) {
    Response.ResponseBuilder responseBuilder = RuntimeDelegate.getInstance().createResponseBuilder()
        .status(HttpServletResponse.SC_OK);

    Produces annotation = resource.getMethod().getAnnotation(Produces.class);
    if (annotation != null) {
      String[] mediaTypes = annotation.value();
      if (mediaTypes.length > 1) {
        throw new IllegalArgumentException("Only one type of response is supported. Found: " + Arrays.toString(mediaTypes) + " in resource: " + resource);
      }
      String mediaType = mediaTypes[0];
      if (MediaType.APPLICATION_JSON.equals(mediaType)) {
        responseBuilder
            .entity(convertToJson(resultObject))
            .type(MediaType.APPLICATION_JSON_TYPE);
      } else if (MediaType.TEXT_PLAIN.equals(mediaType)) {
        responseBuilder
            .entity(resultObject.toString())
            .type(MediaType.TEXT_PLAIN_TYPE);
      } else {
        throw new IllegalArgumentException("Not supported media type: " + mediaType + " in resource " + resource);
      }
    } else {
      //assume json
      responseBuilder
          .entity(convertToJson(resultObject))
          .type(MediaType.APPLICATION_JSON_TYPE);
    }

    return responseBuilder.build();
  }

  private Response handleError(final Throwable e) {

    if (e instanceof WebApplicationException) {
      WebApplicationException webE = (WebApplicationException) e;
      return webE.getResponse();
    }
    return RuntimeDelegate.getInstance().createResponseBuilder()
        .status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
        .entity(e.toString())
        .type(MediaType.TEXT_PLAIN_TYPE)
        .build();
  }

  private void writeAnswer(final HttpServletResponse servletResponse, final Response response) {
    try {
      final String entityMessage = (String) response.getEntity();

      servletResponse.getWriter().print(entityMessage);
      servletResponse.setStatus(response.getStatus());
      Object contentType = response.getMetadata().getFirst(HttpHeaders.CONTENT_TYPE);
      if (contentType != null) {
        servletResponse.setContentType(contentType.toString());
      }

      MultivaluedMap<String, Object> metadata = response.getMetadata();
      if (metadata != null) {
        for (String s : metadata.keySet()) {
          servletResponse.setHeader(s, ResponseImpl.getHeaderString(ResponseImpl.toListOfStrings(metadata.get(s))));
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Object[] buildMethodParams(final Map parameterMap, final HttpServletRequest request) {
    Annotation[][] parameterAnnotations = resource.getMethod().getParameterAnnotations();
    Object[] result = new Object[parameterAnnotations.length];
    for (int i = 0; i < parameterAnnotations.length; i++) {
      Annotation[] annotations = parameterAnnotations[i];
      Class<?> aClass = resource.getMethod().getParameterTypes()[i];
      result[i] = getParameterValue(annotations, parameterMap, aClass, request);
    }
    return result;
  }

  private Object getParameterValue(final Annotation[] annotations, final Map requestParameterMap, final Class<?> parameterType, final HttpServletRequest request) {
    if (annotations.length == 0) {
      //we assume that an parameter without annotation is passed via request
      Gson gson = new Gson();
      Object object;
      try {
        object = gson.fromJson(request.getReader(), parameterType);
      } catch (IOException e) {
        throw new RuntimeException("Could not parse json argument", e);
      }
      return object;

    }
    for (Annotation annotation : annotations) {
      if (annotation.annotationType().equals(PathParam.class)) {
        PathParam pathParam = (PathParam) annotation;
        final String paramName = pathParam.value();
        return this.parameterMap.getValue(paramName);
      }
      if (annotation.annotationType().equals(QueryParam.class)) {
        QueryParam pathParam = (QueryParam) annotation;
        final String paramName = pathParam.value();
        Object o = requestParameterMap.get(paramName);
        if (o == null) {
          for (Annotation annotation1 : annotations) {
            if (annotation1.annotationType().equals(DefaultValue.class)) {
              DefaultValue defaultValue = (DefaultValue) annotation1;
              o = defaultValue.value();
              break;
            }
          }
        }
        //FIXME Use java default according to the parameter type
        if (o == null) {
          throw new RuntimeException("No QueryParam and no default value specified");
        }
        return convertToObject(o.toString(), parameterType);
      }

      if (annotation.annotationType().equals(Context.class)) {
        if (HttpServletRequest.class.isAssignableFrom(parameterType)) {
          return request;
        }
      }
    }
    return null;
  }

  private String convertToJson(final Object invoke) {
    Gson gson = new Gson();
    return gson.toJson(invoke);
  }

  private Object convertToObject(final String json, final Class type) {
    Gson gson = new Gson();
    return gson.fromJson(json, type);
  }


  public String getMethodName() {
    return resource.getMethodName();
  }
}
