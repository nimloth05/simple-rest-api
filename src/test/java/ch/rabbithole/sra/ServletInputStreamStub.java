package ch.rabbithole.sra;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;

import ch.rabbithole.sra.resource.HeaderUtil;

/**
 * TODO JavaDoc
 */
public final class ServletInputStreamStub extends ServletInputStream {

  private final InputStream inputStream;

  public static ServletInputStream create(final String input) {
    return new ServletInputStreamStub(new ByteArrayInputStream(input.getBytes(HeaderUtil.UTF8)));
  }


  public ServletInputStreamStub(final InputStream inputStream) {
    this.inputStream = inputStream;
  }

  @Override
  public int read() throws IOException {
    return inputStream.read();
  }
}
