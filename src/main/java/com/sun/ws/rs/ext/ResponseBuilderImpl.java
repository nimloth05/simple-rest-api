package com.sun.ws.rs.ext;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

/**
 * Impl for {@link javax.ws.rs.core.Response.ResponseBuilder}
 */
public class ResponseBuilderImpl extends Response.ResponseBuilder {

  private int status;
  private Object entity;
  private Date lastModified;
  private String encoding;
  private MultivaluedMap<String, Object> metadata = new MultiValueMapImpl<>();
  private String language;
  private URI uri;
  private Date expire;

  @Override
  public Response build() {
    return new ResponseImpl(this);
  }

  @Override
  public Response.ResponseBuilder clone() {
    return new ResponseBuilderImpl();
  }

  @Override
  public Response.ResponseBuilder status(final int status) {
    this.status = status;
    return this;
  }

  @Override
  public Response.ResponseBuilder entity(final Object entity) {
    this.entity = entity;
    return this;
  }


  @Override
  public Response.ResponseBuilder cacheControl(final CacheControl cacheControl) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Response.ResponseBuilder header(final String s, final Object o) {
    metadata.putSingle(s, o);
    return this;
  }


  @Override
  public Response.ResponseBuilder language(final String language) {
    this.language = language;
    return this;
  }

  @Override
  public Response.ResponseBuilder language(final Locale locale) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Response.ResponseBuilder type(final MediaType mediaType) {
    return type(mediaType.toString());
  }

  @Override
  public Response.ResponseBuilder type(final String mediaType) {
    header(HttpHeaders.CONTENT_TYPE, mediaType);
    return this;
  }

  @Override
  public Response.ResponseBuilder variant(final Variant variant) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Response.ResponseBuilder contentLocation(final URI uri) {
    this.uri = uri;
    return this;
  }

  @Override
  public Response.ResponseBuilder cookie(final NewCookie... newCookies) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Response.ResponseBuilder expires(final Date expire) {
    this.expire = expire;
    return this;
  }

  @Override
  public Response.ResponseBuilder lastModified(final Date lastModified) {
    this.lastModified = lastModified;
    return this;
  }

  @Override
  public Response.ResponseBuilder location(final URI uri) {
    header(HttpHeaders.LOCATION, uri.toString());
    return this;
  }

  @Override
  public Response.ResponseBuilder tag(final EntityTag entityTag) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Response.ResponseBuilder tag(final String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Response.ResponseBuilder variants(final List<Variant> list) {
    throw new UnsupportedOperationException();
  }


  public String getEncoding() {
    return encoding;
  }

  public Object getEntity() {
    return entity;
  }

  public Date getExpire() {
    return expire;
  }

  public String getLanguage() {
    return language;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public MultivaluedMap<String, Object> getMetadata() {
    return metadata;
  }

  public int getStatus() {
    return status;
  }

  public URI getUri() {
    return uri;
  }
}
