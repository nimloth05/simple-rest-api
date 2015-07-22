package ch.rabbithole.sra;

import com.google.gson.Gson;

import com.sun.ws.rs.ext.ResponseImpl;
import com.sun.xml.internal.ws.api.wsdl.parser.MetaDataResolver;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
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

    Object[] params = buildMethodParams(req.getParameterMap());

    Response response = executeResourceMethod(resp, instance, params);
    writeAnswer(resp, response);
  }

  private Response executeResourceMethod(final HttpServletResponse resp, final Object instance, final Object[] params) {
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
    return RuntimeDelegate.getInstance().createResponseBuilder()
        .entity(convertToJson(resultObject))
        .status(HttpServletResponse.SC_OK)
        .type(MediaType.APPLICATION_JSON_TYPE)
        .build();
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

  private Object[] buildMethodParams(final Map parameterMap) {
    Annotation[][] parameterAnnotations = resource.getMethod().getParameterAnnotations();
    Object[] result = new Object[parameterAnnotations.length];
    for (int i = 0; i < parameterAnnotations.length; i++) {
      Annotation[] annotations = parameterAnnotations[i];
      Class<?> aClass = resource.getMethod().getParameterTypes()[i];
      result[i] = getParameterValue(annotations, parameterMap, aClass);
    }
    return result;
  }

  private Object getParameterValue(final Annotation[] annotations, final Map requestParameterMap, final Class<?> parameterType) {
    for (Annotation annotation : annotations) {
      if (annotation.annotationType().equals(PathParam.class)) {
        PathParam pathParam = (PathParam) annotation;
        final String paramName = pathParam.value();
        return this.parameterMap.getValue(paramName);
      }
      if (annotation.annotationType().equals(QueryParam.class)) {
        QueryParam pathParam = (QueryParam) annotation;
        final String paramName = pathParam.value();
        return convertToObject(requestParameterMap.get(paramName).toString(), parameterType);
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
