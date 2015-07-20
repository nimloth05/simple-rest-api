package ch.rabbithole.sra;

import com.google.gson.Gson;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;

public final class ResourceExecution {

  private final Resource resource;
  private final ObjectFactory objectFactory;
  private final ParameterMap parameterMap;

  public ResourceExecution(final Resource resource, final ObjectFactory objectFactory, final ParameterMap parameterMap) {
    this.resource = resource;
    this.objectFactory = objectFactory;
    this.parameterMap = parameterMap;
  }

  public void execute(final HttpServletRequest req, final HttpServletResponse resp) {
    Class<?> declaringClass = resource.getMethod().getDeclaringClass();
    Object instance = objectFactory.createInstance(declaringClass);

    Object[] params = buildMethodParams(req.getParameterMap());

    try {
      Object invoke = resource.getMethod().invoke(instance, params);
      String response = convertToJson(invoke);
      writeAnswer(resp, response);
    } catch (InvocationTargetException e) {
      handleError(resp, e.getCause());
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Method must be public: " + resource.getMethod());
    }
  }

  private void handleError(final HttpServletResponse resp, final Throwable e) {

    if (e instanceof  WebApplicationException) {
      WebApplicationException webE = (WebApplicationException) e;
      resp.setStatus(webE.getResponse().getStatus());
      writeAnswer(resp, webE.getMessage());
    } else {
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      writeAnswer(resp, e.getMessage());
    }
  }

  private void writeAnswer(final HttpServletResponse response, final String responseMessage) {
    try {
      response.getWriter().print(responseMessage);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Object[] buildMethodParams(final Map parameterMap) {
    Annotation[][] parameterAnnotations = resource.getMethod().getParameterAnnotations();
    Object[] result = new Object[parameterAnnotations.length];
    for (int i = 0; i < parameterAnnotations.length; i++) {
      Annotation[] annotations = parameterAnnotations[i];
      Class<?> aClass = resource.getMethod().getParameterTypes()[i];
      result[i] = getParameterValue(annotations, parameterMap, aClass);
    }
    return result;
  }

  private Object getParameterValue(final Annotation[] annotations, final Map requestParameterMap, final Class<?> parameterType) {
    for (Annotation annotation : annotations) {
      if (annotation.annotationType().equals(PathParam.class)) {
        PathParam pathParam = (PathParam) annotation;
        final String paramName = pathParam.value();
        return this.parameterMap.getValue(paramName);
      }
      if (annotation.annotationType().equals(QueryParam.class)) {
        QueryParam pathParam = (QueryParam) annotation;
        final String paramName = pathParam.value();
        return convertToObject(requestParameterMap.get(paramName).toString(), parameterType);
      }
    }
    return null;
  }

  private String convertToJson(final Object invoke) {
    Gson gson = new Gson();
    return gson.toJson(invoke);
  }

  private Object convertToObject(final String json, final Class type) {
    Gson gson = new Gson();
    return gson.fromJson(json, type);
  }


  public String getMethodName() {
    return resource.getMethodName();
  }
}
