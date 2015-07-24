package com.sun.ws.rs.ext;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * A simple implementation of a {@link RuntimeDelegate}
 */
public final class RuntimeDelegateImpl extends RuntimeDelegate {

  @Override
  public UriBuilder createUriBuilder() {
    throw new UnsupportedOperationException();
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
