package net.linkmate.app.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import net.linkmate.app.R;
import net.linkmate.app.util.FileUtils;
import net.linkmate.app.util.OpenFiles;
import net.sdvn.nascommon.utils.EmptyUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static net.linkmate.app.base.MyConstants.UPDATE_CHANNEL_ID;
import static net.linkmate.app.base.MyConstants.UPDATE_CHANNEL_NAME;
import static net.linkmate.app.base.MyConstants.UPDATE_NOTIFICATION_ID;

/**
 * Created by eric on 15/2/6.
 */
public class UpdateService extends Service {
    interface STATUS {
        int START = 0;
        int COMPLETED = 2;
        int DOWNLOADING = 3;
        int FAILURE = 4;

    }

    public static final String KEY_DOWNLOAD_URL = "key_download_url";
    public static final String KEY_DOWNLOAD_HASH = "key_download_HASH";
    private NotificationManager nm;
    private Notification notification;
    private File downloadFile = null;
    private int download_percent = 0;
    private RemoteViews remoteView;
    private UpdateHandler updateHandler;
    private Executor mExecutor;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //创建通知渠道
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onDestroy() {
        nm.cancel(UPDATE_NOTIFICATION_ID);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String update_url = intent.getStringExtra(KEY_DOWNLOAD_URL);
            String update_hash = intent.getStringExtra(KEY_DOWNLOAD_HASH);
            Intent newIntent = null;
            if (!EmptyUtils.isEmpty(update_url)) {
                remoteView = new RemoteViews(getPackageName(), R.layout.update_progress);
                updateHandler = new UpdateHandler(Looper.getMainLooper(), this);
                updateHandler.sendMessage(updateHandler.obtainMessage(STATUS.DOWNLOADING, 0));
                updateHandler.sendMessage(updateHandler.obtainMessage(STATUS.START, getString(R.string.downloading)));
                downFile(update_url, update_hash);
                newIntent = intent;
            }
            NotificationCompat.Builder builder = buildBaseNotification(this, nm);
            if (newIntent != null) {
                PendingIntent pendingIntent = PendingIntent.getService(this, 0, newIntent, 0);
                builder.setContentIntent(pendingIntent);
            }
            notification = builder.build();
            this.startForeground(UPDATE_NOTIFICATION_ID, notification);
            return START_STICKY;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    NotificationCompat.Builder buildBaseNotification(Context context, NotificationManager manager) {

        String channelId = UPDATE_CHANNEL_ID;
        String channelName = UPDATE_CHANNEL_NAME;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            manager.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.stat_sys_download)
                .setTicker(getString(R.string.app_name) + " " + getString(R.string.update))
                .setWhen(System.currentTimeMillis())
                .setSound(null)
                .setVibrate(null)
                .setContent(remoteView);
        return builder;
    }

    private void downFile(String url, final String hash) {
        String fileName = url.substring(url.lastIndexOf("/") + 1);
//        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
//        request.setMimeType(MIMETypeUtils.getMIMEType(fileName));
//        request.allowScanningByMediaScanner();
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
//        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
//        if (dm != null) {
//            long id = dm.enqueue(request);
//            ToastHelper.showToast(R.string.start_download_file);
//        }

        mExecutor.execute(new Runnable() {
            public void run() {
                InputStream is = null;
                BufferedInputStream bis = null;
                FileOutputStream fos = null;
                BufferedOutputStream bos = null;
                try {
                    download_percent = 0;
                    downloadFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + fileName);
                    if (downloadFile.exists()) {
                        if (!Objects.equals(FileUtils.getFileMd5(downloadFile), hash)) {
                            downloadFile.delete();
                        } else {
                            download_percent = 100;
                        }
                    }
                    if (download_percent < 100) {
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url(url)
                                .build();
                        Response response = client.newCall(request).execute();
                        if (!response.isSuccessful())
                            throw new IOException("Unexpected code " + response);
                        long length = response.body().contentLength();
                        is = response.body().byteStream();

                        downloadFile.createNewFile();

                        bis = new BufferedInputStream(is);
                        fos = new FileOutputStream(downloadFile);
                        bos = new BufferedOutputStream(fos);

                        int read;
                        long count = 0;
                        int precent = 0;
                        byte[] buffer = new byte[1024];
                        while (-1 != (read = bis.read(buffer))) {
                            bos.write(buffer, 0, read);
                            count += read;
                            precent = (int) (((double) count / length) * 100);
                            //每下载完成1%就通知任务栏进行修改下载进度
                            if ((precent - download_percent) >= 1) {
                                download_percent = precent;
                                Message message = updateHandler.obtainMessage(STATUS.DOWNLOADING, precent);
                                updateHandler.sendMessage(message);
                            }
                        }
                    }

                    if (download_percent == 100) {
                        Message message = updateHandler.obtainMessage(STATUS.COMPLETED, downloadFile);
                        updateHandler.sendMessage(message);
                    } else {
                        Message message = updateHandler.obtainMessage(STATUS.FAILURE, getString(R.string.DownloadFail));
                        updateHandler.sendMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message message = updateHandler.obtainMessage(STATUS.FAILURE, getString(R.string.DownloadFail));
                    updateHandler.sendMessage(message);
                } finally {
                    try {
                        if (bos != null) {
                            bos.flush();
                            bos.close();
                        }
                        if (fos != null) {
                            fos.flush();
                            fos.close();
                        }
                        if (bis != null) {
                            bis.close();
                        }
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    class UpdateHandler extends Handler {
        private Context context;

        public UpdateHandler(Looper looper, Context c) {
            super(looper);
            this.context = c;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (null != msg) {
                switch (msg.what) {
                    case STATUS.START:
                        Toast.makeText(context, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        break;
                    case STATUS.COMPLETED:
                        download_percent = 0;
                        nm.cancel(UPDATE_NOTIFICATION_ID);
                        OpenFiles.openFile(context,(File) msg.obj);
                        stopSelf();
                        break;
                    case STATUS.DOWNLOADING:
                        remoteView.setTextViewText(R.id.update_ptDownload, download_percent + "%");
                        remoteView.setProgressBar(R.id.update_pbDownload, 100, download_percent, false);
                        notification.contentView = remoteView;
                        nm.notify(UPDATE_NOTIFICATION_ID, notification);
                        break;
                    case STATUS.FAILURE:
                        Toast.makeText(context, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }
}
