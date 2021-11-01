package net.sdvn.nascommon.model.oneos.transfer.limiter;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.OutputStream;

public class UploadLimiter extends OutputStream {
    @Nullable
    private OutputStream os = null;
    @Nullable
    private BandwidthLimiter bandwidthLimiter = null;

    public UploadLimiter(OutputStream os, BandwidthLimiter bandwidthLimiter) {
        this.os = os;
        this.bandwidthLimiter = bandwidthLimiter;
    }

    @Override
    public void write(int b) throws IOException {
        if (bandwidthLimiter != null)
            bandwidthLimiter.limitNextBytes();
        this.os.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (bandwidthLimiter != null)
            bandwidthLimiter.limitNextBytes(len);
        this.os.write(b, off, len);
    }

}