package org.apache.cxf.common.util.UrlUtils;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * TODO JavaDoc
 */
public class UrlUtilsTest {

  @Test
  public void testParseEmptyQueryString() {
    final Map<String, String> map = UrlUtils.parseQueryString("");
    assertTrue(map.isEmpty());
  }

  @Test
  public void testParseNullValue() {
    final Map<String, String> map = UrlUtils.parseQueryString(null);
    assertTrue(map.isEmpty());  }
}