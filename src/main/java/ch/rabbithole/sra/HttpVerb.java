package ch.rabbithole.sra;

import java.lang.reflect.Method;

/**
 * Verb of the REST call.
 */
public enum HttpVerb {
  GET,
  PUT,
  POST,
  DELETE;

  public static HttpVerb getVerb(final Method resourceMethod) {
    if (resourceMethod.getAnnotation(javax.ws.rs.GET.class) != null) {
      return HttpVerb.GET;
    }
    if (resourceMethod.getAnnotation(javax.ws.rs.PUT.class) != null) {
      return HttpVerb.PUT;
    }
    if (resourceMethod.getAnnotation(javax.ws.rs.POST.class) != null) {
      return HttpVerb.POST;
    }

    if (resourceMethod.getAnnotation(javax.ws.rs.DELETE.class) != null) {
      return HttpVerb.DELETE;
    }
    return null;
  }
}
