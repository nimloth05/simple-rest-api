package ch.rabbithole.sra.resource;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

public final class HeaderUtil {

  public static final Charset UTF8 = Charset.forName("UTF-8");

  private HeaderUtil() {
  }


  /**
   * Returns the charset of the media type. If no charset is defined <tt>UTF-8</tt> is returned
   * @param m media type with possible charset declaration
   * @return charset or <tt>UTF-8</tt>
   */
  public static Charset getCharset(MediaType m) {
    String name = m == null ? null : m.getParameters().get("charset");
    return name == null ? UTF8 : Charset.forName(name);
  }

  public static MediaType createMediaTypeWithEncoding(final MediaType mediaType, final Charset charset) {
    Map<String, String> params = new HashMap<>();
    params.put("charset", charset.name());
    return new MediaType(mediaType.getType(), mediaType.getSubtype(), params);
  }

}
