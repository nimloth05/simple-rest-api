package ch.rabbithole.sra;

import com.google.gson.Gson;

import com.sun.ws.rs.ext.MultiValueMapImpl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
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
import ch.rabbithole.sra.resource.Client;
import ch.rabbithole.sra.resource.ResourceExecution;
import ch.rabbithole.sra.resource.ResourcePath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ResourceExecutionTest extends AbstractResourceTest {

  @Before
  public void setUp() throws Exception {
    requestMock = Mockito.mock(HttpServletRequest.class);
    responseMock = Mockito.mock(HttpServletResponse.class);
    out = new StubServletOutputStream();
    when(responseMock.getOutputStream()).thenReturn(out);
  }

  @Test
  public void testResourceExecution() throws NoSuchMethodException, IOException {
    ResourceExecution resource = createResourceExecution(getMethod(ResourceClass.class, "getSomething"), createEmptyInfo());
    resource.execute();
    assertBufferContent("\"something\"");
  }

  @Test
  public void testConvertResultListToJson() throws NoSuchMethodException, IOException {
    ResourceExecution resource = createResourceExecution(getComplexMethod(), createEmptyInfo());
    resource.execute();
    assertBufferContent("[\"1\",\"2\"]");
  }

  @Test
  public void testCallWithPathParam() throws NoSuchMethodException {
    MultivaluedMap<String, String> map = new MultiValueMapImpl<>();
    map.putSingle("id", "anId");
    ResourceExecution resource = createResourceExecution(getMethod(ResourceClass.class, "getWithParam", String.class), createUrlInfoWithPathParams(map));
    resource.execute();
    assertBufferContent("anId");
  }

  @Test
  public void testQueryParam() throws NoSuchMethodException {
    MultiValueMapImpl<String, String> queryParamMap = new MultiValueMapImpl<>();
    queryParamMap.putSingle("param1", "value1");
    queryParamMap.putSingle("param2", "value2");

    ResourceExecution resource = createResourceExecution(getMethod(ResourceClass.class, "getWithParam2", String.class), createUrlInfoWithQueryParams(queryParamMap));
    resource.execute();
    assertBufferContent("value2");
  }

  @Test
  public void testWithJsonQueryParam() throws NoSuchMethodException {
    MultiValueMapImpl<String, String> queryParamMap = new MultiValueMapImpl<>();
    queryParamMap.putSingle("param1", "{'name': gandalf}");
    ResourceExecution resource = createResourceExecution(getMethod(ResourceClass.class, "getWithParam3", ParamValue.class), createUrlInfoWithQueryParams(queryParamMap));
    resource.execute();
    assertBufferContent("\"gandalf\"");
  }

  @Test
  public void testQueryParamWithPrimitiveValueWithLong() throws NoSuchMethodException {
    MultiValueMapImpl<String, String> queryParamMap = new MultiValueMapImpl<>();
    queryParamMap.putSingle("param1", "100");

    ResourceExecution resource = createResourceExecution(getMethod(ResourceClass.class, "getWithParamWithLong", long.class), createUrlInfoWithQueryParams(queryParamMap));
    resource.execute();
    assertBufferContent("\"100\"");
  }

  @Test
  public void testReturnPrimitiveValueLong() throws NoSuchMethodException {
    ResourceExecution resource = createResourceExecution(getMethod(ResourceClass.class, "getLongValue"), createEmptyInfo());
    resource.execute();
    assertBufferContent("200");
  }

  @Test
  public void testWebApplicationExceptionHandling() throws NoSuchMethodException, IOException {
    ResourceExecution resource = createResourceExecution(getMethod(ResourceClass.class, "getWebApplicationException"), createEmptyInfo());
    resource.execute();
    assertBufferContent("");
    assertStatusCode(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  public void testExceptionGeneratesAnInternalServerError() throws NoSuchMethodException, IOException {
    ResourceExecution resource = createResourceExecution(getMethod(ResourceClass.class, "getException"), createEmptyInfo());
    resource.execute();
    assertBufferContent("java.lang.RuntimeException: Error");
    assertStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
  }

  @Test
  public void testGerResponseFromMethod() throws NoSuchMethodException, IOException {
    ResourceExecution resource = createResourceExecution(getMethod(ResourceClass.class, "getRedirectResponse"), createEmptyInfo());
    resource.execute();
    verify(responseMock).setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
    verify(responseMock).setHeader(HttpHeaders.LOCATION, "http://www.example.com");
  }

  @Test
  public void testDontConvertStringPathParamToJson() throws NoSuchMethodException {
    MultivaluedMap<String, String> map = new MultiValueMapImpl<>();
    map.putSingle("value", "Hallo Welt");
    ResourceExecution resource = createResourceExecution(getMethod(ResourceClass.class, "pathParamAsString", String.class), createUrlInfoWithPathParams(map));
    resource.execute();
    assertBufferContent("\"Hallo Welt\"");
  }

  @Test
  public void testMediaTypeText() throws NoSuchMethodException {
    ResourceExecution resource = createResourceExecution(getMethod(ResourceClass.class, "getMediaTypeText"), createEmptyInfo());
    resource.execute();
    verify(responseMock).setStatus(HttpServletResponse.SC_OK);
    verify(responseMock).setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
    assertBufferContent("Hallo Welt");
  }

  @Test
  public void testGetWithContext() throws NoSuchMethodException {
    ResourceExecution resource = createResourceExecution(getMethod(ResourceClass.class, "getWithContext", HttpServletRequest.class), createEmptyInfo());
    resource.execute();
    assertBufferContent("true");
  }

  @Test
  public void testGetQueryParamWithDefault() throws NoSuchMethodException {
    ResourceExecution resource = createResourceExecution(getMethod(ResourceClass.class, "getQueryParamWithDefault", long.class), createEmptyInfo());
    resource.execute();
    assertBufferContent("1");
  }

  @Test
  public void testFormPassing() throws NoSuchMethodException {
    requestTextEntityBody("A=1&B=2");
    ResourceExecution resource = createResourceExecution(getMethod(ResourceClass.class, "formUrlEncoded", Map.class), createEmptyInfo());
    resource.execute();
    assertBufferContent("A=1&B=2");
  }

  @Test
  public void testPutJsonParam() throws NoSuchMethodException {
    requestJsonEntityBody(new ParamValue("gandalf"));

    //Resource method contains assertion
    ResourceExecution resource = createResourceExecution(getMethod(ResourceClass.class, "putJsonParam", ParamValue.class), createEmptyInfo());
    resource.execute();
  }

  @Test
  public void testGetQueryParamWithString() throws NoSuchMethodException {
    MultiValueMapImpl<String, String> queryParamMap = new MultiValueMapImpl<>();
    queryParamMap.putSingle("q", "1");
    ResourceExecution resource = createResourceExecution(getMethod(ResourceClass.class, "getQueryParamWithString", String.class), createUrlInfoWithQueryParams(queryParamMap));
    resource.execute();
    assertBufferContent("1");
  }

  @Test
  public void testConsumesTextPlain() throws NoSuchMethodException {
    requestTextEntityBody("gandalf");

    ResourceExecution resource = createResourceExecution(getMethod(ResourceClass.class, "consumeStringAsTextPlain", String.class), createEmptyInfo());
    resource.execute();
    assertBufferContent("gandalf");
  }

  @Test
  public void testContextUriInfo() throws NoSuchMethodException {
    ResourceExecution resource = createResourceExecution(getMethod(ResourceClass.class, "getContextField"), createEmptyInfo());
    resource.execute();
    assertBufferContent("true");
  }

  @Test
  public void testClientIsInjected() throws NoSuchMethodException {
    ResourceExecution resource = createResourceExecution(getMethod(ResourceClass.class, "clientIsInjected"), createEmptyInfo());
    resource.execute();
    assertBufferContent("true");
  }

  @Test
  public void testPathParamAsLong() throws NoSuchMethodException {
    MultiValueMapImpl<String, String> params = new MultiValueMapImpl<>();
    params.putSingle("id", "1");
    ResourceExecution resource = createResourceExecution(getMethod(ResourceClass.class, "pathParamAsLong", long.class), createUrlInfoWithPathParams(params));
    resource.execute();
    assertBufferContent("1");
  }

  private Method getComplexMethod() throws NoSuchMethodException {
    return getMethod(ResourceClass.class, "getComplexSomething");
  }

  private void requestJsonEntityBody(final Object object) {
    Gson gson = new Gson();
    String jsonString = gson.toJson(object);
    try {
//      when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(jsonString)));
      when(requestMock.getInputStream()).thenReturn(ServletInputStreamStub.create(jsonString));
    } catch (Exception e) {
      //how can a mock crash?
    }
  }

  private void requestTextEntityBody(final Object object) {
    try {
//      when(requestMock.getReader()).thenReturn(new BufferedReader(new StringReader(jsonString)));
      when(requestMock.getInputStream()).thenReturn(ServletInputStreamStub.create(object.toString()));
    } catch (Exception e) {
      //how can a mock crash?
    }
  }

  private UriInfoImpl createUrlInfoWithPathParams(final MultivaluedMap<String, String> pathParams) {
    return new UriInfoImpl("",
                           ResourcePath.empty(),
                           ResourcePath.empty(),
                           ResourcePath.empty(),
                           new MultiValueMapImpl<String, String>(),
                           pathParams);
  }

  @SuppressWarnings("WeakerAccess")
  public static class ResourceClass {

    @Context
    public UriInfo uriInfo;

    @Context
    public Client client;

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
    @Consumes("text/plain")
    public String consumeStringAsTextPlain(final String text) {
      return text;
    }

    @GET
    @Produces("application/x-www-form-urlencoded")
    @Consumes("application/x-www-form-urlencoded")
    public Map<String, String> formUrlEncoded(final Map<String, String> map) {
      assertEquals("1", map.get("A"));
      assertEquals("2", map.get("B"));
      return map;
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

    @GET
    @Path("clientIsInjected")
    @Produces("text/plain")
    public String clientIsInjected() {
      assertNotNull(client);
      return "true";
    }
  }


  @SuppressWarnings("WeakerAccess")
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
