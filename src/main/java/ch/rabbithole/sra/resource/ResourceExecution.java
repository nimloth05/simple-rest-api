package ch.rabbithole.sra.resource;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

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

import ch.rabbithole.sra.ObjectUtil;
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

    Object[] params = buildMethodParams(uriInfoImpl);

    Response response = executeResourceMethod(instance, params);
    writeAnswer( response);
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
    Response.ResponseBuilder responseBuilder = RuntimeDelegate.getInstance().createResponseBuilder();
    if (resultObject == null) {
      responseBuilder.status(HttpServletResponse.SC_NO_CONTENT);
      return responseBuilder.build();
    }

    responseBuilder.status(HttpServletResponse.SC_OK);
    Produces annotation = resource.getAnnotation(Produces.class);
    if (annotation != null) {
      String[] mediaTypes = annotation.value();
      if (mediaTypes.length > 1) {
        throw new IllegalArgumentException("Only one type of response is supported. Found: " + Arrays.toString(mediaTypes) + " in resource: " + resource);
      }

      String mediaType = mediaTypes[0];
      if (MediaType.APPLICATION_JSON.equals(mediaType)) {
        responseBuilder
            .entity(ObjectUtil.toJson(resultObject))
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
          .entity(ObjectUtil.toJson(resultObject))
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

  private void writeAnswer( final Response response) {
    try {
      final String entityMessage = (String) response.getEntity();
      writeToOutputStream(resp, entityMessage);

      resp.setStatus(response.getStatus());
      Object contentType = response.getMetadata().getFirst(HttpHeaders.CONTENT_TYPE);
      if (contentType != null) {
        resp.setContentType(contentType.toString());
      }

      MultivaluedMap<String, Object> metadata = response.getMetadata();
      if (metadata != null) {
        for (String headerKey : metadata.keySet()) {
          List<Object> values = metadata.get(headerKey);
          if (headerKey.equals(HttpHeaders.LOCATION)) {
            values = toAbsoluteUrls(values);
          }
          final String headerString = ResponseImpl.getHeaderString(ResponseImpl.toListOfStrings(values));
          resp.setHeader(headerKey, headerString);
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

  private Object[] buildMethodParams(final UriInfoImpl uriInfo) {
    return resource.buildMethodParams(uriInfo, req);
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
