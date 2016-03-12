package ch.rabbithole.sra.resource.message;

import com.google.common.io.CharStreams;

import com.sun.ws.rs.ext.MultiValueMapImpl;

import org.apache.cxf.common.util.UrlUtils.UrlUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import ch.rabbithole.sra.resource.HeaderUtil;

/**
 * Encodes the given multi value map as form url encoded.
 */
public final class FormParamMessageBodyReaderWriter implements MessageBodyReaderWriter<Map<String, String>> {

  @Override
  public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
    return MediaType.APPLICATION_FORM_URLENCODED_TYPE.equals(mediaType);
  }

  @Override
  public Map<String, String> readFrom(final Class<Map<String, String>> type,
                                                 final Type genericType,
                                                 final Annotation[] annotations,
                                                 final MediaType mediaType,
                                                 final MultivaluedMap<String, String> httpHeaders,
                                                 final InputStream entityStream) throws IOException, WebApplicationException {


    Charset charset = HeaderUtil.getCharset(mediaType);
    final String s = CharStreams.toString(new InputStreamReader(entityStream, charset));
    return UrlUtils.parseQueryString(s);
  }

  @Override
  public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
    return MediaType.APPLICATION_FORM_URLENCODED_TYPE.equals(mediaType);
  }

  @Override
  public long getSize(final Map<String, String> values,
                      final Class<?> type,
                      final Type genericType,
                      final Annotation[] annotations,
                      final MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(final Map<String, String> values,
                      final Class<?> type,
                      final Type genericType,
                      final Annotation[] annotations,
                      final MediaType mediaType,
                      final MultivaluedMap<String, Object> httpHeaders,
                      final OutputStream entityStream) throws IOException, WebApplicationException {

    Charset charset = HeaderUtil.getCharset(mediaType);
    HeaderUtil.replaceContentTypeWithEncoding(mediaType, httpHeaders, charset);

    final String s = UrlUtils.toQueryString(toMVM(values));
    final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(entityStream, charset));
    writer.append(s);
    writer.close();
  }

  @SuppressWarnings("unchecked")
  private MultivaluedMap<String, String> toMVM(final Map<String, String> map) {
    if (map instanceof MultiValueMapImpl) {
      return (MultivaluedMap) map;
    }
    MultiValueMapImpl<String, String> multiValueMap = new MultiValueMapImpl<>();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      multiValueMap.putSingle(entry.getKey(), entry.getValue());
    }
    return multiValueMap;
  }

}

