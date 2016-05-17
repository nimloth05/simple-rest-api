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

  public static void toJson(final Object object, final Appendable writer) {
    GSON.toJson(object, writer);
  }

  public static Object fromJson(final String value, final Class<?> type) {
    return GSON.fromJson(value, type);
  }

  public static Object fromJson(final Reader reader, final Class<?> type) {
    return GSON.fromJson(reader, type);
  }

}
