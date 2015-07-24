package ch.rabbithole.sra;

import org.junit.Test;

import ch.rabbithole.sra.resource.ResourcePath;

import static org.junit.Assert.*;

public final class ResourcePathTest {

  @Test
  public void testParseMethod() {
    ResourcePath path = ResourcePath.parse("a");
    assertEquals("a", path.getPart());
  }

  @Test
  public void testParseMethodWithMultipleParts() {
    ResourcePath path = ResourcePath.parse("/a/b");
    assertEquals("a", path.getPart());
    final ResourcePath subPath = path.getSubPath();
    assertEquals("b", subPath.getPart());
  }

  @Test
  public void testPathWithWildcardParts() {
    ResourcePath path = ResourcePath.parse("/a/{resourceId}");
    assertEquals("a", path.getPart());
    final ResourcePath subPath = path.getSubPath();
    assertEquals("{resourceId}", subPath.getPart());
  }

  @Test
  public void testAddSubPath() {
    ResourcePath path = ResourcePath.parse("/a");
    ResourcePath newPath = path.addSubPath(ResourcePath.parse("b"));
    assertEquals(newPath.getPart(), "a");
    assertEquals(newPath.getSubPath().getPart(), "b");
  }

  @Test
  public void testStringWithTrailingSlash() {
    ResourcePath path = ResourcePath.parse("/a/");
    assertEquals("a", path.getPart());
    assertNull(path.getSubPath());
  }

  @Test
  public void testAddSubPathWithMultiplePaths() {
    ResourcePath path = ResourcePath.parse("/a/b");
    ResourcePath newPath = path.addSubPath(ResourcePath.parse("c"));
    assertEquals(newPath.getPart(), "a");
    assertEquals(newPath.getSubPath().getPart(), "b");
    assertEquals(newPath.getSubPath().getSubPath().getPart(), "c");
  }

  @Test
  public void testToString() {
    ResourcePath path = ResourcePath.parse("a/b");
    assertEquals("a/b", path.toString());
  }

}