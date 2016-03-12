package ch.rabbithole.sra.resource.message;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

/**
 * Registry for different message body writer/reader.
 */
public final class MessageBodyReaderWriterRegistry implements MessageBodyReaderWriterProvider {

  private Map<MediaType, MessageBodyReaderWriter<?>> handlers = new HashMap<>();

  public void register(final MediaType mediaType, final MessageBodyReaderWriter<?> readerWriter) {
    handlers.put(mediaType, readerWriter);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> MessageBodyReaderWriter<T> get(final MediaType mediaType) {
    return (MessageBodyReaderWriter<T>) handlers.get(mediaType);
  }

  public static MessageBodyReaderWriterProvider createWithDefaults() {
    MessageBodyReaderWriterRegistry provider = new MessageBodyReaderWriterRegistry();
    provider.register(MediaType.APPLICATION_JSON_TYPE, new JsonMessageBodyReaderWriter());
    provider.register(MediaType.TEXT_PLAIN_TYPE, new TextMessageBodyReaderWriter());
    provider.register(MediaType.APPLICATION_FORM_URLENCODED_TYPE, new FormParamMessageBodyReaderWriter());
    return provider;
  }

}
