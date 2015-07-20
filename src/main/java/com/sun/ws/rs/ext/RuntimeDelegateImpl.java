package com.sun.ws.rs.ext;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
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
    throw new RuntimeException("TBI");
  }

  @Override
  public Response.ResponseBuilder createResponseBuilder() {
    return new ResponseBuilderImpl();
  }

  @Override
  public Variant.VariantListBuilder createVariantListBuilder() {
    throw new RuntimeException("TBI");
  }

  @Override
  public <T> T createEndpoint(final Application application, final Class<T> aClass) throws IllegalArgumentException, UnsupportedOperationException {
    throw new RuntimeException("TBI");
  }

  @Override
  public <T> HeaderDelegate<T> createHeaderDelegate(final Class<T> aClass) throws IllegalArgumentException {
    throw new RuntimeException("TBI");
  }


  public static class ResponseBuilderImpl extends Response.ResponseBuilder {

    private int status;
    private Object entity;
    private Date lastModified;
    private String encoding;
    private MultivaluedMap<String, Object> metadata = new MultiValueMapImpl<>();
    private String language;
    private String mediaType;
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
      throw new RuntimeException("TBI");
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
      throw new RuntimeException("TBI");
    }

    @Override
    public Response.ResponseBuilder type(final MediaType mediaType) {
      return type(mediaType.getType());
    }

    @Override
    public Response.ResponseBuilder type(final String mediaType) {
      this.mediaType = mediaType;
      return this;
    }

    @Override
    public Response.ResponseBuilder variant(final Variant variant) {
      throw new RuntimeException("TBI");
    }

    @Override
    public Response.ResponseBuilder contentLocation(final URI uri) {
      this.uri = uri;
      return this;
    }

    @Override
    public Response.ResponseBuilder cookie(final NewCookie... newCookies) {
      throw new RuntimeException("TBI");
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
      throw new RuntimeException("TBI");
    }

    @Override
    public Response.ResponseBuilder tag(final EntityTag entityTag) {
      throw new RuntimeException("TBI");
    }

    @Override
    public Response.ResponseBuilder tag(final String s) {
      throw new RuntimeException("TBI");
    }


    @Override
    public Response.ResponseBuilder variants(final List<Variant> list) {
      throw new RuntimeException("TBI");
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

    public String getMediaType() {
      return mediaType;
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

}
