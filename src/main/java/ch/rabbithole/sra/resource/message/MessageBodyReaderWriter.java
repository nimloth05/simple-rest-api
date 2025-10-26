package ch.rabbithole.sra.resource.message;

import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;

/**
 * Glue interface
 */
public interface MessageBodyReaderWriter<T> extends MessageBodyReader<T>, MessageBodyWriter<T> {

}
