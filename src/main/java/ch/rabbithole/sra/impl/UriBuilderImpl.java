package ch.rabbithole.sra.impl;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

/**
 * TODO JavaDoc
 */
public final class UriBuilderImpl extends UriBuilder {

  private final int DEFAULT_PORT = 80;

  private String host;
  private int port = DEFAULT_PORT;
  private String sceme;
  private String path = "";
  private String query = "";


  @Override
  public UriBuilder clone() {
    throw new UnsupportedOperationException();
  }

  @Override
  public UriBuilder uri(URI uri) throws IllegalArgumentException {
    this.host = uri.getHost();
    this.port = uri.getPort() > -1 ? uri.getPort() : port;
    this.sceme = uri.getScheme();
    this.path = uri.getPath();
    return this;
  }

  @Override
  public UriBuilder scheme(String scheme) throws IllegalArgumentException {
    this.sceme = scheme;
    return this;
  }

  @Override
  public UriBuilder schemeSpecificPart(String ssp) throws IllegalArgumentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public UriBuilder userInfo(String ui) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UriBuilder host(String host) throws IllegalArgumentException {
    this.host = host;
    return this;
  }

  @Override
  public UriBuilder port(int port) throws IllegalArgumentException {
    this.port = port;
    return this;
  }

  @Override
  public UriBuilder replacePath(String path) {
    this.path = path;
    return this;
  }

  @Override
  public UriBuilder path(String path) throws IllegalArgumentException {
    return segment(path);
  }

  @Override
  public UriBuilder path(Class resource) throws IllegalArgumentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public UriBuilder path(Class resource, String method) throws IllegalArgumentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public UriBuilder path(Method method) throws IllegalArgumentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public UriBuilder segment(String... segments) throws IllegalArgumentException {
    final StringBuilder builder = new StringBuilder(this.path);
    for (String segment : segments) {
      if (segment.startsWith("/")) {
        builder.append(segment);
      } else {
        builder.append("/").append(segment);
      }

      int lastCharIndex = builder.length() - 1;
      if (builder.charAt(lastCharIndex) == '/') {
        builder.deleteCharAt(lastCharIndex);
      }
    }
    this.path = builder.toString();
    return this;
  }

  @Override
  public UriBuilder replaceMatrix(String matrix) throws IllegalArgumentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public UriBuilder matrixParam(String name, Object... values) throws IllegalArgumentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public UriBuilder replaceMatrixParam(String name, Object... values) throws IllegalArgumentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public UriBuilder replaceQuery(String query) throws IllegalArgumentException {
    this.query = query != null ? query : "";
    return this;
  }

  @Override
  public UriBuilder queryParam(String name, Object... values) throws IllegalArgumentException {
    final StringBuilder builder = new StringBuilder(query);
    buildQueryString(builder, name, values);
    return this;
  }

  private void buildQueryString(final StringBuilder builder, final String name, final Object[] values) {
    for (Object value : values) {

      if (builder.length() > 0) {
        builder.append('&');
      }

      builder.append(encode(name));
      if (value != null) {
        builder.append('=');
        builder.append(encode(value.toString()));
      }
    }
    this.query = builder.toString();
  }

  private String encode(final String value) {
    try {
      return URLEncoder.encode(value, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public UriBuilder replaceQueryParam(String name, Object... values) throws IllegalArgumentException {
    buildQueryString(new StringBuilder(), name, values);
    return this;
  }

  @Override
  public UriBuilder fragment(String fragment) {
    throw new UnsupportedOperationException();
  }

  @Override
  public URI buildFromMap(Map<String, ? extends Object> values) throws IllegalArgumentException, UriBuilderException {
    return build(values);
  }

  @Override
  public URI buildFromEncodedMap(Map<String, ? extends Object> values) throws IllegalArgumentException, UriBuilderException {
    return build(values);
  }

  @Override
  public URI build(Object... values) throws IllegalArgumentException, UriBuilderException {
    if (values != null && values.length > 0) {
      throw new IllegalArgumentException("URL template parameter featre not implemented");
    }
    final String portPart = port == DEFAULT_PORT ? "" : ":" + port;

    final String queryPart = query.isEmpty() ? "" : "?" + query;

    final String pathPart = path == null || path.isEmpty() ? "" : path;

    return URI.create(sceme + "://" + host + portPart + pathPart + queryPart);
  }

  @Override
  public URI buildFromEncoded(Object... values) throws IllegalArgumentException, UriBuilderException {
    return build(values);
  }
}
