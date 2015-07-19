package ch.rabbithole.sra;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import static org.junit.Assert.assertEquals;
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
    assertEquals("\"something\"", out.getBuffer().toString());
  }

  @Test
  public void testConvertObjectToJson() throws NoSuchMethodException, IOException {
    ResourceExecution resource = createResourceExecution(getComplexMethod(), new ParameterMap());

    resource.execute(requestMock, responseMock);
    assertEquals("[\"1\",\"2\"]", out.getBuffer().toString());
  }

  @Test
  public void testCallWithResourceArguments() throws NoSuchMethodException {
    ParameterMap map = new ParameterMap();
    map.addParameter("id", "anId");
    ResourceExecution resource = createResourceExecution(getMethod("getWithParam", String.class), map);
    resource.execute(requestMock, responseMock);
    assertEquals("\"anId\"", out.getBuffer().toString());
  }

  @Test
  public void testQueryParam() throws NoSuchMethodException {
    ParameterMap map = new ParameterMap();
    ResourceExecution resource = createResourceExecution(getMethod("getWithParam2", String.class), map);
    Map<String, String> queryParamMap = new HashMap<>();
    queryParamMap.put("param1", "value1");
    queryParamMap.put("param2", "value2");
    when(requestMock.getParameterMap()).thenReturn(queryParamMap);

    resource.execute(requestMock, responseMock);
    assertEquals("\"value2\"", out.getBuffer().toString());
  }

  @Test
  public void testWithJsonQueryParam() throws NoSuchMethodException {
    ParameterMap map = new ParameterMap();
    ResourceExecution resource = createResourceExecution(getMethod("getWithParam3", QueryParamValue.class), map);
    Map<String, String> queryParamMap = new HashMap<>();
    queryParamMap.put("param1", "{'name': gandalf}");
    when(requestMock.getParameterMap()).thenReturn(queryParamMap);

    resource.execute(requestMock, responseMock);
    assertEquals("\"gandalf\"", out.getBuffer().toString());
  }

  @Test
  public void testQueryParamWithPrimitiveValueWithLong() throws NoSuchMethodException {
    ParameterMap map = new ParameterMap();
    ResourceExecution resource = createResourceExecution(getMethod("getWithParamWithLong", long.class), map);
    Map<String, String> queryParamMap = new HashMap<>();
    queryParamMap.put("param1", "100");
    when(requestMock.getParameterMap()).thenReturn(queryParamMap);

    resource.execute(requestMock, responseMock);
    assertEquals("\"100\"", out.getBuffer().toString());
  }

  @Test
  public void testReturnPrimitiveValueLong() throws NoSuchMethodException {
    ParameterMap map = new ParameterMap();
    ResourceExecution resource = createResourceExecution(getMethod("getLongValue"), map);

    resource.execute(requestMock, responseMock);
    assertEquals("200", out.getBuffer().toString());
  }

  private ResourceExecution createResourceExecution(final Method method, final ParameterMap map) {
    return new ResourceExecution(new Resource(method), new SimpleObjectObjectFactory(), map);
  }

  private Method getMethod(final String methodName, final Class...paramTypes) throws NoSuchMethodException {
    return ResourceClass.class.getMethod(methodName, paramTypes);
  }

  private Method getComplexMethod() throws NoSuchMethodException {
    return getMethod("getComplexSomething");
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
    public String getWithParam3(@QueryParam("param1") QueryParamValue value) {
      return value.name;
    }

    @GET
    public String getWithParamWithLong(@QueryParam("param1") long value) {
      return Long.toString(value);
    }

    @GET
    public long getLongValue() {
      return 200;
    }
  }

  public static class QueryParamValue {
    public String name;
  }

}
