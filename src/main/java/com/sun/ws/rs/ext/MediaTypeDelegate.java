package com.sun.ws.rs.ext;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * MediaType delegate impl
 */
public final class MediaTypeDelegate implements RuntimeDelegate.HeaderDelegate<MediaType> {

  @Override
  public MediaType fromString(final String value) throws IllegalArgumentException {
    String[] split = value.split("/");
    return new MediaType(split[0], split[1]);
  }

  @Override
  public String toString(final MediaType value) {
    return value.getType() + "/" + value.getSubtype();
  }

}
