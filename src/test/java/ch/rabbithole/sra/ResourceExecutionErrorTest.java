package ch.rabbithole.sra;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;

import ch.rabbithole.sra.resource.ResourceExecution;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ResourceExecutionErrorTest extends AbstractResourceTest {

  @Before
  public void setUp() throws Exception {
    requestMock = Mockito.mock(HttpServletRequest.class);
    responseMock = Mockito.mock(HttpServletResponse.class);
    out = new StubServletOutputStream();
    when(responseMock.getOutputStream()).thenReturn(out);
  }

  @Test
  public void testResourceMethodThrowsRuntimeException() throws NoSuchMethodException, IOException {
    ResourceExecution resource = createResourceExecution(getMethod(ResourceClass.class, "npe"), createEmptyInfo());
    resource.execute();
    verify(responseMock).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "java.lang.NullPointerException: Error message");
  }

  public static class ResourceClass {

    @Path("/npe")
    public void npe() {
      throw new NullPointerException("Error message");
    }
  }


}
