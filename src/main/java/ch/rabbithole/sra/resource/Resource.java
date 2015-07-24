package ch.rabbithole.sra.resource;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Wraps a method into a {@link ResourceNode}
 */
public final class Resource implements ResourceNode {

  private final Method method;

  public Resource(final Method method) {
    this.method = method;
  }

  public String getMethodName() {
    return method.getName();
  }

  public Method getMethod() {
    return method;
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

  public Annotation[][] getParameterAnnotations() {
    return method.getParameterAnnotations();
  }

  public Class<?> getParameterTypes(final int index) {
    return method.getParameterTypes()[index];
  }
}
