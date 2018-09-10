package ch.rabbithole.sra;

import com.sun.ws.rs.ext.MultiValueMapImpl;
import com.sun.ws.rs.ext.RuntimeDelegateImpl;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Util for Responses.
 */
public final class ResponseUtil {

  public static Response notFound(final String message) {
    return RuntimeDelegateImpl.getInstance().createResponseBuilder()
        .status(Response.Status.NOT_FOUND.getStatusCode())
        .entity(message)
        .build();
  }

  public static Response.ResponseBuilder clientError() {
    return RuntimeDelegateImpl.getInstance().createResponseBuilder()
        .status(Response.Status.BAD_REQUEST.getStatusCode());
  }

  public static void writeToHttpResponse(final HttpServletResponse resp, final CountingOutputStream cos) throws IOException {
    resp.setContentLength((int) cos.getTransferred());
    OutputStream os = resp.getOutputStream();
    os.write(((ByteArrayOutputStream) cos.getUnderlyingStream()).toByteArray());
    os.close();
  }

  public static MultivaluedMap<String, Object> createTextPlainContentTypeMultiMap() {
    MultiValueMapImpl<String, Object> result = new MultiValueMapImpl<>();
    result.putSingle(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
    return result;
  }

  public static Response fromError(final Throwable e) {
    WebApplicationException responseException = findCause(e);
    if (responseException != null) {
      return responseException.getResponse();
    }
    return Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(e.toString())
            .build();
  }

  @SuppressWarnings("ConstantConditions")
  private static WebApplicationException findCause(final Throwable e) {
    Throwable current = e;
    while (!(current instanceof WebApplicationException)) {
      if (current.getCause() == null) {
        break;
      }
      current = current.getCause();
    }
    return current instanceof WebApplicationException ? ((WebApplicationException) current) : null;
  }
}
