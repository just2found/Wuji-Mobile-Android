package net.sdvn.nascommon.model.http;

import androidx.annotation.Nullable;

import java.io.IOException;

import okhttp3.MediaType;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class ByteBufferRequestBody extends okhttp3.RequestBody{
    @Nullable
    private final MediaType mContentType;
    private final ByteBufferStream mByteBufferStream;

    public ByteBufferRequestBody(final @Nullable MediaType contentType, ByteBufferStream byteBufferStream){
        mContentType = contentType;
        mByteBufferStream = byteBufferStream;
    }

    @javax.annotation.Nullable
    @Override
    public MediaType contentType() {
        return mContentType;
    }

    @Override
    public long contentLength() throws IOException {
        return mByteBufferStream.length();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        try (Source source = Okio.source(mByteBufferStream)) {
            sink.writeAll(source);
        }
    }
}
