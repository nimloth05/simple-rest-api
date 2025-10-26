package com.sun.ws.rs.ext;

import ch.rabbithole.sra.impl.UriBuilderImpl;
import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.RuntimeDelegate;

import java.util.concurrent.CompletionStage;

/**
 * A simple implementation of a {@link RuntimeDelegate}
 */
public final class RuntimeDelegateImpl extends RuntimeDelegate {

  @Override
  public UriBuilder createUriBuilder() {
    return new UriBuilderImpl();
  }

  @Override
  public Response.ResponseBuilder createResponseBuilder() {
    return new ResponseBuilderImpl();
  }

  @Override
  public Variant.VariantListBuilder createVariantListBuilder() {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T createEndpoint(final Application application, final Class<T> aClass) throws IllegalArgumentException, UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> HeaderDelegate<T> createHeaderDelegate(final Class<T> aClass) throws IllegalArgumentException {
    if (aClass.equals(MediaType.class)) {
      return (HeaderDelegate<T>) new MediaTypeDelegate();
    }
    throw new RuntimeException("Header not supported: " + aClass);
  }

  @Override
  public Link.Builder createLinkBuilder() {
    return null;
  }

  @Override
  public SeBootstrap.Configuration.Builder createConfigurationBuilder() {
    return null;
  }

  @Override
  public CompletionStage<SeBootstrap.Instance> bootstrap(Application application, SeBootstrap.Configuration configuration) {
    return null;
  }

  @Override
  public CompletionStage<SeBootstrap.Instance> bootstrap(Class<? extends Application> clazz, SeBootstrap.Configuration configuration) {
    return null;
  }

  @Override
  public EntityPart.Builder createEntityPartBuilder(String partName) throws IllegalArgumentException {
    return null;
  }


}
