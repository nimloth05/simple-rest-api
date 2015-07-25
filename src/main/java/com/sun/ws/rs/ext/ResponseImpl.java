package com.sun.ws.rs.ext;


import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.net.URI;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * TODO JavaDoc
 */
public final class ResponseImpl extends Response {

  private int status;
  private Object entity;
  private Date lastModified;
  private String encoding;
  private MultivaluedMap<String, Object> metadata = new MultiValueMapImpl<>();
  private String language;
  private String mediaType;
  private URI uri;
  private Date expire;

  protected ResponseImpl(final ResponseBuilderImpl impl) {
    status = impl.getStatus();
    entity = impl.getEntity();
    lastModified = impl.getLastModified();
    encoding = impl.getEncoding();
    language = impl.getLanguage();
    metadata = impl.getMetadata();
    uri = impl.getUri();
    expire = impl.getExpire();

  }

  public static String getHeaderString(List<String> values) {
    if (values == null) {
      return null;
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < values.size(); i++) {
      String value = values.get(i);
      if (value == null || value.isEmpty()) {
        continue;
      }
      sb.append(value);
      if (i + 1 < values.size()) {
        sb.append(",");
      }
    }
    return sb.toString();
  }

  public static int getContentLength(String value) {
    if (value == null) {
      return -1;
    }
    try {
      int len = Integer.valueOf(value);
      return len >= 0 ? len : -1;
    } catch (Exception ex) {
      return -1;
    }
  }

  public static List<String> toListOfStrings(List<Object> values) {
    if (values == null) {
      return null;
    } else {
      return Lists.transform(values, new Function<Object, String>() {
        @Override
        public String apply(final Object input) {
          return input.toString();
        }
      });
    }
  }

  @Override
  public int getStatus() {
    return status;
  }

  @Override
  public Object getEntity() {
    return entity;
  }

  @Override
  public MultivaluedMap<String, Object> getMetadata() {
    return metadata;
  }
}
