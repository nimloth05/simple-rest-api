package ch.rabbithole.sra;

import com.sun.ws.rs.ext.RuntimeDelegateImpl;

import javax.ws.rs.core.Response;

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
}
