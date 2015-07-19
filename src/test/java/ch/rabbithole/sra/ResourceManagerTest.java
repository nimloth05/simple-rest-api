package ch.rabbithole.sra;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

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
    ResourceExecution resource = resourceManager.getResource(parse, HttpVerb.GET);
    assertNotNull(resource);
  }

  @Test
  public void testMethodAssignment() {
    resourceManager.addResource(TestResource.class);

    ResourcePath parse = ResourcePath.parse("/a/b");
    ResourceExecution resource = resourceManager.getResource(parse, HttpVerb.GET);
    assertNotNull(resource);
    assertEquals("getFoo", resource.getMethodName());
  }

  @Test
  public void testMultipleResourcesWithSamePathButDifferentVerb() {
    resourceManager.addResource(TestResource.class);

    ResourcePath getPath = ResourcePath.parse("/a/b/");
    ResourceExecution getResource = resourceManager.getResource(getPath, HttpVerb.GET);
    assertNotNull(getResource);
    assertEquals("getFoo", getResource.getMethodName());

    ResourcePath putPath = ResourcePath.parse("/a/b/");
    ResourceExecution resource = resourceManager.getResource(putPath, HttpVerb.PUT);
    assertNotNull(resource);
    assertEquals("putFoo", resource.getMethodName());
  }

  @Test
  public void testWithSubResource() {
    resourceManager.addResource(TestResource.class);

    ResourcePath getPath = ResourcePath.parse("/a/b/c/d/");
    ResourceExecution getResource = resourceManager.getResource(getPath, HttpVerb.GET);
    assertNotNull(getResource);
    assertEquals("subResource", getResource.getMethodName());
  }

  @Test
  public void testResourceWithParamInPath() {
    resourceManager.addResource(TestResource.class);

    ResourcePath getPath = ResourcePath.parse("/a/b/X/");
    ResourceExecution getResource = resourceManager.getResource(getPath, HttpVerb.GET);
    assertNotNull(getResource);
    assertEquals("subResourceWithId", getResource.getMethodName());
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
    public String subResourceWithId(@PathParam("id") String id) {
      return "";
    }

  }

}