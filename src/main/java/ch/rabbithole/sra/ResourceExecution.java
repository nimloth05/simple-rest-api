package ch.rabbithole.sra;

import com.google.gson.Gson;

import sun.invoke.anon.AnonymousClassLoader;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

public final class ResourceExecution {

  private final Resource resource;
  private final Instantiator instantiator;
  private final ParameterMap parameterMap;

  public ResourceExecution(final Resource resource, final Instantiator instantiator, final ParameterMap parameterMap) {
    this.resource = resource;
    this.instantiator = instantiator;
    this.parameterMap = parameterMap;
  }

  public void execute(final HttpServletRequest req, final HttpServletResponse resp) {
    Class<?> declaringClass = resource.getMethod().getDeclaringClass();
    Object instance = instantiator.createInstance(declaringClass);

    Object[] params = buildMethodParams(req.getParameterMap());

    try {
      Object invoke = resource.getMethod().invoke(instance, params);
      String response = convertToJson(invoke);
      resp.getWriter().print(response);
    } catch (Exception e) {

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
