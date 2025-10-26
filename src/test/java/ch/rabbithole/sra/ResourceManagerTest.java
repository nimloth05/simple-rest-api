package ch.rabbithole.sra;

import org.junit.Before;
import org.junit.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;

import ch.rabbithole.sra.resource.ResourcePath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
    ResourceExecutionBuilder resource = resourceManager.getResource(parse, HttpVerb.GET);
    assertNotNull(resource);
  }

  @Test
  public void testMethodAssignment() {
    resourceManager.addResource(TestResource.class);

    ResourcePath parse = ResourcePath.parse("/a/b");
    ResourceExecutionBuilder resource = resourceManager.getResource(parse, HttpVerb.GET);
    assertNotNull(resource);
    assertEquals("getFoo", resource.getMethodName());
  }

  @Test
  public void testMultipleResourcesWithSamePathButDifferentVerb() {
    resourceManager.addResource(TestResource.class);

    ResourcePath getPath = ResourcePath.parse("/a/b/");
    ResourceExecutionBuilder getResource = resourceManager.getResource(getPath, HttpVerb.GET);
    assertNotNull(getResource);
    assertEquals("getFoo", getResource.getMethodName());

    ResourcePath putPath = ResourcePath.parse("/a/b/");
    ResourceExecutionBuilder resource = resourceManager.getResource(putPath, HttpVerb.PUT);
    assertNotNull(resource);
    assertEquals("putFoo", resource.getMethodName());
  }

  @Test
  public void testWithSubResource() {
    resourceManager.addResource(TestResource.class);

    ResourcePath getPath = ResourcePath.parse("/a/b/c/d/");
    ResourceExecutionBuilder getResource = resourceManager.getResource(getPath, HttpVerb.GET);
    assertNotNull(getResource);
    assertEquals("subResource", getResource.getMethodName());
  }

  @Test
  public void testResourceWithParamInPath() {
    resourceManager.addResource(TestResource.class);

    ResourcePath getPath = ResourcePath.parse("/a/b/X/");
    ResourceExecutionBuilder getResource = resourceManager.getResource(getPath, HttpVerb.GET);
    assertNotNull(getResource);
    assertEquals("subGetResourceWithId", getResource.getMethodName());
  }

  @Test
  public void testPathParamWithMultipleVerbs() {
    resourceManager.addResource(TestResource.class);

    ResourcePath getPath = ResourcePath.parse("/a/b/1");
    ResourceExecutionBuilder getResource = resourceManager.getResource(getPath, HttpVerb.PUT);
    assertNotNull(getResource);
    assertEquals("subPutResourceWithId", getResource.getMethodName());
  }

  @Test
  public void testConsecutivePathIds() {
    resourceManager.addResource(TestResource.class);

    ResourcePath getPath = ResourcePath.parse("/a/b/subIds/1/2");
    ResourceExecutionBuilder getResource = resourceManager.getResource(getPath, HttpVerb.GET);
    assertNotNull(getResource);
    assertEquals("subPathWithMultipleIds", getResource.getMethodName());
  }

  @Test(expected = WebApplicationException.class)
  public void testNotMatchingResource() {
    resourceManager.addResource(TestResource.class);

    ResourcePath getPath = ResourcePath.parse("/a/b/fail/path/");
     resourceManager.getResource(getPath, HttpVerb.GET);
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

    /*
    * All tests will fail if the ResourceManager has an error processing these
     */
    public void setRandomStuff() {

    }

    public String getRandomStuff() {
      return "";
    }

  }

}
