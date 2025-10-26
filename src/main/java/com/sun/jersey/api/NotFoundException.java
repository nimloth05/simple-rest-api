package com.sun.jersey.api;

import ch.rabbithole.sra.ResponseUtil;
import jakarta.ws.rs.WebApplicationException;

/**
 * Exception for 404.
 */
public final class NotFoundException extends WebApplicationException{

  public NotFoundException(final String message) {
    super(ResponseUtil.notFound(message));
  }

  public NotFoundException() {
    super(ResponseUtil.notFound("Resource not found"));
  }

}
