package ch.rabbithole.sra.resource;

import java.util.HashMap;
import java.util.Map;

/**
 * A class containing parameter values form a path.
 */
public final class ParameterMap {

  private final Map<String, String> paramName2Value = new HashMap<>();

  public void addParameter(final String key, final String value) {
    paramName2Value.put(key, value);
  }

  public String getValue(final String key) {
    return paramName2Value.get(key);
  }

}
