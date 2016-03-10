package ch.rabbithole.sra.impl;

import com.sun.ws.rs.ext.MultiValueMapImpl;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
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
  private String path;

  private MultivaluedMap<String, String> queryParams = new MultiValueMapImpl<>();

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
    }
    return null;
  }

  @Override
  public UriBuilder replaceMatrix(String matrix) throws IllegalArgumentException {
    return null;
  }

  @Override
  public UriBuilder matrixParam(String name, Object... values) throws IllegalArgumentException {
    return null;
  }

  @Override
  public UriBuilder replaceMatrixParam(String name, Object... values) throws IllegalArgumentException {
    return null;
  }

  @Override
  public UriBuilder replaceQuery(String query) throws IllegalArgumentException {
    return null;
  }

  @Override
  public UriBuilder queryParam(String name, Object... values) throws IllegalArgumentException {
    return null;
  }

  @Override
  public UriBuilder replaceQueryParam(String name, Object... values) throws IllegalArgumentException {
    return null;
  }

  @Override
  public UriBuilder fragment(String fragment) {
    return null;
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
    if (values != null) {
      throw new IllegalArgumentException("URL template parameter featre not implemented");
    }
    final String portPart = port == DEFAULT_PORT ? "" : ":" + port;

    final String queryString = buildQueryString();
    final String queryPart = queryString.isEmpty() ? "" : "?" + queryString;

    final String pathPart = path.isEmpty() ? "" : "/" + path;

    return URI.create(sceme + "://" + host + portPart + pathPart + queryPart);
  }

  private String buildQueryString() {
    return "";
  }

  @Override
  public URI buildFromEncoded(Object... values) throws IllegalArgumentException, UriBuilderException {
    return build(values);
  }
}
