package ch.rabbithole.sra.resource.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import ch.rabbithole.sra.ObjectUtil;
import ch.rabbithole.sra.resource.HeaderUtil;

/**
 * Implementation for converting objects from and to json.
 */
public final class JsonMessageBodyReaderWriter implements MessageBodyReaderWriter<Object> {

  @Override
  public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
    return MediaType.APPLICATION_JSON_TYPE.equals(mediaType);
  }

  @Override
  public Object readFrom(final Class<Object> type,
                         final Type genericType,
                         final Annotation[] annotations,
                         final MediaType mediaType,
                         final MultivaluedMap<String, String> httpHeaders,
                         final InputStream entityStream) throws IOException, WebApplicationException {
    Charset charset = HeaderUtil.getCharset(mediaType);
    return ObjectUtil.fromJson(new InputStreamReader(entityStream, charset), type);
  }

  @Override
  public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
    return MediaType.APPLICATION_JSON_TYPE.equals(mediaType);
  }

  @Override
  public long getSize(final Object o, final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(final Object o,
                      final Class<?> type,
                      final Type genericType,
                      final Annotation[] annotations,
                      final MediaType mediaType,
                      final MultivaluedMap<String, Object> httpHeaders,
                      final OutputStream entityStream) throws IOException, WebApplicationException {

    Charset charset = HeaderUtil.getCharset(mediaType);
    httpHeaders.remove(HttpHeaders.CONTENT_TYPE);
    httpHeaders.putSingle(HttpHeaders.CONTENT_TYPE, HeaderUtil.createMediaTypeWithEncoding(mediaType, charset));
    final OutputStreamWriter writer = new OutputStreamWriter(entityStream, charset);
    ObjectUtil.toJson(o, writer);
    writer.flush();
  }
}
