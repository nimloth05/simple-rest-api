package ch.rabbithole.sra;

import com.google.inject.Injector;

import javax.inject.Inject;
import javax.inject.Singleton;

import ch.rabbithole.sra.resource.ObjectFactory;

/**
 * Object factory with guice.
 */
@Singleton
public final class GuiceObjectFactory implements ObjectFactory {

  private final Injector injector;

  @Inject
  public GuiceObjectFactory(final Injector injector) {
    this.injector = injector;
  }

  @Override
  public Object createInstance(final Class<?> clazz) {
    return injector.getInstance(clazz);
  }
}
