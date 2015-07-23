package ch.rabbithole.sra;

import com.google.inject.Injector;

import ch.rabbithole.sra.resource.ObjectFactory;

/**
 * Object factory for guice.
 */
public final class GuiceObjectFactory implements ObjectFactory {

  private final Injector injector;

  public GuiceObjectFactory(final Injector injector) {
    this.injector = injector;
  }

  @Override
  public Object createInstance(final Class<?> clazz) {
    return injector.getInstance(clazz);
  }
}
