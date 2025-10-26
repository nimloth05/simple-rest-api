package ch.rabbithole.sra.impl;

import ch.rabbithole.sra.resource.ResourcePath;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

public class UriInfoImplTest {

  private UriInfoImpl uriInfo;

  @Before
  public void setUp() {
    uriInfo = new UriInfoImpl(
            "http://rabbithole.ch",
            ResourcePath.parse("/a/b/resource"),
            ResourcePath.parse("/resource"),
            ResourcePath.parse("/a/b"),
            new MultivaluedHashMap<>(),
            new MultivaluedHashMap<>()
    );
  }

  @Test
  public void testResolve_relative() throws URISyntaxException {
    // act
    final var result = uriInfo.resolve(new URI("x"));

    // assert
    assertEquals("http://rabbithole.ch/a/b/x", result.toString());
  }

  @Test
  public void testResolve_absolutePath() throws URISyntaxException {
    // act
    final var result = uriInfo.resolve(new URI("/x"));

    // assert
    assertEquals("http://rabbithole.ch/x", result.toString());
  }
}
