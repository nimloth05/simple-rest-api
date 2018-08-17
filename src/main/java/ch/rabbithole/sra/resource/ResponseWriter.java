package ch.rabbithole.sra.resource;

import ch.rabbithole.sra.CountingOutputStream;
import ch.rabbithole.sra.resource.message.MessageBodyReaderWriter;
import ch.rabbithole.sra.resource.message.MessageBodyReaderWriterProvider;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.sun.ws.rs.ext.ResponseImpl;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ch.rabbithole.sra.ResponseUtil.createTextPlainContentTypeMultiMap;
import static ch.rabbithole.sra.ResponseUtil.writeToHttpResponse;

public final class ResponseWriter {

  private static final Logger log = Logger.getLogger(ResponseWriter.class.getName());

  private final MessageBodyReaderWriterProvider provider;

  public ResponseWriter(final MessageBodyReaderWriterProvider provider) {
    this.provider = provider;
  }

  public void writeResponse(final HttpServletResponse resp,
                            final Response response,
                            final UriInfo uriInfo,
                            final Annotation[] annotations) {
    try {
      MultivaluedMap<String, Object> metadata = response.getMetadata();

      final Object entity = response.getEntity();
      CountingOutputStream cos;
      if (response.getStatus() >= 200 && response.getStatus() <= 399) {
        try {
          cos = writeEntity(entity, metadata, provider, annotations);
          resp.setStatus(response.getStatus());

          Object contentType = metadata.getFirst(HttpHeaders.CONTENT_TYPE);
          if (contentType != null) {
            resp.setContentType(contentType.toString());
          }
        } catch (WebApplicationException e) {
          log.log(Level.SEVERE, "Error during response processing", e);
          log.severe("Error during writing response: " + e);
          resp.setStatus(e.getResponse().getStatus());
          cos = writeEntity(e.getResponse().getEntity().toString(), createTextPlainContentTypeMultiMap(), provider, annotations);
        }
      } else {
        resp.setStatus(response.getStatus());
        cos = writeEntity(entity != null ? entity.toString() : "", createTextPlainContentTypeMultiMap(), provider, annotations);
      }

      if (metadata != null) {
        for (String headerKey : metadata.keySet()) {
          List<Object> values = metadata.get(headerKey);
          if (headerKey.equals(HttpHeaders.LOCATION)) {
            values = toAbsoluteUrls(values, uriInfo);
          }
          final String headerString = ResponseImpl.getHeaderString(ResponseImpl.toListOfStrings(values));
          resp.setHeader(headerKey, headerString);
        }
      }
      writeToHttpResponse(resp, cos);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public List<Object> toAbsoluteUrls(final List<Object> values, final UriInfo uriInfo) {
    return Lists.transform(values, new Function<Object, Object>() {
      @Override
      public Object apply(Object o) {
        final URI uri = URI.create(o.toString());
        if (uri.isAbsolute()) {
          return o;
        }
        return uriInfo.getBaseUri().resolve(uri).toString();
      }
    });
  }

  public CountingOutputStream writeEntity(final Object entity, final MultivaluedMap<String, Object> metadata, final MessageBodyReaderWriterProvider provider, final Annotation[] annotations) throws IOException {
    MediaType contentType = MediaType.APPLICATION_JSON_TYPE;
    if (metadata.containsKey(HttpHeaders.CONTENT_TYPE)) {
      contentType = MediaType.valueOf(metadata.getFirst(HttpHeaders.CONTENT_TYPE).toString());
    }

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    CountingOutputStream cos = new CountingOutputStream(bos);
    if (entity != null) {
      final MessageBodyReaderWriter<Object> writer = provider.get(contentType);
      writer.writeTo(entity, entity.getClass(), null, annotations, contentType, metadata, cos);
    }

    return cos;
  }
}
