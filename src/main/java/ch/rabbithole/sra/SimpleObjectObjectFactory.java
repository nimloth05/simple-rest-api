package ch.rabbithole.sra;

/**
 * TODO JavaDoc
 */
public final class SimpleObjectObjectFactory implements ObjectFactory {

  @Override
  public Object createInstance(final Class<?> clazz) {
    try {
      return clazz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
