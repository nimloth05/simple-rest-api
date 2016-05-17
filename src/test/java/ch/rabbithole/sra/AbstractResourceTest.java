package ch.rabbithole.sra;

import com.sun.ws.rs.ext.MultiValueMapImpl;

import org.mockito.Mockito;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedMap;

import ch.rabbithole.sra.impl.UriInfoImpl;
import ch.rabbithole.sra.resource.ConstructorObjectFactory;
import ch.rabbithole.sra.resource.Resource;
import ch.rabbithole.sra.resource.ResourceExecution;
import ch.rabbithole.sra.resource.ResourcePath;
import ch.rabbithole.sra.resource.message.MessageBodyReaderWriterRegistry;

import static org.junit.Assert.assertEquals;

/**
 * TODO JavaDoc
 */
abstract class AbstractResourceTest {

  HttpServletRequest requestMock;
  HttpServletResponse responseMock;
  StubServletOutputStream out;

  ResourceExecution createResourceExecution(final Method method, final UriInfoImpl uriInfo) {
    return new ResourceExecution(MessageBodyReaderWriterRegistry.createWithDefaults(), new ConstructorObjectFactory(), new Resource(method), uriInfo, requestMock, responseMock);
  }

  Method getMethod(final Class<?> resourceClassClass,
                   final String methodName,
                   final Class... paramTypes) throws NoSuchMethodException {
    return resourceClassClass.getMethod(methodName, paramTypes);
  }

  void assertBufferContent(final String expected) {
    try {
      String bufferContent = new String(out.baos.toByteArray(), "UTF-8");
      assertEquals(expected, bufferContent);
      Mockito.verify(responseMock).setContentLength(out.baos.toByteArray().length);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  void assertStatusCode(final int statusCode) {
    Mockito.verify(responseMock).setStatus(statusCode);
  }

  UriInfoImpl createUrlInfoWithQueryParams(final MultivaluedMap<String, String> queryParams) {
    return new UriInfoImpl("",
                           ResourcePath.empty(),
                           ResourcePath.empty(),
                           ResourcePath.empty(),
                           queryParams,
                           new MultiValueMapImpl<String, String>());
  }

  UriInfoImpl createEmptyInfo() {
    return createUrlInfoWithQueryParams(new MultiValueMapImpl<>(Collections.<String, String>emptyMap()));
  }
}
