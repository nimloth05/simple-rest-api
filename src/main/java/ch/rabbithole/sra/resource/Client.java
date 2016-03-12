package ch.rabbithole.sra.resource;

import com.google.common.io.CharStreams;

import com.sun.ws.rs.ext.MultiValueMapImpl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.rabbithole.sra.HeaderUtil;
import ch.rabbithole.sra.resource.message.MessageBodyReaderWriter;
import ch.rabbithole.sra.resource.message.MessageBodyReaderWriterProvider;

public final class Client {

  private URI uri;
  private MediaType requestType = MediaType.APPLICATION_JSON_TYPE;
  private MediaType responseType = MediaType.APPLICATION_JSON_TYPE;
  private final MessageBodyReaderWriterProvider provider;
  private Object entity = null;
  private Class acceptJavaType;

  public Client(final MessageBodyReaderWriterProvider provider) {
    this.provider = provider;
  }

  public Client type(final MediaType type) {
    this.requestType = type;
    return this;
  }

  public Client accept(final MediaType type) {
    this.responseType = type;
    return this;
  }

  public Client acceptJavaType(final Class acceptJavaType) {
    this.acceptJavaType = acceptJavaType;
    return this;
  }

  public Client entity(final Object entity) {
    this.entity = entity;
    return this;
  }

  public Client target(final URI uri) {
    this.uri = uri;
    return this;
  }

  public Response get() {
    try {
      return sendGetRequest();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  private Response sendGetRequest() throws IOException {
    URL url = uri.toURL();
    final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
    urlConnection.setRequestProperty(HttpHeaders.CONTENT_TYPE, requestType.toString());

    if(entity != null) {
      urlConnection.setDoOutput(true);
      final OutputStream outputStream = urlConnection.getOutputStream();
      final MessageBodyReaderWriter<Object> writer = provider.get(requestType);
      writer.writeTo(entity, entity.getClass(), null, new Annotation[0], requestType, new MultiValueMapImpl<String, Object>(), outputStream);
      outputStream.flush();
    }

    urlConnection.setDoInput(true);

    Response.ResponseBuilder responseBuilder = Response.created(uri);
    final int responseCode = urlConnection.getResponseCode();
    responseBuilder
        .status(responseCode);

    if (responseCode >= 200 && responseCode <= 299) {

      if (acceptJavaType == null) {
        throw new IllegalStateException("Java accept type not set");
      }

      final MessageBodyReaderWriter<Object> writer = provider.get(responseType);
      final Object o = writer.readFrom(acceptJavaType,
                                       null,
                                       new Annotation[0],
                                       responseType,
                                       HeaderUtil.toMap(urlConnection),
                                       urlConnection.getInputStream());
      responseBuilder
          .entity(o)
          .type(MediaType.valueOf(urlConnection.getContentType()));
    } else {
      final String response = CharStreams.toString(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
      responseBuilder
          .entity(response)
          .status(responseCode)
          .type(MediaType.TEXT_PLAIN);
    }

    return responseBuilder.build();
  }

}
