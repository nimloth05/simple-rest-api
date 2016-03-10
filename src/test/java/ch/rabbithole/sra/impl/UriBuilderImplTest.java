package ch.rabbithole.sra.impl;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public class UriBuilderImplTest {

  @Test
  public void testAppendPath() {
    UriBuilderImpl builder = new UriBuilderImpl();
    final URI expected = URI.create("http://example.com");
    final URI actual = builder.uri(expected).build(null);
    assertEquals(expected, actual);
  }



}