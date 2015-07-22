package com.sun.ws.rs.ext;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * MediaType delegate impl
 */
public final class MediaTypeDelegate implements RuntimeDelegate.HeaderDelegate<MediaType> {

  @Override
  public MediaType fromString(final String value) throws IllegalArgumentException {
    com.google.common.net.MediaType parse = com.google.common.net.MediaType.parse(value);
    return new MediaType(parse.type(), parse.subtype());
  }

  @Override
  public String toString(final MediaType value) {
    return value.getType() + "/" + value.getSubtype();
  }

}
