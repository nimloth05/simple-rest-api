package ch.rabbithole.sra.resource;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import com.sun.ws.rs.ext.MultiValueMapImpl;
import com.sun.ws.rs.ext.ResponseImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.RuntimeDelegate;

import ch.rabbithole.sra.CountingOutputStream;
import ch.rabbithole.sra.HeaderUtil;
import ch.rabbithole.sra.impl.UriInfoImpl;
import ch.rabbithole.sra.resource.message.MessageBodyReaderWriter;
import ch.rabbithole.sra.resource.message.MessageBodyReaderWriterProvider;

public final class ResourceExecution {

  private static final Logger log = Logger.getLogger(ResourceExecution.class.getName());

  @Nonnull
  private final MessageBodyReaderWriterProvider provider;

  private final Resource resource;
  private final ObjectFactory objectFactory;
  @Nonnull
  private final HttpServletRequest req;
  @Nonnull
  private final HttpServletResponse resp;
  private UriInfoImpl uriInfoImpl;

  public ResourceExecution(@Nonnull final MessageBodyReaderWriterProvider provider,
                           @Nonnull final ObjectFactory objectFactory,
                           @Nonnull final Resource resource,
                           @Nonnull final UriInfoImpl uriInfo,
                           @Nonnull final HttpServletRequest req,
                           @Nonnull final HttpServletResponse resp) {
    this.provider = provider;
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
    writeAnswer(response);
  }

  private void setContextFields(Object instance) {
    final Field[] fields = instance.getClass().getDeclaredFields();
    for (Field field : fields) {
      final Annotation[] annotations = field.getAnnotations();
      for (Annotation annotation : annotations) {
        if (annotation.annotationType().equals(Context.class)) {
          if (UriInfo.class.isAssignableFrom(field.getType())) {
            setFieldValue(instance, field, uriInfoImpl);
          } else if (Client.class.isAssignableFrom(field.getType())) {
            setFieldValue(instance, field, new Client(provider));
          }
        }
      }
    }
  }

  private void setFieldValue(final Object instance, final Field field, final Object parameter) {
    try {
      field.set(instance, parameter);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Field does not have sufficient access privileges: " + field, e);
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
      log.log(Level.SEVERE, "Resource method threw exception", e.getCause());
      return handleError(e.getCause());
    } catch (IllegalAccessException e) {
      log.log(Level.SEVERE, "Field has not enough visibility", e.getCause());
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
      responseBuilder
          .type(mediaType)
          .entity(resultObject);

    } else {
      //no produces annotation, assume json
      responseBuilder
          .type(MediaType.APPLICATION_JSON_TYPE)
          .entity(resultObject);
    }

    return responseBuilder.build();
  }

  private Response handleError(final Throwable e) {
    if (e instanceof WebApplicationException) {
      WebApplicationException webE = (WebApplicationException) e;
      return webE.getResponse();
    }

    StringWriter writer = new StringWriter();
    writer.write(e.toString());

    return RuntimeDelegate.getInstance().createResponseBuilder()
        .status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
        .entity(writer.toString())
        .type(MediaType.TEXT_PLAIN_TYPE)
        .build();
  }

  private void writeAnswer(final Response response) {
    try {
      MultivaluedMap<String, Object> metadata = response.getMetadata();

      final Object entity = response.getEntity();
      CountingOutputStream cos;
      if (response.getStatus() >= 200 && response.getStatus() <= 399) {
        try {
          cos = writeEntity(entity, metadata);
          resp.setStatus(response.getStatus());

          Object contentType = metadata.getFirst(HttpHeaders.CONTENT_TYPE);
          if (contentType != null) {
            resp.setContentType(contentType.toString());
          }
        } catch (WebApplicationException e) {
          log.log(Level.SEVERE, "Error during response processing", e);
          log.severe("Error during writing response: " + e);
          resp.setStatus(e.getResponse().getStatus());
          cos = writeEntity(e.getResponse().getEntity().toString(), createTextPlainContentTypeMultiMap());
        }
      } else {
        resp.setStatus(response.getStatus());
        cos = writeEntity(entity != null ? entity.toString() : "", createTextPlainContentTypeMultiMap());
      }

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
      writeToResponse(cos);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private MultivaluedMap<String, Object> createTextPlainContentTypeMultiMap() {
    MultiValueMapImpl<String, Object> result = new MultiValueMapImpl<>();
    result.putSingle(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
    return result;
  }

  private CountingOutputStream writeEntity(final Object entity, final MultivaluedMap<String, Object> metadata) throws IOException {
    MediaType contentType = MediaType.APPLICATION_JSON_TYPE;
    if (metadata.containsKey(HttpHeaders.CONTENT_TYPE)) {
      contentType = MediaType.valueOf(metadata.getFirst(HttpHeaders.CONTENT_TYPE).toString());
    }

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    CountingOutputStream cos = new CountingOutputStream(bos);
    if (entity != null) {
      final MessageBodyReaderWriter<Object> writer = provider.get(contentType);
      writer.writeTo(entity, entity.getClass(), null, resource.getAnnotations(), contentType, metadata, cos);
    }

    return cos;
  }

  private void writeToResponse(final CountingOutputStream cos) throws IOException {
    resp.setContentLength((int) cos.getTransferred());
    OutputStream os = resp.getOutputStream();
    os.write(((ByteArrayOutputStream) cos.getUnderlyingStream()).toByteArray());
    os.close();
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
    return resource.buildMethodParams(provider, uriInfo, req, HeaderUtil.toMap(req));
  }

  public String getMethodName() {
    return resource.getMethodName();
  }
}
