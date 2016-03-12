package ch.rabbithole.sra;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class CountingOutputStream extends FilterOutputStream {

  private long transferred;

  public CountingOutputStream(final OutputStream out) {
    super(out);
    this.transferred = 0;
  }

  public void write(byte[] b, int off, int len) throws IOException {
    out.write(b, off, len);
    this.transferred += len;
  }

  public void write(int b) throws IOException {
    out.write(b);
    this.transferred++;
  }

  public long getTransferred() {
    return transferred;
  }
}