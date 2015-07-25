package ch.rabbithole.sra.impl;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

/**
 * TODO JavaDoc
 */
public final class UriBuilderImpl extends UriBuilder{

  @Override
  public UriBuilder clone() {
    throw new UnsupportedOperationException();
  }

  @Override
  public UriBuilder uri(URI uri) throws IllegalArgumentException {
    return null;
  }

  @Override
  public UriBuilder scheme(String scheme) throws IllegalArgumentException {
    return null;
  }

  @Override
  public UriBuilder schemeSpecificPart(String ssp) throws IllegalArgumentException {
    return null;
  }

  @Override
  public UriBuilder userInfo(String ui) {
    return null;
  }

  @Override
  public UriBuilder host(String host) throws IllegalArgumentException {
    return null;
  }

  @Override
  public UriBuilder port(int port) throws IllegalArgumentException {
    return null;
  }

  @Override
  public UriBuilder replacePath(String path) {
    return null;
  }

  @Override
  public UriBuilder path(String path) throws IllegalArgumentException {
    return null;
  }

  @Override
  public UriBuilder path(Class resource) throws IllegalArgumentException {
    return null;
  }

  @Override
  public UriBuilder path(Class resource, String method) throws IllegalArgumentException {
    return null;
  }

  @Override
  public UriBuilder path(Method method) throws IllegalArgumentException {
    return null;
  }

  @Override
  public UriBuilder segment(String... segments) throws IllegalArgumentException {
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
    return null;
  }

  @Override
  public URI buildFromEncodedMap(Map<String, ? extends Object> values) throws IllegalArgumentException, UriBuilderException {
    return null;
  }

  @Override
  public URI build(Object... values) throws IllegalArgumentException, UriBuilderException {
    return null;
  }

  @Override
  public URI buildFromEncoded(Object... values) throws IllegalArgumentException, UriBuilderException {
    return null;
  }
}
