package ch.rabbithole.sra.impl;

import com.sun.ws.rs.ext.MultiValueMapImpl;

import org.apache.cxf.common.util.UrlUtils.UrlUtils;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import ch.rabbithole.sra.resource.Resource;
import ch.rabbithole.sra.resource.ResourcePath;

/**
 * Impl for {@link UriInfo}
 */
public final class UriInfoImpl implements UriInfo {

  private final ResourcePath basePath;
  private final MultivaluedMap<String, String> queryParams;
  private final MultivaluedMap<String, String> pathParams;
  private final ResourcePath requestPath;
  private String domainPart;
  private ResourcePath resourcePath;
  private Resource matchedResource;

  public UriInfoImpl(String domainPart, ResourcePath requestPath,
                     ResourcePath resourcePath,
                     ResourcePath basePath,
                     MultivaluedMap<String, String> queryParams,
                     MultivaluedMap<String, String> pathParams) {
    this.domainPart = domainPart;

    this.requestPath = requestPath;
    this.basePath = basePath;
    this.resourcePath = resourcePath;

    this.queryParams = queryParams;
    this.pathParams = pathParams;
  }

  public static UriInfoImpl create(final String domainPart,
                                   final String requestUri,
                                   final String pathInfo,
                                   final String queryString,
                                   MultivaluedMap<String, String> pathParams) {
    final ResourcePath requestPath = ResourcePath.parse(requestUri);
    final ResourcePath resourcePath = ResourcePath.parse(pathInfo);
    final ResourcePath basePath = requestPath.disjoint(resourcePath);

    final MultivaluedMap<String, String> queryParams = new MultiValueMapImpl<>(UrlUtils.parseQueryString(queryString));

    return new UriInfoImpl(domainPart, requestPath, resourcePath, basePath, queryParams, pathParams);
  }

  public void setMatchedResource(final Resource matchedResource) {
    this.matchedResource = matchedResource;
  }

  @Override
  public String getPath() {
    return getPath(true);
  }

  @Override
  public String getPath(boolean decode) {
    String value = resourcePath.toString();
    return value.length() > 1 && value.startsWith("/") ?
           value.substring(1) :
           value;
  }

  @Override
  public List<PathSegment> getPathSegments() {
    return getPathSegments(true);
  }

  @Override
  public List<PathSegment> getPathSegments(boolean decode) {
    return resourcePath.getPathSegments();
  }

  @Override
  public URI getRequestUri() {
    String path = requestPath.toString();
    String queries = UrlUtils.toQueryString(queryParams);
    if (queries != null) {
      path += "?" + queries;
    }
    return URI.create(path);
  }

  @Override
  public UriBuilder getRequestUriBuilder() {
    throw new UnsupportedOperationException();
  }

  @Override
  public URI getAbsolutePath() {
    return URI.create(domainPart + "/" + requestPath.toString());
  }

  @Override
  public UriBuilder getAbsolutePathBuilder() {
    throw new UnsupportedOperationException();
  }

  @Override
  public URI getBaseUri() {
    return URI.create(domainPart + "/" + basePath.toString());
  }

  @Override
  public UriBuilder getBaseUriBuilder() {
    throw new UnsupportedOperationException();
  }

  @Override
  public MultivaluedMap<String, String> getPathParameters() {
    return getPathParameters(true);
  }

  @Override
  @NotNull
  public MultivaluedMap<String, String> getPathParameters(boolean decode) {
    MultivaluedMap<String, String> values = new MultiValueMapImpl<>();
    if (pathParams.isEmpty()) {
      return values;
    }

    //don't copy if same values are returned.
    if (!decode) {
      return pathParams;
    }

    for (Map.Entry<String, List<String>> entry : pathParams.entrySet()) {
      values.add(entry.getKey(), UrlUtils.pathDecode(entry.getValue().get(0)));
    }

    return values;
  }

  @Override
  public MultivaluedMap<String, String> getQueryParameters() {
    return getQueryParameters(true);
  }

  @Override
  @NotNull
  public MultivaluedMap<String, String> getQueryParameters(boolean decode) {

    MultivaluedMap<String, String> values = new MultiValueMapImpl<>();
    if (queryParams.isEmpty()) {
      return values;
    }

    if (!decode) {
      return queryParams;
    }

    for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
      values.add(entry.getKey(), UrlUtils.urlDecode(entry.getValue().get(0)));
    }

    return values;
  }

  @Override
  public List<String> getMatchedURIs() {
    return getMatchedURIs(true);
  }

  @Override
  public List<String> getMatchedURIs(boolean decode) {
    return Collections.singletonList(resourcePath.toString());
  }

  @Override
  public List<Object> getMatchedResources() {
    return Collections.<Object>singletonList(matchedResource);
  }
}
