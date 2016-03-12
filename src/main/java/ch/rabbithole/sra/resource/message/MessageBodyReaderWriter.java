package ch.rabbithole.sra.resource.message;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * Glue interface
 */
public interface MessageBodyReaderWriter<T> extends MessageBodyReader<T>, MessageBodyWriter<T> {

}
