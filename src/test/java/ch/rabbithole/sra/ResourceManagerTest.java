package ch.rabbithole.sra;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import ch.rabbithole.sra.resource.ConstructorObjectFactory;
import ch.rabbithole.sra.resource.ResourceExecution;
import ch.rabbithole.sra.resource.ResourcePath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public final class ResourceManagerTest {

  private ResourceManager resourceManager;

  @Before
  public void setUp() throws Exception {
    resourceManager = new ResourceManager();
  }

  @Test
  public void testsAddResource() {
    resourceManager.addResource(TestResource.class);
    ResourcePath parse = ResourcePath.parse("/a/b/");
    ResourceExecution resource = resourceManager.getResource(parse, HttpVerb.GET, new ConstructorObjectFactory());
    assertNotNull(resource);
  }

  @Test
  public void testMethodAssignment() {
    resourceManager.addResource(TestResource.class);

    ResourcePath parse = ResourcePath.parse("/a/b");
    ResourceExecution resource = resourceManager.getResource(parse, HttpVerb.GET, new ConstructorObjectFactory());
    assertNotNull(resource);
    assertEquals("getFoo", resource.getMethodName());
  }

  @Test
  public void testMultipleResourcesWithSamePathButDifferentVerb() {
    resourceManager.addResource(TestResource.class);

    ResourcePath getPath = ResourcePath.parse("/a/b/");
    ResourceExecution getResource = resourceManager.getResource(getPath, HttpVerb.GET, new ConstructorObjectFactory());
    assertNotNull(getResource);
    assertEquals("getFoo", getResource.getMethodName());

    ResourcePath putPath = ResourcePath.parse("/a/b/");
    ResourceExecution resource = resourceManager.getResource(putPath, HttpVerb.PUT, new ConstructorObjectFactory());
    assertNotNull(resource);
    assertEquals("putFoo", resource.getMethodName());
  }

  @Test
  public void testWithSubResource() {
    resourceManager.addResource(TestResource.class);

    ResourcePath getPath = ResourcePath.parse("/a/b/c/d/");
    ResourceExecution getResource = resourceManager.getResource(getPath, HttpVerb.GET, new ConstructorObjectFactory());
    assertNotNull(getResource);
    assertEquals("subResource", getResource.getMethodName());
  }

  @Test
  public void testResourceWithParamInPath() {
    resourceManager.addResource(TestResource.class);

    ResourcePath getPath = ResourcePath.parse("/a/b/X/");
    ResourceExecution getResource = resourceManager.getResource(getPath, HttpVerb.GET, new ConstructorObjectFactory());
    assertNotNull(getResource);
    assertEquals("subGetResourceWithId", getResource.getMethodName());
  }

  @Test
  public void testPathParamWithMultipleVerbs() {
    resourceManager.addResource(TestResource.class);

    ResourcePath getPath = ResourcePath.parse("/a/b/1");
    ResourceExecution getResource = resourceManager.getResource(getPath, HttpVerb.PUT, new ConstructorObjectFactory());
    assertNotNull(getResource);
    assertEquals("subPutResourceWithId", getResource.getMethodName());
  }

  @Test
  public void testConsecutivePathIds() {
    resourceManager.addResource(TestResource.class);

    ResourcePath getPath = ResourcePath.parse("/a/b/subIds/1/2");
    ResourceExecution getResource = resourceManager.getResource(getPath, HttpVerb.GET, new ConstructorObjectFactory());
    assertNotNull(getResource);
    assertEquals("subPathWithMultipleIds", getResource.getMethodName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNotMatchingResource() {
    resourceManager.addResource(TestResource.class);

    ResourcePath getPath = ResourcePath.parse("/a/b/fail/path/");
     resourceManager.getResource(getPath, HttpVerb.GET, new ConstructorObjectFactory());
  }

  @Path("/a/b")
  private static class TestResource {

    @GET
    public String getFoo() {
      return "";
    }

    @PUT
    public String putFoo() {
      return "";
    }

    @GET
    @Path("/c/d")
    public String subResource() {
      return "";
    }

    @GET
    @Path("{id}")
    public String subGetResourceWithId(@PathParam("id") String id) {
      return "";
    }

    @PUT
    @Path("{id}")
    public void subPutResourceWithId(@PathParam("id") String id) {
      assertEquals("1", id);
    }

    @GET
    @Path("/subIds/{id}/{id2}")
    public String subPathWithMultipleIds(@PathParam("id") String id1) {
      return id1;
    }

  }

}