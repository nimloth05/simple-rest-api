package ch.rabbithole.sra.resource;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import ch.rabbithole.sra.impl.UriInfoImpl;

/**
 * Wraps a method into a {@link ResourceNode}
 */
public final class Resource implements ResourceNode {

  private final Method method;
  private final ParamType[] paramTypes;

  public Resource(final Method method) {
    this.method = method;
    paramTypes = getParamTypes();
  }

  private ParamType[] getParamTypes() {
    Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    ParamType[] paramType = new ParamType[parameterAnnotations.length];

    for (int i = 0; i < parameterAnnotations.length; i++) {
      Class<?> aClass = method.getParameterTypes()[i];
      Annotation[] annotation = parameterAnnotations[i];
      paramType[i] = createParamType(aClass, annotation);
    }

    return paramType;
  }

  private ParamType createParamType(final Class<?> paramType, final Annotation[] annotations) {
    if (annotations.length == 0) {
      return new ParamType.ObjectParam(annotations, paramType);
    }

    for (Annotation annotation : annotations) {
      if (annotation.annotationType().equals(Context.class)) {
        return new ParamType.ContextParam(annotations, paramType);
      }

      if (annotation.annotationType().equals(PathParam.class)) {
        PathParam pathParam = (PathParam) annotation;
        return new ParamType.PathParam(annotations, paramType, pathParam.value());
      }
      if (annotation.annotationType().equals(QueryParam.class)) {
        QueryParam queryParam = (QueryParam) annotation;
        final String paramName = queryParam.value();
        String defaultStringValue = null;
        for (Annotation subAnnotation : annotations) {
          if (subAnnotation.annotationType().equals(DefaultValue.class)) {
            DefaultValue defaultValue = (DefaultValue) subAnnotation;
            defaultStringValue = defaultValue.value();
          }
        }
        return new ParamType.QueryParam(annotations, paramType, paramName, defaultStringValue);
      }
    }
    throw new IllegalArgumentException("Cannot process param type: " + paramType);
  }

  public String getMethodName() {
    return method.getName();
  }

  @Override
  public String toString() {
    return method.getName();
  }

  public Class<?> getDeclaringClass() {
    return method.getDeclaringClass();
  }

  public Object invoke(Object obj, Object... args) throws InvocationTargetException, IllegalAccessException {
    return method.invoke(obj, args);
  }

  public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
    return method.getAnnotation(annotationClass);
  }

  public Object[] buildMethodParams(final UriInfoImpl uriInfo, final HttpServletRequest req) {
    Object[] paramValues = new Object[paramTypes.length];
    for (int i = 0; i < paramValues.length; i++) {
      paramValues[i] = paramTypes[i].getValue(uriInfo, req);
    }
    return paramValues;
  }

  public Annotation[] getAnnotations() {
    return method.getAnnotations();
  }
}
