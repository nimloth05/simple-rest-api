package ch.rabbithole.sra;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

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

  @Override
  public boolean isFinished() {
    return false;
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void setReadListener(ReadListener readListener) {

  }
}
