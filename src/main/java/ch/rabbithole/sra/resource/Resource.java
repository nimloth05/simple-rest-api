package ch.rabbithole.sra.resource;

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
}
