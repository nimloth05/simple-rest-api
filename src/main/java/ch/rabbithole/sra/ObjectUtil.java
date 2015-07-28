package ch.rabbithole.sra;

import com.google.gson.Gson;

import java.io.Reader;

/**
 * Util for converting strings to object and vice versa.
 */
public final class ObjectUtil {

  private ObjectUtil(){}

  private static final Gson GSON = new Gson();

  public static String toJson(final Object object) {
    return GSON.toJson(object);
  }

  public static Object fromJson(final String value, final Class<?> type) {
    return GSON.fromJson(value, type);
  }

  public static Object fromJson(final Reader reader, final Class<?> type) {
    return GSON.fromJson(reader, type);
  }

  public static Object getJavaDefault(Class<?> parameterType) {
    if (!parameterType.isPrimitive()) {
      return null;
    }

    if (parameterType.equals(long.class)) {
      return 0;
    }
    if (parameterType.equals(byte.class)) {
      return 0;
    }
    if (parameterType.equals(char.class)) {
      return 0;
    }
    if (parameterType.equals(short.class)) {
      return 0;
    }
    if (parameterType.equals(boolean.class)) {
      return 0;
    }
    if (parameterType.equals(int.class)) {
      return 0;
    }
    if (parameterType.equals(float.class)) {
      return 0.0f;
    }
    if (parameterType.equals(double.class)) {
      return 0.0f;
    }

    return null;
  }

}
