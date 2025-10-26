package com.sun.ws.rs.ext;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.Variant;
import jakarta.ws.rs.ext.RuntimeDelegate;

import ch.rabbithole.sra.impl.UriBuilderImpl;

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


}
