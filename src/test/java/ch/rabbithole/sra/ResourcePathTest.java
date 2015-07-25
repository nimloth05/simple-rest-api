package ch.rabbithole.sra;

import org.junit.Test;

import ch.rabbithole.sra.resource.Resource;
import ch.rabbithole.sra.resource.ResourcePath;

import static org.junit.Assert.*;

public final class ResourcePathTest {

  @Test
  public void testParseMethod() {
    ResourcePath path = ResourcePath.parse("a");
    assertEquals("a", path.getPathSegments().get(0).getPath());
  }

  @Test
  public void testParseWithParam() {
    ResourcePath path = ResourcePath.parse("{a}");
    assertEquals("{a}", path.getPathSegments().get(0).getPath());
  }

  @Test
  public void testParseMethodWithMultipleParts() {
    ResourcePath path = ResourcePath.parse("/a/b");
    assertEquals("a", path.getPathSegments().get(0).getPath());
    assertEquals("b", path.getPathSegments().get(1).getPath());
  }

  @Test
  public void testPathWithWildcardParts() {
    ResourcePath path = ResourcePath.parse("/a/{resourceId}");
    assertEquals(2, path.getPathSegments().size());
    assertEquals("a", path.getPathSegments().get(0).getPath());
    assertEquals("{resourceId}", path.getPathSegments().get(1).getPath());
  }

  @Test
  public void testAddSubPath() {
    ResourcePath path = ResourcePath.parse("/a");
    ResourcePath newPath = path.addSubPath(ResourcePath.parse("b"));
    assertEquals("a", newPath.getPathSegments().get(0).getPath());
    assertEquals("b", newPath.getPathSegments().get(1).getPath());
  }

  @Test
  public void testStringWithTrailingSlash() {
    ResourcePath path = ResourcePath.parse("/a/");
    assertEquals("a", path.getPathSegments().get(0).getPath());
    assertEquals(1, path.getPathSegments().size());
  }

  @Test
  public void testAddSubPathWithMultiplePaths() {
    ResourcePath path = ResourcePath.parse("/a/b");
    ResourcePath newPath = path.addSubPath(ResourcePath.parse("c"));
    assertEquals(newPath.getPathSegments().get(0).getPath(), "a");
    assertEquals(newPath.getPathSegments().get(1).getPath(), "b");
    assertEquals(newPath.getPathSegments().get(2).getPath(), "c");
  }

  @Test
  public void testToString() {
    ResourcePath path = ResourcePath.parse("a/b");
    assertEquals("a/b", path.toString());
  }

  @Test
  public void testAddEmptyPath() {
    ResourcePath path = ResourcePath.parse("/a/b/");
    final ResourcePath newPath = path.addSubPath(ResourcePath.parse(""));
    assertEquals(2, newPath.getPathSegments().size());
  }

}