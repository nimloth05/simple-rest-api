package ch.rabbithole.sra;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.invocation.finder.VerifiableInvocationsFinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.rabbithole.sra.resource.ConstructorObjectFactory;
import ch.rabbithole.sra.resource.ParameterMap;
import ch.rabbithole.sra.resource.Resource;
import ch.rabbithole.sra.resource.ResourceExecution;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ResourceExecutionTest {

  private HttpServletRequest requestMock;
  private StringWriter out;
  private HttpServletResponse responseMock;

  @Before
  public void setUp() throws Exception {
    requestMock = Mockito.mock(HttpServletRequest.class);
    out = new StringWriter();
    PrintWriter writer = new PrintWriter(out);
    responseMock = Mockito.mock(HttpServletResponse.class);
    when(responseMock.getWriter()).thenReturn(writer);


  }

  @Test
  public void testResourceExecution() throws NoSuchMethodException, IOException {
    ResourceExecution resource = createResourceExecution(getMethod("getSomething"), new ParameterMap());
    resource.execute(requestMock, responseMock);
    assertBufferContent("\"something\"");
  }

  @Test
  public void testConvertObjectToJson() throws NoSuchMethodException, IOException {
    ResourceExecution resource = createResourceExecution(getComplexMethod(), new ParameterMap());

    resource.execute(requestMock, responseMock);
    assertBufferContent("[\"1\",\"2\"]");
  }

  @Test
  public void testCallWithResourceArguments() throws NoSuchMethodException {
    ParameterMap map = new ParameterMap();
    map.addParameter("id", "anId");
    ResourceExecution resource = createResourceExecution(getMethod("getWithParam", String.class), map);
    resource.execute(requestMock, responseMock);
    assertBufferContent("\"anId\"");
  }

  @Test
  public void testQueryParam() throws NoSuchMethodException {
    ParameterMap map = new ParameterMap();
    ResourceExecution resource = createResourceExecution(getMethod("getWithParam2", String.class), map);
    Map<String, String[]> queryParamMap = new HashMap<>();
    queryParamMap.put("param1", new String[] {"value1"});
    queryParamMap.put("param2", new String[] {"value2"});
    when(requestMock.getParameterMap()).thenReturn(queryParamMap);

    resource.execute(requestMock, responseMock);
    assertBufferContent("\"value2\"");
  }

  @Test
  public void testWithJsonQueryParam() throws NoSuchMethodException {
    ParameterMap map = new ParameterMap();
    ResourceExecution resource = createResourceExecution(getMethod("getWithParam3", ParamValue.class), map);
    Map<String, String[]> queryParamMap = new HashMap<>();
    queryParamMap.put("param1", new String[] {"{'name': gandalf}"});
    when(requestMock.getParameterMap()).thenReturn(queryParamMap);

    resource.execute(requestMock, responseMock);
    assertBufferContent("\"gandalf\"");
  }

  @Test
  public void testQueryParamWithPrimitiveValueWithLong() throws NoSuchMethodException {
    ParameterMap map = new ParameterMap();
    ResourceExecution resource = createResourceExecution(getMethod("getWithParamWithLong", long.class), map);
    Map<String, String[]> queryParamMap = new HashMap<>();
    queryParamMap.put("param1", new String[] {"100"});
    when(requestMock.getParameterMap()).thenReturn(queryParamMap);

    resource.execute(requestMock, responseMock);
    assertBufferContent("\"100\"");
  }

  @Test
  public void testReturnPrimitiveValueLong() throws NoSuchMethodException {
    ParameterMap map = new ParameterMap();
    ResourceExecution resource = createResourceExecution(getMethod("getLongValue"), map);

    resource.execute(requestMock, responseMock);
    assertBufferContent("200");
  }

  private void assertBufferContent(final String expected) {
    assertEquals(expected, out.getBuffer().toString());
  }

  @Test
  public void testWebApplicationExceptionHandling() throws NoSuchMethodException {
    ResourceExecution resource = createResourceExecution(getMethod("getWebApplicationException"), new ParameterMap());

    resource.execute(requestMock, responseMock);
    verify(responseMock).setStatus(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  public void testExceptionGeneratesAnInternalServerError() throws NoSuchMethodException {
    ResourceExecution resource = createResourceExecution(getMethod("getException"), new ParameterMap());

    resource.execute(requestMock, responseMock);
    verify(responseMock).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
  }

  @Test
  public void testGerResponseFromMethod() throws NoSuchMethodException {
    ResourceExecution resource = createResourceExecution(getMethod("getRedirectResponse"), new ParameterMap());

    resource.execute(requestMock, responseMock);
    verify(responseMock).setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
    verify(responseMock).setHeader(HttpHeaders.LOCATION, "http://www.example.com");
  }

  @Test
  public void testMediaTypeText() throws NoSuchMethodException {
    ResourceExecution resource = createResourceExecution(getMethod("getMediaTypeText"), new ParameterMap());

    resource.execute(requestMock, responseMock);
    verify(responseMock).setStatus(HttpServletResponse.SC_OK);
    verify(responseMock).setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
    assertBufferContent("Hallo Welt");
  }

  @Test
  public void testGetWithContext() throws NoSuchMethodException {
    ResourceExecution resource = createResourceExecution(getMethod("getWithContext", HttpServletRequest.class), new ParameterMap());

    resource.execute(requestMock, responseMock);
    assertBufferContent("true");
  }

  @Test
  public void testGetQueryParamWithDefault() throws NoSuchMethodException {
    ResourceExecution resource = createResourceExecution(getMethod("getQueryParamWithDefault", long.class), new ParameterMap());

    resource.execute(requestMock, responseMock);
    assertBufferContent("1");
  }

  @Test
  public void testPutJsonParam() throws NoSuchMethodException {
    ResourceExecution resource = createResourceExecution(getMethod("putJsonParam", ParamValue.class), new ParameterMap());
    requestEntityBody(new ParamValue("gandalf"));

    //Resource method contains assertion
    resource.execute(requestMock, responseMock);
    try {
      verify(requestMock).getReader();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testGetQueryParamWithString() throws NoSuchMethodException {
    ResourceExecution resource = createResourceExecution(getMethod("getQueryParamWithString", String.class), new ParameterMap());

    Map<String, String[]> queryParamMap = new HashMap<>();
    queryParamMap.put("q", new String[] {"1"});
    when(requestMock.getParameterMap()).thenReturn(queryParamMap);

    resource.execute(requestMock, responseMock);
    assertBufferContent("1");
  }


  private ResourceExecution createResourceExecution(final Method method, final ParameterMap map) {
    return new ResourceExecution(new Resource(method), new ConstructorObjectFactory(), map);
  }

  private Method getMethod(final String methodName, final Class... paramTypes) throws NoSuchMethodException {
    return ResourceClass.class.getMethod(methodName, paramTypes);
  }

  private Method getComplexMethod() throws NoSuchMethodException {
    return getMethod("getComplexSomething");
  }

  private void requestEntityBody(final Object object) {
    Gson gson = new Gson();
    String jsonString = gson.toJson(object);
    try {
      when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(jsonString)));
    } catch (Exception e) {
      //how can a mock crash?
    }
  }

  public static class ResourceClass {

    @GET
    public String getSomething() {
      return "something";
    }

    @GET
    public List<String> getComplexSomething() {
      return Arrays.asList("1", "2");
    }

    @GET
    @Path("{id}")
    public String getWithParam(@PathParam("id") final String id) {
      return id;
    }

    @GET
    public String getWithParam2(@QueryParam("param2") String value) {
      return value;
    }

    @GET
    public String getWithParam3(@QueryParam("param1") ParamValue value) {
      return value.name;
    }

    @GET
    public String getWithParamWithLong(@QueryParam("param1") long value) {
      return Long.toString(value);
    }

    @GET
    @Path("queryParamWithDefault")
    @Produces("text/plain")
    public String getQueryParamWithDefault(@QueryParam("param1") @DefaultValue("1") long value) {
      return Long.toString(value);
    }

    @GET
    public long getLongValue() {
      return 200;
    }

    @GET
    public long getWebApplicationException() {
      throw new WebApplicationException(HttpServletResponse.SC_NOT_FOUND);
    }

    @GET
    public long getException() {
      throw new RuntimeException("Error");
    }

    @GET
    public Response getRedirectResponse() throws URISyntaxException {
      return Response.temporaryRedirect(new URI("http://www.example.com")).build();
    }

    @GET
    @Produces("text/plain")
    public String getMediaTypeText() throws URISyntaxException {
      return "Hallo Welt";
    }

    @GET
    @Path("getContext")
    public boolean getWithContext(@Context HttpServletRequest request) {
      return request != null;
    }

    @GET
    @Path("getQueryWithString")
    @Produces("text/plain")
    public String getQueryParamWithString(@QueryParam("q") String value) {
      return value;
    }

    @PUT
    @Path("putJsonParam")
    public void putJsonParam(ParamValue param) {
      assertEquals("gandalf", param.name);
    }

  }

  public static class ParamValue {

    public ParamValue() {

    }

    public ParamValue(final String name) {
      this.name = name;
    }

    public String name;
  }

}
