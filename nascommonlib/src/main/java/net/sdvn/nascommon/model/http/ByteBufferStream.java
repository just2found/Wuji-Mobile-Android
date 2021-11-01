package net.sdvn.nascommon.model.http;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


public class ByteBufferStream extends InputStream {
    private static final int UNSET = -1;
    @NonNull
    private final ByteBuffer byteBuffer;
    private int markPos = UNSET;

    public ByteBufferStream(@NonNull ByteBuffer byteBuffer) {
      this.byteBuffer = byteBuffer;
    }
    public int length(){
        return byteBuffer.limit();
    }
    @Override
    public int available() {
      return byteBuffer.remaining();
    }

    @Override
    public int read() {
      if (!byteBuffer.hasRemaining()) {
        return -1;
      }
      return byteBuffer.get();
    }

    @Override
    public synchronized void mark(int readLimit) {
      markPos = byteBuffer.position();
    }

    @Override
    public boolean markSupported() {
      return true;
    }

    @Override
    public int read(@NonNull byte[] buffer, int byteOffset, int byteCount) throws IOException {
      if (!byteBuffer.hasRemaining()) {
        return -1;
      }
      int toRead = Math.min(byteCount, available());
      byteBuffer.get(buffer, byteOffset, toRead);
      return toRead;
    }

    @Override
    public synchronized void reset() throws IOException {
      if (markPos == UNSET) {
        throw new IOException("Cannot reset to unset mark position");
      }
      // reset() was not implemented correctly in 4.0.4, so we track the mark position ourselves.
      byteBuffer.position(markPos);
    }

    @Override
    public long skip(long byteCount) throws IOException {
      if (!byteBuffer.hasRemaining()) {
        return -1;
      }

      long toSkip = Math.min(byteCount, available());
      byteBuffer.position((int) (byteBuffer.position() + toSkip));
      return toSkip;
    }

}