package net.sdvn.nascommon.model.http;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.Serializable;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by yun on 2018/1/10.
 */

public class ProgressRequestBody extends RequestBody implements Serializable {
    private static final long serialVersionUID = -2356316059481125946L;
    private transient RequestBody requestBody;         //实际的待包装请求体


    private ProgressInterceptor interceptor;
    private CountingSink countingSink;

    public ProgressRequestBody(RequestBody requestBody, ProgressInterceptor interceptor) {
        this.requestBody = requestBody;
        this.interceptor = interceptor;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() {
        try {
            return requestBody.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        if (countingSink == null)
            countingSink = new CountingSink(contentLength(), sink);
        BufferedSink bufferedSink = Okio.buffer(countingSink);
        requestBody.writeTo(bufferedSink);
        bufferedSink.flush();

    }


    /**
     * 包装
     */
    private final class CountingSink extends ForwardingSink {

        private final long size;

        CountingSink(long size, @NonNull Sink delegate) {
            super(delegate);

            this.size = size;
        }


        @Override
        public void write(@NonNull Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            if (interceptor != null) {
                interceptor.progress(size, byteCount);
            }

        }
    }


    public interface ProgressInterceptor {

        void progress(long contentLength, long byteCount) throws IOException;
    }
}
