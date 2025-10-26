package ch.rabbithole.sra;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

public class StubServletOutputStream extends ServletOutputStream {

 public ByteArrayOutputStream baos = new ByteArrayOutputStream();

   public void write(int i) throws IOException {
    baos.write(i);
 }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void setWriteListener(WriteListener writeListener) {

  }
}
