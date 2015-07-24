package com.sun.jersey.api;

import javax.ws.rs.WebApplicationException;

import ch.rabbithole.sra.ResponseUtil;

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
