package ch.rabbithole.sra;

import java.lang.reflect.Method;

/**
 * TODO JavaDoc
 */
public enum HttpVerb {
  GET,
  PUT;

  public static HttpVerb getVerb(final Method resourceMethod) {
    if (resourceMethod.getAnnotation(javax.ws.rs.GET.class) != null) {
      return HttpVerb.GET;
    }
    if (resourceMethod.getAnnotation(javax.ws.rs.PUT.class) != null) {
      return HttpVerb.PUT;
    }
    return null;
  }
}
