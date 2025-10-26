package ch.rabbithole.sra;

import ch.rabbithole.sra.resource.ObjectFactory;
import com.google.inject.Injector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

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
