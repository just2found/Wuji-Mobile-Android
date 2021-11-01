package net.sdvn.nascommon.model.oneos.transfer.limiter;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

public class DownloadLimiter extends InputStream {
    @Nullable
    private InputStream is = null;
    @Nullable
    private BandwidthLimiter bandwidthLimiter = null;

    public DownloadLimiter(InputStream is, BandwidthLimiter bandwidthLimiter) {
        this.is = is;
        this.bandwidthLimiter = bandwidthLimiter;
    }

    @Override
    public int read() throws IOException {
        if (this.bandwidthLimiter != null)
            this.bandwidthLimiter.limitNextBytes();
        return this.is.read();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (bandwidthLimiter != null)
            bandwidthLimiter.limitNextBytes(len);
        return this.is.read(b, off, len);
    }
}