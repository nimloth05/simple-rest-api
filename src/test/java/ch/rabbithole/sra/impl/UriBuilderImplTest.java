package ch.rabbithole.sra.impl;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public class UriBuilderImplTest {

  @Test
  public void testAppendPath() {
    UriBuilderImpl builder = new UriBuilderImpl();
    final URI expected = URI.create("http://example.com");
    final URI actual = builder.uri(expected).build();
    assertEquals(expected, actual);
  }

  @Test
  public void testUriBuildByComponents() {
    UriBuilderImpl impl = new UriBuilderImpl();

    impl.host("example.com");
    impl.scheme("http");

    final URI actual = impl.build();
    assertEquals(URI.create("http://example.com"), actual);
  }

  @Test
  public void testWithPath() {
    UriBuilderImpl impl = new UriBuilderImpl();

    impl
        .host("example.com")
        .scheme("http")
        .path("a");

    assertEquals(URI.create("http://example.com/a"), impl.build());
  }

  @Test
  public void testAppendMultiplePaths() {
    UriBuilderImpl impl = new UriBuilderImpl();

    impl
        .scheme("http")
        .host("example.com")
        .path("/a")
        .path("/b/")
        .path("c");
    assertEquals(URI.create("http://example.com/a/b/c"), impl.build());
  }

  @Test
  public void testBuildQueryParam() {
    UriBuilderImpl impl = new UriBuilderImpl();

    impl
        .scheme("http")
        .host("example.com")
        .path("/a")
        .queryParam("k1", "v1");
    assertEquals(URI.create("http://example.com/a?k1=v1"), impl.build());
  }

  @Test
  public void testBuildWithMultipleQueryParams() {
    UriBuilderImpl impl = new UriBuilderImpl();

    impl
        .scheme("http")
        .host("example.com")
        .path("/a")
        .queryParam("k1", "v1", "v2")
        .queryParam("k2", "v3");
    assertEquals(URI.create("http://example.com/a?k1=v1&k1=v2&k2=v3"), impl.build());
  }

  @Test
  public void testReplaceQueryParam() {
    UriBuilderImpl impl = new UriBuilderImpl();

    impl
        .uri(URI.create("http://example.com/a/b?k1=v1"))
        .replaceQueryParam("k2", "v2");
    assertEquals(URI.create("http://example.com/a/b?k2=v2"), impl.build());
  }

  @Test
  public void testWithHttps() {
    UriBuilderImpl impl = new UriBuilderImpl();

    impl
        .scheme("https")
        .host("example.com");
    assertEquals(URI.create("https://example.com"), impl.build());
  }

  @Test
  public void testHttpsWithDefaultPort() {
    UriBuilderImpl impl = new UriBuilderImpl();

    impl
        .scheme("https")
        .port(443)
        .host("example.com");
    assertEquals(URI.create("https://example.com:443"), impl.build());
  }


}