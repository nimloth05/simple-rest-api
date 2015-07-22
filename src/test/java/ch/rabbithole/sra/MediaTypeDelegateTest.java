package ch.rabbithole.sra;

import com.sun.ws.rs.ext.MediaTypeDelegate;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

import static org.junit.Assert.assertEquals;

public final class MediaTypeDelegateTest {

  private MediaTypeDelegate delegate;

  @Before
  public void setUp() {
    delegate = new MediaTypeDelegate();
  }

  @Test
  public void testFromString() {
    MediaType mediaType = delegate.fromString(MediaType.APPLICATION_JSON);
    assertEquals(MediaType.APPLICATION_JSON_TYPE, mediaType);
  }

  @Test
  public void testToString() {
    String asString = delegate.toString(MediaType.APPLICATION_JSON_TYPE);
    assertEquals(MediaType.APPLICATION_JSON, asString);
  }

}
