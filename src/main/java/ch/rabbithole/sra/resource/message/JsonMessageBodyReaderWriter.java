package ch.rabbithole.sra.resource.message;

import ch.rabbithole.sra.HeaderUtil;
import ch.rabbithole.sra.ObjectUtil;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

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
