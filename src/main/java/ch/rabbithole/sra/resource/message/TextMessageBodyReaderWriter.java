package ch.rabbithole.sra.resource.message;

import com.google.common.io.CharStreams;
import com.google.common.primitives.Bytes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import ch.rabbithole.sra.resource.HeaderUtil;

/**
 * TODO JavaDoc
 */
public final class TextMessageBodyReaderWriter implements MessageBodyReaderWriter<Object> {

  @Override
  public boolean isReadable(final Class type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
    return MediaType.TEXT_PLAIN_TYPE.equals(mediaType);
  }

  @Override
  public boolean isWriteable(final Class type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
    return MediaType.TEXT_PLAIN_TYPE.equals(mediaType);
  }

  @Override
  public long getSize(final Object o, final Class type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
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
    writer.write(o.toString());
    writer.flush();
  }

  @Override
  public Object readFrom(final Class<Object> type,
                         final Type genericType,
                         final Annotation[] annotations,
                         final MediaType mediaType,
                         final MultivaluedMap<String, String> httpHeaders,
                         final InputStream entityStream) throws IOException, WebApplicationException {

    Charset charset = HeaderUtil.getCharset(mediaType);
    final InputStreamReader reader = new InputStreamReader(entityStream, charset);
    return CharStreams.toString(reader);
  }
}
