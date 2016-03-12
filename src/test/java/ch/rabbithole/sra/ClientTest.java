package ch.rabbithole.sra;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.rabbithole.sra.impl.UriBuilderImpl;
import ch.rabbithole.sra.resource.Client;
import ch.rabbithole.sra.resource.message.MessageBodyReaderWriterRegistry;

import static org.junit.Assert.assertEquals;

public final class ClientTest {


  public static void main(String[] args) {
    Client client = new Client(MessageBodyReaderWriterRegistry.createWithDefaults());
    final Response response = client
        .target(UriBuilderImpl.fromUri("http://www.google.com").build())
        .type(MediaType.TEXT_PLAIN_TYPE)
        .acceptJavaType(String.class)
        .accept(MediaType.TEXT_PLAIN_TYPE)
        .get();

    assertEquals(200, response.getStatus());
    System.out.println(response.getEntity());
  }


}
