package ch.rabbithole.sra;

/**
 * TODO JavaDoc
 */
public final class SimpleObjectInstantiator implements Instantiator {

  @Override
  public Object createInstance(final Class<?> clazz) {
    try {
      return clazz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
