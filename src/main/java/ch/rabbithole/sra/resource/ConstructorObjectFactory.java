package ch.rabbithole.sra.resource;

/**
 * TODO JavaDoc
 */
public final class ConstructorObjectFactory implements ObjectFactory {

  @Override
  public Object createInstance(final Class<?> clazz) {
    try {
      return clazz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
