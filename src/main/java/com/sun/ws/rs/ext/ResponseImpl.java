package com.sun.ws.rs.ext;


import com.google.common.base.Function;
import com.google.common.collect.Lists;
import jakarta.ws.rs.core.*;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.*;

/**
 * TODO JavaDoc
 */
public final class ResponseImpl extends Response {

  private int status;
  private Object entity;
  private Date lastModified;
  private String encoding;
  private MultivaluedMap<String, Object> metadata = new MultivaluedHashMap<>();
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
  public StatusType getStatusInfo() {
    return null;
  }

  @Override
  public Object getEntity() {
    return entity;
  }

  @Override
  public <T> T readEntity(Class<T> entityType) {
    return null;
  }

  @Override
  public <T> T readEntity(GenericType<T> entityType) {
    return null;
  }

  @Override
  public <T> T readEntity(Class<T> entityType, Annotation[] annotations) {
    return null;
  }

  @Override
  public <T> T readEntity(GenericType<T> entityType, Annotation[] annotations) {
    return null;
  }

  @Override
  public boolean hasEntity() {
    return false;
  }

  @Override
  public boolean bufferEntity() {
    return false;
  }

  @Override
  public void close() {

  }

  @Override
  public MediaType getMediaType() {
    return null;
  }

  @Override
  public Locale getLanguage() {
    return null;
  }

  @Override
  public int getLength() {
    return 0;
  }

  @Override
  public Set<String> getAllowedMethods() {
    return Set.of();
  }

  @Override
  public Map<String, NewCookie> getCookies() {
    return Map.of();
  }

  @Override
  public EntityTag getEntityTag() {
    return null;
  }

  @Override
  public Date getDate() {
    return null;
  }

  @Override
  public Date getLastModified() {
    return null;
  }

  @Override
  public URI getLocation() {
    return null;
  }

  @Override
  public Set<Link> getLinks() {
    return Set.of();
  }

  @Override
  public boolean hasLink(String relation) {
    return false;
  }

  @Override
  public Link getLink(String relation) {
    return null;
  }

  @Override
  public Link.Builder getLinkBuilder(String relation) {
    return null;
  }

  @Override
  public MultivaluedMap<String, Object> getMetadata() {
    return metadata;
  }

  @Override
  public MultivaluedMap<String, String> getStringHeaders() {
    return null;
  }

  @Override
  public String getHeaderString(String name) {
    return "";
  }
}
