package ch.rabbithole.sra;

import com.sun.ws.rs.ext.MultiValueMapImpl;

import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

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

  public static void replaceContentTypeWithEncoding(final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders, final Charset charset) {
    httpHeaders.remove(HttpHeaders.CONTENT_TYPE);
    httpHeaders.putSingle(HttpHeaders.CONTENT_TYPE, createMediaTypeWithEncoding(mediaType, charset));
  }

  @SuppressWarnings("unchecked")
  public static MultivaluedMap<String, String> toMap(final HttpServletRequest request) {
    MultiValueMapImpl<String, String> result = new MultiValueMapImpl<>();

    final Enumeration<String> headerNames = request.getHeaderNames();
    if (headerNames == null) {
      return result;
    }


    while (headerNames.hasMoreElements()) {
      final String name = headerNames.nextElement();
      final String header = request.getHeader(name);
      result.putSingle(name, header);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  public static MultivaluedMap<String, String> toMap(final HttpURLConnection connection) {
    MultiValueMapImpl<String, String> result = new MultiValueMapImpl<>();

    final Map<String, List<String>> headerFields = connection.getHeaderFields();
    for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  public static String toCommaList(final List<String> value) {
    StringBuilder builder = new StringBuilder();
    for (String s : value) {
      if (builder.length() > 0) {
        builder.append(",");
      }
      builder.append(s);
    }
    return builder.toString();
  }
}
