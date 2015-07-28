package ch.rabbithole.sra.resource;

import java.io.IOException;
import java.lang.annotation.Annotation;

import javax.servlet.http.HttpServletRequest;

import ch.rabbithole.sra.ObjectUtil;
import ch.rabbithole.sra.impl.UriInfoImpl;

public abstract class ParamType {

  protected final Annotation[] annotations;
  protected final Class<?> paramType;

  public ParamType(final Annotation[] annotations, final Class<?> paramType) {
    this.annotations = annotations;
    this.paramType = paramType;
  }

  public abstract Object getValue(final UriInfoImpl uriInfo, final HttpServletRequest request);

  /**
   * Param type for context annotated params.
   */
  public static final class ContextParam extends ParamType {

    public ContextParam(final Annotation[] annotations, final Class<?> paramType) {
      super(annotations, paramType);
    }

    @Override
    public Object getValue(final UriInfoImpl uriInfo, final HttpServletRequest request) {
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
    public Object getValue(final UriInfoImpl uriInfo, final HttpServletRequest request) {
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
    public Object getValue(final UriInfoImpl uriInfo, final HttpServletRequest request) {
      String value = uriInfo.getQueryParameters().getFirst(queryParamName);
      return value == null ? defaultValue : convertObject(value);
    }
  }

  /**
   * Param type for parameters representing the entity body of the request. (PUT and POST)
   */
  public static final class ObjectParam extends ParamType {

    public ObjectParam(final Annotation[] annotations, final Class<?> paramType) {
      super(annotations, paramType);
    }

    @Override
    public Object getValue(final UriInfoImpl uriInfo, final HttpServletRequest request) {
      try {
        return ObjectUtil.fromJson(request.getReader(), paramType);
      } catch (IOException e) {
        throw new RuntimeException("Could not parse param: " + request + " paramType. " + paramType);
      }
    }
  }

}
