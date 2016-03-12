package ch.rabbithole.sra.resource.message;

import javax.ws.rs.core.MediaType;

public interface MessageBodyReaderWriterProvider {

  @SuppressWarnings("unchecked")
  <T> MessageBodyReaderWriter<T> get(MediaType mediaType);
}
