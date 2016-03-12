package ch.rabbithole.sra.resource;

import java.io.IOException;
import java.lang.annotation.Annotation;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import ch.rabbithole.sra.ObjectUtil;
import ch.rabbithole.sra.impl.UriInfoImpl;
import ch.rabbithole.sra.resource.message.MessageBodyReaderWriter;
import ch.rabbithole.sra.resource.message.MessageBodyReaderWriterProvider;

public abstract class ParamType {

  protected final Annotation[] annotations;
  protected final Class<?> paramType;

  public ParamType(final Annotation[] annotations, final Class<?> paramType) {
    this.annotations = annotations;
    this.paramType = paramType;
  }

  public abstract Object getValue(final MessageBodyReaderWriterProvider registry,
                                  final UriInfoImpl uriInfo,
                                  final HttpServletRequest request, final MultivaluedMap<String, String> headers);

  /**
   * Param type for context annotated params.
   */
  public static final class ContextParam extends ParamType {

    public ContextParam(final Annotation[] annotations, final Class<?> paramType) {
      super(annotations, paramType);
    }

    @Override
    public Object getValue(final MessageBodyReaderWriterProvider registry,
                           final UriInfoImpl uriInfo,
                           final HttpServletRequest request, final MultivaluedMap<String, String> headers) {
      if (HttpServletRequest.class.isAssignableFrom(paramType)) {
        return request;
      }
      throw new IllegalArgumentException("Unsupported context param: " + paramType);
    }
  }

  /**
   * Param type for parameter annotated with {@link javax.ws.rs.PathParam}
   */
  public static final class PathParam extends ParamType {

    private final String paramName;

    public PathParam(final Annotation[] annotations, final Class<?> paramType, final String paramName) {
      super(annotations, paramType);
      this.paramName = paramName;
    }

    @Override
    public Object getValue(final MessageBodyReaderWriterProvider registry,
                           final UriInfoImpl uriInfo,
                           final HttpServletRequest request, final MultivaluedMap<String, String> headers) {
      String value = uriInfo.getPathParameters().getFirst(paramName);
      if (paramType.equals(String.class)) {
        return value;
      }
      return ObjectUtil.fromJson(value, paramType);
    }
  }

  /**
   * Param type for {@link javax.ws.rs.QueryParam}
   */
  public static final class QueryParam extends ParamType {

    private final Object defaultValue;
    private final String queryParamName;

    public QueryParam(final Annotation[] annotations, final Class<?> paramType, final String queryParamName, final String defaultValue) {
      super(annotations, paramType);
      this.queryParamName = queryParamName;
      this.defaultValue = convertObject(defaultValue);
    }

    private Object convertObject(final String defaultValue) {
      if (paramType.equals(String.class)) {
        return defaultValue;
      }
      if (paramType.equals(String[].class)) {
        return new String[]{defaultValue};
      }
      return ObjectUtil.fromJson(defaultValue, paramType);
    }

    @Override
    public Object getValue(final MessageBodyReaderWriterProvider registry,
                           final UriInfoImpl uriInfo,
                           final HttpServletRequest request, final MultivaluedMap<String, String> headers) {
      String value = uriInfo.getQueryParameters().getFirst(queryParamName);
      return value == null ? defaultValue : convertObject(value);
    }
  }

  /**
   * Param type for parameters representing the entity body of the request. (PUT and POST)
   */
  public static final class ObjectParam extends ParamType {

    private final MediaType type;

    public ObjectParam(final MediaType type, final Annotation[] annotations, final Class<?> paramType) {
      super(annotations, paramType);
      this.type = type;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getValue(final MessageBodyReaderWriterProvider registry,
                           final UriInfoImpl uriInfo,
                           final HttpServletRequest request, final MultivaluedMap<String, String> headers) {
      try {
        final MessageBodyReaderWriter<Object> writer = registry.get(type);
        return writer.readFrom((Class)paramType, null, annotations, type, headers, request.getInputStream());
      } catch (IOException e) {
        throw new RuntimeException("Could not parse param: " + request + " paramType. " + paramType);
      }
    }
  }

}
