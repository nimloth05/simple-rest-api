package ch.rabbithole.sra.resource;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import com.sun.ws.rs.ext.ResponseImpl;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.RuntimeDelegate;

import ch.rabbithole.sra.impl.UriInfoImpl;

public final class ResourceExecution {

  private final Resource resource;
  private final ObjectFactory objectFactory;
  @NotNull
  private final HttpServletRequest req;
  @NotNull
  private final HttpServletResponse resp;
  private UriInfoImpl uriInfoImpl;

  public ResourceExecution(@NotNull final Resource resource,
                           @NotNull final ObjectFactory objectFactory,
                           @NotNull final UriInfoImpl uriInfo,
                           @NotNull final HttpServletRequest req,
                           @NotNull final HttpServletResponse resp) {
    this.resource = resource;
    this.objectFactory = objectFactory;
    this.uriInfoImpl = uriInfo;
    this.req = req;
    this.resp = resp;
  }

  public void execute() {
    Class<?> declaringClass = resource.getDeclaringClass();
    Object instance = objectFactory.createInstance(declaringClass);

    setContextFields(instance);

    Object[] params = buildMethodParams(uriInfoImpl, req);

    Response response = executeResourceMethod(instance, params);
    writeAnswer(resp, response);
  }

  private void setContextFields(Object instance) {
    final Field[] fields = instance.getClass().getDeclaredFields();
    for (Field field : fields) {
      if (field.getType().equals(UriInfo.class)) {
        final Annotation[] annotations = field.getAnnotations();
        for (Annotation annotation : annotations) {
          if (annotation.annotationType().equals(Context.class)) {
            try {
              field.set(instance, uriInfoImpl);
            } catch (IllegalAccessException e) {
              throw new RuntimeException("Field does not have sufficient access privileges: " + field, e);
            }
          }
        }
      }
    }
  }

  private Response executeResourceMethod(final Object instance, final Object[] params) {
    try {
      Object resultObject = resource.invoke(instance, params);
      if (resultObject instanceof Response) {
        return (Response) resultObject;
      }
      return buildResponseObject(resultObject);
    } catch (InvocationTargetException e) {
      return handleError(e.getCause());
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Method must be public: " + resource);
    }
  }

  private Response buildResponseObject(final Object resultObject) {
    Response.ResponseBuilder responseBuilder = RuntimeDelegate.getInstance().createResponseBuilder()
        .status(HttpServletResponse.SC_OK);

    Produces annotation = resource.getAnnotation(Produces.class);
    if (annotation != null) {
      String[] mediaTypes = annotation.value();
      if (mediaTypes.length > 1) {
        throw new IllegalArgumentException("Only one type of response is supported. Found: " + Arrays.toString(mediaTypes) + " in resource: " + resource);
      }

      String mediaType = mediaTypes[0];
      if (MediaType.APPLICATION_JSON.equals(mediaType)) {
        responseBuilder
            .entity(convertToJson(resultObject))
            .type(createMediaTypeWithEncoding(MediaType.APPLICATION_JSON_TYPE));

      } else if (MediaType.TEXT_PLAIN.equals(mediaType)) {
        responseBuilder
            .entity(resultObject.toString())
            .type(createMediaTypeWithEncoding(MediaType.TEXT_PLAIN_TYPE));

      } else {
        throw new IllegalArgumentException("Not supported media type: " + mediaType + " in resource " + resource);
      }
    } else {
      //no produces annotation, assume json
      responseBuilder
          .entity(convertToJson(resultObject))
          .type(createMediaTypeWithEncoding(MediaType.APPLICATION_JSON_TYPE));
    }

    return responseBuilder.build();
  }

  private Response handleError(final Throwable e) {

    if (e instanceof WebApplicationException) {
      WebApplicationException webE = (WebApplicationException) e;
      return webE.getResponse();
    }

    StringWriter writer = new StringWriter();
    e.printStackTrace(new PrintWriter(writer));

    return RuntimeDelegate.getInstance().createResponseBuilder()
        .status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
        .entity(writer.toString())
        .type(MediaType.TEXT_PLAIN_TYPE)
        .build();
  }

  private void writeAnswer(final HttpServletResponse servletResponse, final Response response) {
    try {
      final String entityMessage = (String) response.getEntity();
      writeToOutputStream(servletResponse, entityMessage);

      servletResponse.setStatus(response.getStatus());
      Object contentType = response.getMetadata().getFirst(HttpHeaders.CONTENT_TYPE);
      if (contentType != null) {
        servletResponse.setContentType(contentType.toString());
      }

      MultivaluedMap<String, Object> metadata = response.getMetadata();
      if (metadata != null) {
        for (String headerKey : metadata.keySet()) {
          List<Object> values = metadata.get(headerKey);
          if (headerKey.equals(HttpHeaders.LOCATION)) {
            values = toAbsoluteUrls(values);
          }
          final String headerString = ResponseImpl.getHeaderString(ResponseImpl.toListOfStrings(values));
          servletResponse.setHeader(headerKey, headerString);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void writeToOutputStream(final HttpServletResponse servletResponse, final String entityMessage) throws IOException {
    if (entityMessage == null) {
      return;
    }
    OutputStream os = servletResponse.getOutputStream();
    OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
    writer.write(entityMessage);
    writer.close();
  }

  private List<Object> toAbsoluteUrls(List<Object> values) {
    return Lists.transform(values, new Function<Object, Object>() {
      @Override
      public Object apply(Object o) {
        final URI uri = URI.create(o.toString());
        if (uri.isAbsolute()) {
          return o;
        }
        return uriInfoImpl.getBaseUri().resolve(uri).toString();
      }
    });

  }

  private Object[] buildMethodParams(final UriInfoImpl uriInfo, final HttpServletRequest request) {
    Annotation[][] parameterAnnotations = resource.getParameterAnnotations();
    Object[] result = new Object[parameterAnnotations.length];
    for (int i = 0; i < parameterAnnotations.length; i++) {
      Annotation[] annotations = parameterAnnotations[i];
      Class<?> aClass = resource.getParameterTypes(i);
      result[i] = getParameterValue(annotations, uriInfo, aClass, request);
    }
    return result;
  }

  private Object getParameterValue(final Annotation[] annotations, final UriInfoImpl uriInfo, final Class<?> parameterType, final HttpServletRequest request) {
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
      if (annotation.annotationType().equals(Context.class)) {
        if (HttpServletRequest.class.isAssignableFrom(parameterType)) {
          return request;
        }
      }

      if (annotation.annotationType().equals(PathParam.class)) {
        PathParam pathParam = (PathParam) annotation;
        final String paramName = pathParam.value();
        final String value = this.uriInfoImpl.getPathParameters(true).getFirst(paramName);
        if (parameterType.equals(String.class)) {
          return value;
        }
        return convertToObject(value, parameterType);
      }

      if (annotation.annotationType().equals(QueryParam.class)) {
        QueryParam pathParam = (QueryParam) annotation;
        final String paramName = pathParam.value();
        List<String> values = uriInfo.getQueryParameters(true).get(paramName);
        Object result = null;

        if (values == null || values.isEmpty()) {
          for (Annotation annotation1 : annotations) {
            if (annotation1.annotationType().equals(DefaultValue.class)) {
              DefaultValue defaultValue = (DefaultValue) annotation1;
              result = convertToObject(defaultValue.value(), parameterType);
              break;
            }
          }
        } else {
          if (parameterType.equals(String.class)) {
            result = values.get(0);
          } else if (parameterType.equals(String[].class)) {
            result = values.toArray(new String[values.size()]);
          } else {
            result = convertToObject(values.get(0), parameterType);
          }
        }

        if (result == null) {
          result = getJavaDefault(parameterType);
        }

        return result;
      }

    }
    return null;
  }

  private Object getJavaDefault(Class<?> parameterType) {
    if (!parameterType.isPrimitive()) {
      return null;
    }

    if (parameterType.equals(long.class)) {
      return 0;
    }
    if (parameterType.equals(byte.class)) {
      return 0;
    }
    if (parameterType.equals(char.class)) {
      return 0;
    }
    if (parameterType.equals(short.class)) {
      return 0;
    }
    if (parameterType.equals(boolean.class)) {
      return 0;
    }
    if (parameterType.equals(int.class)) {
      return 0;
    }
    if (parameterType.equals(float.class)) {
      return 0.0f;
    }
    if (parameterType.equals(double.class)) {
      return 0.0f;
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

  private MediaType createMediaTypeWithEncoding(final MediaType mediaType) {
    Map<String, String> params = new HashMap<>();
    params.put("charset", "UTF-8");
    return new MediaType(mediaType.getType(), mediaType.getSubtype(), params);
  }

  public String getMethodName() {
    return resource.getMethodName();
  }
}
