package libs.source.common.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/15.
 */
public class MediaScanner {
    private static final String TAG = MediaScanner.class.getSimpleName();

    @NonNull
    private static MediaScanner Instance = new MediaScanner();
    private MediaScannerThread scannerThread;
    @NonNull
    private ArrayList<String> pathList = new ArrayList<>();
    private Context mContext;

    @NonNull
    public static MediaScanner getInstance() {
        return MediaScanner.Instance;
    }

    private MediaScanner() {
    }

    /**
     * notify media scanner scanning file
     *
     * @param path file path to scanning
     */
    public void scanningFile(Context context, @Nullable String path) {
        mContext = context;
        if (null == scannerThread || !scannerThread.isAlive()) {
            scannerThread = new MediaScannerThread("MediaScanner-Thread");
            scannerThread.start();
        }
        if (!TextUtils.isEmpty(path) && null != scannerThread && scannerThread.isAlive()) {
            scannerThread.scanningFile(path);
        }
    }

    /**
     * stop scanning files
     */
    public void stop() {
        if (null != scannerThread) {
            scannerThread.stopScanner();
        }
    }

    private class MediaScannerThread extends Thread {
        private boolean hasTask = false;
        @Nullable
        private MediaScannerConnection conn = null;
        @NonNull
        private MediaScannerConnection.MediaScannerConnectionClient client = new MediaScannerConnection.MediaScannerConnectionClient() {
            /**
             * Called to notify the client when a connection to the
             * MediaScanner service has been established.
             */
            @Override
            public void onMediaScannerConnected() {
                Timber.d( "---------Scanner Connected");
                synchronized (pathList) {
                    pathList.notify();
                }
            }

            /**
             * Called to notify the client when the media scanner has finished
             * scanning a file.
             *
             * @param path the path to the file that has been scanned.
             * @param uri  the Uri for the file if the scanning operation succeeded
             */
            @Override
            public void onScanCompleted(String path, Uri uri) {
                Timber.d("<<<<<<<<<<Scanning complete: " + path);
                hasTask = false;
                synchronized (MediaScannerThread.this) {
                    MediaScannerThread.this.notify();
                }
                try {
                    Thread.sleep(20); // sleep 10ms
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (pathList) {
                    pathList.remove(path);
                    pathList.notify();
                }
            }
        };

        public MediaScannerThread(@NonNull String threadName) {
            super(threadName);
        }


        @Override
        public void run() {
            while (!isInterrupted()) {
                if (hasTask) {
                    try {
                        synchronized (this) {
                            Timber.d( "~~~~~~~~~Waiting to scanning task stop");
                            this.wait();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                try {
                    Timber.d( "~~~~~~~~~Waiting to scanning file...");
                    synchronized (pathList) {
                        pathList.wait();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                synchronized (pathList) {
                    if (pathList.size() > 0) {
                        String path = pathList.get(0);
                        Timber.d( ">>>>>>>>>>>Scanning file: " + path);
                        if (conn != null) {
                            if (conn.isConnected()) {
                                try {
                                    conn.scanFile(path, MIMETypeUtils.getMIMEType(path));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else
                                conn.connect();
                        }
                        hasTask = true;
                    }
                }
            }
        }

        /**
         * stop scanning files
         */
        public void stopScanner() {
            interrupt();
            if (null != conn && conn.isConnected()) {
                conn.disconnect();
                conn = null;
            }
        }

        /**
         * notify media scanner scanning file
         *
         * @param path file path to scanning
         */
        public void scanningFile(String path) {
            Timber.d( "========Add scanning file: " + path);
            synchronized (pathList) {
                pathList.add(path);
                if (conn == null) {
                    conn = new MediaScannerConnection(mContext, client);
                }
                if (!conn.isConnected()) {
                    conn.connect();
                } else {
                    pathList.notify();
                }

            }
        }
    }
}
