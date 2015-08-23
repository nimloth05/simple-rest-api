package ch.rabbithole.sra;

import com.google.gson.Gson;

import com.sun.ws.rs.ext.MultiValueMapImpl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.rabbithole.sra.impl.UriInfoImpl;
import ch.rabbithole.sra.resource.ConstructorObjectFactory;
import ch.rabbithole.sra.resource.Resource;
import ch.rabbithole.sra.resource.ResourceExecution;
import ch.rabbithole.sra.resource.ResourcePath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.AdditionalMatchers.eq;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ResourceExecutionTest {

  private HttpServletRequest requestMock;
  private StubServletOutputStream out;
  private HttpServletResponse responseMock;

  @Before
  public void setUp() throws Exception {
    requestMock = Mockito.mock(HttpServletRequest.class);
    responseMock = Mockito.mock(HttpServletResponse.class);
    out = new StubServletOutputStream();
    when(responseMock.getOutputStream()).thenReturn(out);
  }

  @Test
  public void testResourceExecution() throws NoSuchMethodException, IOException {
    ResourceExecution resource = createResourceExecution(getMethod("getSomething"), createEmptyInfo());
    resource.execute();
    assertBufferContent("\"something\"");
  }

  @Test
  public void testConvertObjectToJson() throws NoSuchMethodException, IOException {
    ResourceExecution resource = createResourceExecution(getComplexMethod(), createEmptyInfo());
    resource.execute();
    assertBufferContent("[\"1\",\"2\"]");
  }

  @Test
  public void testCallWithPathParam() throws NoSuchMethodException {
    MultivaluedMap<String, String> map = new MultiValueMapImpl<>();
    map.putSingle("id", "anId");
    ResourceExecution resource = createResourceExecution(getMethod("getWithParam", String.class), createUrlInfoWithPathParams(map));
    resource.execute();
    assertBufferContent("anId");
  }

  @Test
  public void testQueryParam() throws NoSuchMethodException {
    MultiValueMapImpl<String, String> queryParamMap = new MultiValueMapImpl<>();
    queryParamMap.putSingle("param1", "value1");
    queryParamMap.putSingle("param2", "value2");

    ResourceExecution resource = createResourceExecution(getMethod("getWithParam2", String.class), createUrlInfoWithQueryParams(queryParamMap));
    resource.execute();
    assertBufferContent("value2");
  }

  @Test
  public void testWithJsonQueryParam() throws NoSuchMethodException {
    MultiValueMapImpl<String, String> queryParamMap = new MultiValueMapImpl<>();
    queryParamMap.putSingle("param1", "{'name': gandalf}");
    ResourceExecution resource = createResourceExecution(getMethod("getWithParam3", ParamValue.class), createUrlInfoWithQueryParams(queryParamMap));
    resource.execute();
    assertBufferContent("\"gandalf\"");
  }

  @Test
  public void testQueryParamWithPrimitiveValueWithLong() throws NoSuchMethodException {
    MultiValueMapImpl<String, String> queryParamMap = new MultiValueMapImpl<>();
    queryParamMap.putSingle("param1", "100");

    ResourceExecution resource = createResourceExecution(getMethod("getWithParamWithLong", long.class), createUrlInfoWithQueryParams(queryParamMap));
    resource.execute();
    assertBufferContent("\"100\"");
  }

  @Test
  public void testReturnPrimitiveValueLong() throws NoSuchMethodException {
    ResourceExecution resource = createResourceExecution(getMethod("getLongValue"), createEmptyInfo());
    resource.execute();
    assertBufferContent("200");
  }

  @Test
  public void testWebApplicationExceptionHandling() throws NoSuchMethodException, IOException {
    ResourceExecution resource = createResourceExecution(getMethod("getWebApplicationException"), createEmptyInfo());
    resource.execute();
    verify(responseMock).sendError(Matchers.eq(HttpServletResponse.SC_NOT_FOUND), (String) anyObject());
  }

  @Test
  public void testExceptionGeneratesAnInternalServerError() throws NoSuchMethodException, IOException {
    ResourceExecution resource = createResourceExecution(getMethod("getException"), createEmptyInfo());
    resource.execute();
    verify(responseMock).sendError(Matchers.eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), (String) anyObject());
  }

  @Test
  public void testGerResponseFromMethod() throws NoSuchMethodException, IOException {
    ResourceExecution resource = createResourceExecution(getMethod("getRedirectResponse"), createEmptyInfo());
    resource.execute();
    verify(responseMock).sendError(Matchers.eq(HttpServletResponse.SC_TEMPORARY_REDIRECT), (String) anyObject());
    verify(responseMock).setHeader(HttpHeaders.LOCATION, "http://www.example.com");
  }

  @Test
  public void testDontConvertStringPathParamToJson() throws NoSuchMethodException {
    MultivaluedMap<String, String> map = new MultiValueMapImpl<>();
    map.putSingle("value", "Hallo Welt");
    ResourceExecution resource = createResourceExecution(getMethod("pathParamAsString", String.class), createUrlInfoWithPathParams(map));
    resource.execute();
    assertBufferContent("\"Hallo Welt\"");
  }

  @Test
  public void testMediaTypeText() throws NoSuchMethodException {
    ResourceExecution resource = createResourceExecution(getMethod("getMediaTypeText"), createEmptyInfo());
    resource.execute();
    verify(responseMock).setStatus(HttpServletResponse.SC_OK);
    verify(responseMock).setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
    assertBufferContent("Hallo Welt");
  }

  @Test
  public void testGetWithContext() throws NoSuchMethodException {
    ResourceExecution resource = createResourceExecution(getMethod("getWithContext", HttpServletRequest.class), createEmptyInfo());
    resource.execute();
    assertBufferContent("true");
  }

  @Test
  public void testGetQueryParamWithDefault() throws NoSuchMethodException {
    ResourceExecution resource = createResourceExecution(getMethod("getQueryParamWithDefault", long.class), createEmptyInfo());
    resource.execute();
    assertBufferContent("1");
  }

  @Test
  public void testPutJsonParam() throws NoSuchMethodException {
    requestEntityBody(new ParamValue("gandalf"));

    //Resource method contains assertion
    ResourceExecution resource = createResourceExecution(getMethod("putJsonParam", ParamValue.class), createEmptyInfo());
    resource.execute();
    try {
      verify(requestMock).getReader();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testGetQueryParamWithString() throws NoSuchMethodException {
    MultiValueMapImpl<String, String> queryParamMap = new MultiValueMapImpl<>();
    queryParamMap.putSingle("q", "1");
    ResourceExecution resource = createResourceExecution(getMethod("getQueryParamWithString", String.class), createUrlInfoWithQueryParams(queryParamMap));
    resource.execute();
    assertBufferContent("1");
  }

  @Test
  public void testContextUriInfo() throws NoSuchMethodException {
    ResourceExecution resource = createResourceExecution(getMethod("getContextField"), createEmptyInfo());
    resource.execute();
    assertBufferContent("true");
  }

  @Test
  public void testPathParamAsLong() throws NoSuchMethodException {
    MultiValueMapImpl<String, String> params = new MultiValueMapImpl<>();
    params.putSingle("id", "1");
    ResourceExecution resource = createResourceExecution(getMethod("pathParamAsLong", long.class), createUrlInfoWithPathParams(params));
    resource.execute();
    assertBufferContent("1");
  }

  private ResourceExecution createResourceExecution(final Method method, final UriInfoImpl uriInfo) {
    return new ResourceExecution(new Resource(method), new ConstructorObjectFactory(), uriInfo, requestMock, responseMock);
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

  private UriInfoImpl createUrlInfoWithQueryParams(final MultivaluedMap<String, String> queryParams) {
    return new UriInfoImpl("",
                           ResourcePath.empty(),
                           ResourcePath.empty(),
                           ResourcePath.empty(),
                           queryParams,
                           new MultiValueMapImpl<String, String>());
  }

  private UriInfoImpl createUrlInfoWithPathParams(final MultivaluedMap<String, String> pathParams) {
    return new UriInfoImpl("",
                           ResourcePath.empty(),
                           ResourcePath.empty(),
                           ResourcePath.empty(),
                           new MultiValueMapImpl<String, String>(),
                           pathParams);
  }

  private UriInfoImpl createEmptyInfo() {
    return createUrlInfoWithQueryParams(new MultiValueMapImpl<>(Collections.<String, String>emptyMap()));
  }

  private void assertBufferContent(final String expected) {
    try {
      String bufferContent = new String(out.baos.toByteArray(), "UTF-8");
      assertEquals(expected, bufferContent);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  public static class ResourceClass {

    @Context
    public UriInfo uriInfo;

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
    @Produces("text/plain")
    public String getWithParam(@PathParam("id") final String id) {
      assertFalse(id.startsWith("\""));
      return id;
    }

    @GET
    @Produces("text/plain")
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

    @GET
    @Path("contextText")
    @Produces("text/plain")
    public boolean getContextField() {
      return uriInfo != null;
    }

    @GET
    @Path("pathParamAsLong/{id}")
    public long pathParamAsLong(@PathParam("id") long id) {
      return id;
    }

    @GET
    @Path("pathParamAsString/{value}")
    public String pathParamAsString(@PathParam("value") String value) {
      return value;
    }

  }

  public static class ParamValue {

    @SuppressWarnings("unused")
    public ParamValue() {

    }

    public ParamValue(final String name) {
      this.name = name;
    }

    public String name;
  }

}
