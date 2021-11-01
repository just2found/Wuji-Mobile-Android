package net.sdvn.nascommon.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.bumptech.glide.request.transition.Transition;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.ielse.imagewatcher.ImageWatcher;
import com.github.ielse.imagewatcher.ImageWatcherHelper;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.disklrucache.DiskLruCache;
import com.rxjava.rxlife.RxLife;

import net.sdvn.cmapi.util.LogUtils;
import net.sdvn.common.DynamicDBHelper;
import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.constant.OneOSAPIs;
import net.sdvn.nascommon.iface.Callback;
import net.sdvn.nascommon.iface.Result;
import net.sdvn.nascommon.model.glide.EliCacheGlideUrl;
import net.sdvn.nascommon.model.glide.GlideCacheConfig;
import net.sdvn.nascommon.model.oneos.FileInfoHolder;
import net.sdvn.nascommon.model.oneos.OneOSFile;
import net.sdvn.nascommon.model.oneos.OneOSFileManage;
import net.sdvn.nascommon.model.oneos.OneOSFileType;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.model.phone.LocalFile;
import net.sdvn.nascommon.receiver.ResultCallback;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.WeakHashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.weline.libimageloader.OnProgressListener;
import io.weline.libimageloader.ProgressInterceptor;
import libs.source.common.AppExecutors;
import timber.log.Timber;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

//import com.google.android.exoplayer2.MediaItem;
//import io.weline.mediaplayer.IntentUtil;
//import io.weline.mediaplayer.PlayerActivity;

public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();
    public static final String DEFAULT_TIME_FMT = "yyyy-MM-dd HH:mm:ss";

    public static final String ROOT_DIR = "Android/data/"
            + Utils.getApp().getPackageName();
    public static final String DOWNLOAD_DIR = "download";
    public static final String CACHE_DIR = "cache";
    public static final String ICON_DIR = "images";
    public static final String CRASH_DIR = "crash";
    public static final String separator = File.separator;

    public static boolean isContentUri(@Nullable Uri uri) {
        return uri != null && ContentResolver.SCHEME_CONTENT.equals(uri.getScheme());
    }

    @TargetApi(26)
    public static long getCacheSizeByAndroidO(Context mContext, @NonNull String mPackageName) {

        StorageStatsManager storageStatsManager =
                (StorageStatsManager) mContext.getSystemService(Context.STORAGE_STATS_SERVICE);

        try {
            if (storageStatsManager != null) {
                StorageStats storageStats = storageStatsManager.
                        queryStatsForPackage(StorageManager.UUID_DEFAULT,
                                mPackageName, android.os.Process.myUserHandle());
                return storageStats.getCacheBytes();
            }
            return 0L;
        } catch (@NonNull PackageManager.NameNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    /**
     * get photo date
     *
     * @param file
     * @return photo date
     */
    public static String getPhotoDate(@NonNull File file) {
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            if (exif != null) {
                String dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
                if (dateTime != null) {
                    String date = exif.getAttribute(ExifInterface.TAG_DATETIME).substring(0, 7);
                    return date.replace(":", "-");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return AppConstants.PHOTO_DATE_UNKNOWN;
    }

    /**
     * get video date
     *
     * @param file
     * @return video date
     */
    public static String getVideoDate(File file) {
        long time = file.lastModified();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        return format.format(new Date(time));
    }

    /**
     * Compress Image
     *
     * @param imgPath
     * @param width   target width
     * @param height  target height
     * @return
     */
    public static Bitmap compressImage(String imgPath, float width, float height) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，options.inJustDecodeBounds=true，即只读边不读内容
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;

        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, newOpts);
        newOpts.inJustDecodeBounds = false;

        int w = newOpts.outWidth;
        int h = newOpts.outHeight;

        float hh = height;
        float ww = width;

        int be = 1;// 缩放比例
        if (w > h && w > ww) {
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0) {
            be = 1;
        }
        newOpts.inSampleSize = be;

        bitmap = BitmapFactory.decodeFile(imgPath, newOpts);

        return bitmap;
    }

    public static String getFileTime(File file) {
        long time = file.lastModified();
//        SimpleDateFormat format = new SimpleDateFormat(DEFAULT_TIME_FMT, Locale.getDefault());
        return formatTime(time, DEFAULT_TIME_FMT);
    }

    public static String getCurFormatTime() {
        return formatTime(System.currentTimeMillis());
    }

    @Nullable
    public static String getCurFormatTime(@NonNull String fmt) {
        return formatTime(System.currentTimeMillis(), fmt);
    }

    public static String formatTime(long time) {
//        SimpleDateFormat format = new SimpleDateFormat(DEFAULT_TIME_FMT, Locale.getDefault());
//        String date = format.format(new Date(time));
        return formatTime(time, DEFAULT_TIME_FMT);
    }

    public static String formatTime(long time, @NonNull String fmt) {
        if (EmptyUtils.isEmpty(fmt)) {
            return null;
        }
        if (time <= 0) {
            return Utils.getApp().getString(R.string.unknown);
        }
        SimpleDateFormat format = new SimpleDateFormat(fmt, Locale.getDefault());
        return format.format(new Date(time));
    }

    public static long parseFmtTime(String time, String fmt) {
        if (EmptyUtils.isEmpty(time)) {
            return 0;
        }
        if (EmptyUtils.isEmpty(fmt)) {
            fmt = DEFAULT_TIME_FMT;
        }
        SimpleDateFormat format = new SimpleDateFormat(fmt, Locale.getDefault());
        try {
            Date date = format.parse(time);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Converted into a standard BeiJing Time, and format output
     */
    public static String fmtTimeByZone(long time, @NonNull String format) {
//        System.setProperty("user.timezone", "Asia/Shanghai");
//        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
//        TimeZone.setDefault(tz);
        Date date = new Date(time * 1000L);
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        return fmt.format(date);
    }

    /**
     * Converted into a standard BeiJing Time, and format["yyyy-MM-dd  HH:mm:ss"] output
     */
    public static String fmtTimeByZone(long time) {
        return fmtTimeByZone(time, DEFAULT_TIME_FMT);
    }

    /**
     * get file except folder
     */
    public static int fmtFileIcon(String name) {
        if (EmptyUtils.isEmpty(name)) {
            return R.drawable.icon_device_other;
        }

        name = name.toLowerCase().trim();
        int icon;
        if (MIMETypeUtils.isAudioFile(name)) {
            icon = R.drawable.icon_device_music;
        } else if (MIMETypeUtils.isVideoFile(name)) {
            icon = R.drawable.icon_device_vedio;
        } else if (name.toLowerCase().endsWith(".txt") || name.toLowerCase().endsWith(".log")) {
            icon = R.drawable.icon_device_doc;
        } else if (name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg")
                || name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".bmp")
                || name.toLowerCase().endsWith(".gif")
        ) {
            icon = R.drawable.icon_device_img;
        } else if (name.toLowerCase().endsWith(".apk")) {
            icon = R.drawable.icon_device_android;
        } else if (name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".rar") || name.toLowerCase().endsWith(".nz2") || name.toLowerCase().endsWith(".bz")
                || name.toLowerCase().endsWith(".gz") || name.toLowerCase().endsWith(".7z")
                || name.toLowerCase().endsWith(".iso") || name.toLowerCase().endsWith(".tgz")
                || name.toLowerCase().endsWith(".jar")) {
            icon = R.drawable.icon_device_zip;
        } else if (name.toLowerCase().endsWith(".xls") || name.toLowerCase().endsWith(".xlsx")) {
            icon = R.drawable.icon_device_xsl;
        } else if (name.toLowerCase().endsWith(".ppt") || name.toLowerCase().endsWith(".pptx")) {
            icon = R.drawable.icon_device_ppt;
        } else if (name.toLowerCase().endsWith(".doc") || name.toLowerCase().endsWith(".docx")) {
            icon = R.drawable.icon_device_word;
        } else if (name.toLowerCase().endsWith(".pdf")) {
            icon = R.drawable.icon_device_pdf;
        } else if (name.toLowerCase().endsWith(".bin") || name.toLowerCase().endsWith(".exe")) {
            icon = R.drawable.icon_device_other;
        } else if (name.toLowerCase().endsWith(".torrent")) {
            icon = R.drawable.icon_device_bt;
        } else {
            icon = R.drawable.icon_device_other;
        }

        return icon;
    }

    public static int fmtFileIcon(File file) {
        if (file.isDirectory()) {
            return R.drawable.icon_file_folder;
        }

        return fmtFileIcon(file.getName());
    }

    public static String fmtFileSize(long len) {
//        return Formatter.formatFileSize(Utils.getApp(), len);
        String sizeFormat;
        long B = len % 1024;
        long KB = len / 1024;
        long MB = KB / 1024;
        long GB = MB / 1024;
        long TB = GB / 1024;
        if (TB > 0) {
            long decimals = GB % 1024 * 100 / 1024;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = TB + "." + decimalsStr + "TB";
        } else if (GB > 0) {
            long decimals = MB % 1024 * 100 / 1024;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = GB + "." + decimalsStr + "GB";
        } else if (MB > 0) {
            long decimals = KB % 1024 * 100 / 1024;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = MB + "." + decimalsStr + "MB";
        } else if (KB > 0) {
            long decimals = B % 1024 * 100 / 1024;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = KB + "." + decimalsStr + "KB";
        } else {
            sizeFormat = B + "B";
        }
        return sizeFormat;
    }

    public static long calcETA(long totalBytes, long curBytes, long speed) {
        long left = totalBytes - curBytes;
        if (left <= 0)
            return 0;
        if (speed <= 0)
            return -1;

        return left / speed;
    }

    public static String fmtFileSpeed(long len) {
//        return Formatter.formatFileSize(Utils.getApp(), len);
        String sizeFormat;
        long B = len % 1000;
        long KB = len / 1000;
        long MB = KB / 1000;
        long GB = MB / 1000;
        long TB = GB / 1000;
        if (TB > 0) {
            long decimals = GB % 1000 * 100 / 1000;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = TB + "." + decimalsStr + "T";
        } else if (GB > 0) {
            long decimals = MB % 1000 * 100 / 1000;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = GB + "." + decimalsStr + "G";
        } else if (MB > 0) {
            long decimals = KB % 1000 * 100 / 1000;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = MB + "." + decimalsStr + "M";
        } else if (KB > 0) {
            long decimals = B % 1000 * 100 / 1000;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = KB + "." + decimalsStr + "K";
        } else {
            sizeFormat = B + "B";
        }
        return sizeFormat;
    }

    public static String fmtFileSpeed2(long len) {
//        return Formatter.formatFileSize(Utils.getApp(), len);
        String sizeFormat;
        len = len * 8;
        long B = len % 1000;
        long KB = len / 1000;
        long MB = KB / 1000;
        long GB = MB / 1000;
        long TB = GB / 1000;
        if (TB > 0) {
            long decimals = GB % 1000 * 100 / 1000;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = TB + "." + decimalsStr + "Tb";
        } else if (GB > 0) {
            long decimals = MB % 1000 * 100 / 1000;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = GB + "." + decimalsStr + "Gb";
        } else if (MB > 0) {
            long decimals = KB % 1000 * 100 / 1000;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = MB + "." + decimalsStr + "Mb";
        } else if (KB > 0) {
            long decimals = B % 1000 * 100 / 1000;
            String decimalsStr = String.valueOf(decimals >= 10 ? decimals : "0" + decimals);
            sizeFormat = KB + "." + decimalsStr + "Kb";
        } else {
            sizeFormat = B + "b";
        }
        return sizeFormat;
    }
    public static void openOneOSFile(@NonNull LoginSession loginSession, @NonNull Context context,  @NonNull final OneOSFile file) {
        String url = OneOSAPIs.genOpenUrl(loginSession, file,null);
        Uri uri = Uri.parse(url);
        if (file.hasLocalFile())
            uri = getFileProviderUri(file.getLocalFile());
        openFileByOtherApp(context, uri, file.getName(), (e, e2) -> {
            ToastHelper.showLongToastSafe(R.string.operate_failed);
        });
    }

    public static void openOneOSFile(@NonNull LoginSession loginSession, @NonNull FragmentActivity activity, View view, int position, @NonNull final List<OneOSFile> fileList, OneOSFileType fileType) {

//        if (!LoginManage.getInstance().isHttp()) {
////            MagicDialog dialog = new MagicDialog(activity);
////            dialog.title(R.string.tips).content(R.string.tip_ssudp_open_file_failed).positive(R.string.ok)
////                    .bold(MagicDialog.MagicDialogButton.POSITIVE).show();
////            return;
////        }

        OneOSFile file = fileList.get(position);
        if (file.isEncrypt()) {
            DialogUtils.showNotifyDialog(activity, R.string.tips, R.string.error_open_encrypt_file, R.string.ok, null);
            return;
        }

        if (file.isPicture()) {
            ArrayList<OneOSFile> picList = new ArrayList<>();
            for (OneOSFile f : fileList) {
                if (f.isPicture()) {
                    picList.add(f);
                    if (f == file) {
                        position = picList.size() - 1;
                    }
                }
            }
            openOneOSPicture(activity, view, position, picList, fileType, loginSession);
        } else if (file.isVideo() || file.isAudio()) {
            String url = OneOSAPIs.genOpenUrl(loginSession, file,null);
            Uri uri = Uri.parse(url);
            if (file.hasLocalFile())
                uri = getFileProviderUri(file.getLocalFile());
//            if (BuildConfig.DEBUG) {
//                Intent intent = new Intent(activity, PlayerActivity.class);
//                intent.putExtra(
//                        IntentUtil.PREFER_EXTENSION_DECODERS_EXTRA,
//                        false);
//                List<MediaItem> mediaItems = new ArrayList<>();
//                MediaItem mediaItem = new MediaItem.Builder()
//                        .setUri(url)
//                        .setMimeType(MIMETypeUtils.getMIMEType(file.getName()))
//                        .build();
//                mediaItems.add(mediaItem);
//                IntentUtil.addToIntent(mediaItems, intent);
//                activity.startActivity(intent);
//            } else {
            openFileByOtherApp(activity, uri, file.getName(), (e, e2) -> {
                ToastHelper.showLongToastSafe(R.string.operate_failed);
            });
//            }
        } else if (file.isExtract()) {
            new OneOSFileManage(activity, null, loginSession, view, new OneOSFileManage.OnManageCallback() {
                @Override
                public void onComplete(boolean isSuccess) {

                }
            }).doOnlineExtract(file);
        } else {
            String toPath = SessionManager.getInstance().getDefaultDownloadPathByID(loginSession.getId(), file);
            String localPath = toPath + File.separator + file.getName();
            File localFile = new File(localPath);

            if (localFile.exists() && localFile.isFile() && localFile.length() == file.getSize()) {
                openFileByOtherApp(activity, getFileProviderUri(localFile), file.getName(), (e, e2) -> {
                    ToastHelper.showLongToastSafe(R.string.error_app_not_found_to_open_file);
                });
            } else {
                ARouter.getInstance().build("/nas/file_view", "nas")
                        .withSerializable("file", file)
                        .withString(AppConstants.SP_FIELD_DEVICE_ID, loginSession.getId())
                        .navigation(activity);
            }
        }
    }

    public static void openOneOSFile(@NonNull LoginSession loginSession, @NonNull FragmentActivity activity, View view, int position, @NonNull final List<OneOSFile> fileList, OneOSFileType fileType,Long groupId) {

//        if (!LoginManage.getInstance().isHttp()) {
////            MagicDialog dialog = new MagicDialog(activity);
////            dialog.title(R.string.tips).content(R.string.tip_ssudp_open_file_failed).positive(R.string.ok)
////                    .bold(MagicDialog.MagicDialogButton.POSITIVE).show();
////            return;
////        }

        OneOSFile file = fileList.get(position);
        if (file.isEncrypt()) {
            DialogUtils.showNotifyDialog(activity, R.string.tips, R.string.error_open_encrypt_file, R.string.ok, null);
            return;
        }

        if (file.isPicture()) {
            ArrayList<OneOSFile> picList = new ArrayList<>();
            for (OneOSFile f : fileList) {
                if (f.isPicture()) {
                    picList.add(f);
                    if (f == file) {
                        position = picList.size() - 1;
                    }
                }
            }
            openOneOSPicture(activity, view, position, picList, fileType, loginSession,groupId);
        } else if (file.isVideo() || file.isAudio()) {
            String url = OneOSAPIs.genOpenUrl(loginSession, file,groupId);
            Uri uri = Uri.parse(url);
            if (file.hasLocalFile())
                uri = getFileProviderUri(file.getLocalFile());
//            if (BuildConfig.DEBUG) {
//                Intent intent = new Intent(activity, PlayerActivity.class);
//                intent.putExtra(
//                        IntentUtil.PREFER_EXTENSION_DECODERS_EXTRA,
//                        false);
//                List<MediaItem> mediaItems = new ArrayList<>();
//                MediaItem mediaItem = new MediaItem.Builder()
//                        .setUri(url)
//                        .setMimeType(MIMETypeUtils.getMIMEType(file.getName()))
//                        .build();
//                mediaItems.add(mediaItem);
//                IntentUtil.addToIntent(mediaItems, intent);
//                activity.startActivity(intent);
//            } else {
            openFileByOtherApp(activity, uri, file.getName(), (e, e2) -> {
                ToastHelper.showLongToastSafe(R.string.operate_failed);
            });
//            }
        } else if (file.isExtract()) {
            new OneOSFileManage(activity, null, loginSession, view, new OneOSFileManage.OnManageCallback() {
                @Override
                public void onComplete(boolean isSuccess) {

                }
            }).doOnlineExtract(file);
        } else {
            String toPath = SessionManager.getInstance().getDefaultDownloadPathByID(loginSession.getId(), file);
            String localPath = toPath + File.separator + file.getName();
            File localFile = new File(localPath);

            if (localFile.exists() && localFile.isFile() && localFile.length() == file.getSize()) {
                openFileByOtherApp(activity, getFileProviderUri(localFile), file.getName(), (e, e2) -> {
                    ToastHelper.showLongToastSafe(R.string.error_app_not_found_to_open_file);
                });
            } else {
                ARouter.getInstance().build("/nas/file_view", "nas")
                        .withSerializable("file", file)
                        .withLong("group_id", groupId)
                        .withString(AppConstants.SP_FIELD_DEVICE_ID, loginSession.getId())
                        .navigation(activity);
            }
        }
    }

    public static void openFileByOtherApp(Context context, Uri uri, @NonNull String fileName, @NonNull ResultCallback resultCallback) {
        try {
            Intent intent = new Intent();
            String type = MIMETypeUtils.getMIMEType(fileName);
            Logger.LOGD(TAG, "Open share file: " + uri + "; type: " + type);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT) &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            intent.setDataAndType(uri, type);
            Intent chooser = Intent.createChooser(intent,
                    context.getApplicationContext().getString(R.string.open));
            context.startActivity(chooser);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            resultCallback.onFailure(e, null);
        }
    }

    public static void addDataAndTypeIntoIntent(File file, @NonNull Intent intent) {
        //获取文件file的MIME类型
        String type = MIMETypeUtils.getMIMEType(file.getName());
        //设置intent的data和Type属性。
        Uri uri = getFileProviderUri(file);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.setDataAndType(uri, type);
        Logger.LOGE("{SDVNtest}", "Uri " + uri.toString());
        Logger.LOGE("{SDVNtest}", "type: " + type);
    }

    public static Uri getFileProviderUri(@NonNull File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                String authority = Utils.getApp().getPackageName() + ".fileprovider";
                LogUtils.d("authority=" + authority + "  file  " + file.getAbsolutePath());

                uri = FileProvider.getUriForFile(Utils.getApp()
                        , authority, file);
            } catch (Exception e) {
                uri = Uri.fromFile(file);
            }

        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    public static void openLocalFile(@NonNull AppCompatActivity activity, View view, int position, @NonNull final ArrayList<LocalFile> fileList) {
        try {
            File file = fileList.get(position).getFile();

            if (isPictureFile(file.getName())) {
                ArrayList<File> picList = new ArrayList<>();
                for (LocalFile f : fileList) {
                    if (isPictureFile(f.getName())) {
                        picList.add(f.getFile());
                        if (f.getFile() == file) {
                            position = picList.size() - 1;
                        }
                    }
                }
                openLocalPicture(activity, view, position, picList);
            } else {
                openLocalFile(activity, file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openOneOSPicture(@NonNull Activity activity, View view, int position,
                                        final ArrayList<OneOSFile> picList, OneOSFileType fileType, LoginSession loginSession) {
        FileInfoHolder.getInstance().save(FileInfoHolder.PIC, picList);
//        Intent intent = PictureViewActivity.Companion.startActivity(activity, loginSession.getId(), position, FileInfoHolder.PIC, fileType);
//        View icon = null;
//        if (icon != null && !TextUtils.isEmpty(icon.getTransitionName())) {
//
//            // Now we provide a list of Pair items which contain the view we can transitioning
//            // from, and the name of the view it is transitioning to, in the launched activity
//            ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                    activity, icon, activity.getString(R.string.transitionNamePhoto));
//
//            // Now we can start the Activity, providing the activity options as a bundle
//            ActivityCompat.startActivity(activity, intent, activityOptions.toBundle());
//        } else {
//            activity.overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
//            activity.startActivity(intent);
//        }
//        intent.putExtra("StartIndex", position)
//        intent.putExtra("PictureList", pic)
//        intent.putExtra("FileType", fileType)
        ARouter.getInstance().build("/nas/pic_view", "nas")
                .withSerializable("FileType", fileType)
                .withString("PictureList", FileInfoHolder.PIC)
                .withInt("StartIndex", position)
                .withString(AppConstants.SP_FIELD_DEVICE_ID, loginSession.getId())
                .navigation(activity);

//        openOsFileByImageWatcher(activity, position, picList, loginSession);
    }

    public static void openOneOSPicture(@NonNull Activity activity, View view, int position,
                                        final ArrayList<OneOSFile> picList, OneOSFileType fileType, LoginSession loginSession,Long groupId) {
        FileInfoHolder.getInstance().save(FileInfoHolder.PIC, picList);
//        Intent intent = PictureViewActivity.Companion.startActivity(activity, loginSession.getId(), position, FileInfoHolder.PIC, fileType);
//        View icon = null;
//        if (icon != null && !TextUtils.isEmpty(icon.getTransitionName())) {
//
//            // Now we provide a list of Pair items which contain the view we can transitioning
//            // from, and the name of the view it is transitioning to, in the launched activity
//            ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                    activity, icon, activity.getString(R.string.transitionNamePhoto));
//
//            // Now we can start the Activity, providing the activity options as a bundle
//            ActivityCompat.startActivity(activity, intent, activityOptions.toBundle());
//        } else {
//            activity.overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
//            activity.startActivity(intent);
//        }
//        intent.putExtra("StartIndex", position)
//        intent.putExtra("PictureList", pic)
//        intent.putExtra("FileType", fileType)
        ARouter.getInstance().build("/nas/pic_view", "nas")
                .withSerializable("FileType", fileType)
                .withString("PictureList", FileInfoHolder.PIC)
                .withInt("StartIndex", position)
                .withLong("groupId",groupId)
                .withString(AppConstants.SP_FIELD_DEVICE_ID, loginSession.getId())
                .navigation(activity);

//        openOsFileByImageWatcher(activity, position, picList, loginSession);
    }


    private static void openOsFileByImageWatcher(@NonNull AppCompatActivity activity, int position, ArrayList<OneOSFile> picList, LoginSession loginSession) {
        Map<Uri, OneOSFile> map = new HashMap<>();
        List<Uri> list = new ArrayList<>();
        for (OneOSFile oneOSFile : picList) {
            final Uri uri = Uri.parse(oneOSFile.getPath());
            list.add(uri);
            map.put(uri, oneOSFile);
        }
        WeakHashMap<BaseViewHolder, Integer> weakHashMap = new WeakHashMap<>();
        final OnProgressListener onProgressListener = new OnProgressListener() {
            @Override
            public void onProgress(Object imageUrl, long bytesRead, long totalBytes, boolean isDone, GlideException exception) {

            }
        };
        WeakHashMap<BaseViewHolder, Integer> weakHashMap2 = new WeakHashMap<>();
        ImageWatcherHelper.with(activity, new ImageWatcher.Loader() {
            @Override
            public void load(Context context, Uri uri, ImageWatcher.LoadCallback lc) {
                final OneOSFile file = map.get(uri);
                if (file != null) {
                    final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
                    final String url = OneOSAPIs.genDownloadUrl(loginSession, file.getAllPath());
                    final String thumbnailUrl = OneOSAPIs.genThumbnailUrl(loginSession, file.getPath());
                    Observable.create((ObservableOnSubscribe<Result<File>>) emitter -> {
                        File cachedFile = null;
                        try {
                            cachedFile = Glide.with(context).downloadOnly()
                                    .load(new EliCacheGlideUrl(url))
                                    .apply(new RequestOptions().onlyRetrieveFromCache(true))
                                    .submit()
                                    .get();
                            if (cachedFile != null && cachedFile.exists()) {
                                Logger.LOGD(TAG, "cachedFile: ", cachedFile.getAbsolutePath());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        emitter.onNext(new Result(cachedFile));
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .as(RxLife.as(activity))
                            .subscribe(result -> {
                                if (result.data != null && result.data.exists()) {
                                    Glide.with(context)
                                            .load(new EliCacheGlideUrl(url))
                                            .centerInside()
                                            .override(displayMetrics.widthPixels, displayMetrics.heightPixels)
                                            .into(new CustomTarget<Drawable>() {
                                                @Override
                                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                                    lc.onResourceReady(resource);
                                                }

                                                @Override
                                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                                }

                                                @Override
                                                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                                    lc.onLoadFailed(errorDrawable);
                                                }

                                                @Override
                                                public void onLoadStarted(@Nullable Drawable placeholder) {
                                                    lc.onLoadStarted(placeholder);

                                                }

                                            });
                                } else {
                                    final RequestBuilder<Drawable> requestBuilder = Glide.with(context)
                                            .load(new EliCacheGlideUrl(thumbnailUrl));
                                    DrawableCrossFadeFactory factory =
                                            new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(false).build();
                                    Glide.with(context)
                                            .load(new EliCacheGlideUrl(url))
                                            .centerInside()
                                            .transition(withCrossFade(factory))
                                            .thumbnail(requestBuilder)
                                            .override(displayMetrics.widthPixels, displayMetrics.heightPixels)
                                            .into(new CustomTarget<Drawable>() {
                                                @Override
                                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                                    lc.onResourceReady(resource);
                                                }

                                                @Override
                                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                                }

                                                @Override
                                                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                                    lc.onLoadFailed(errorDrawable);
                                                }

                                                @Override
                                                public void onLoadStarted(@Nullable Drawable placeholder) {
                                                    lc.onLoadStarted(placeholder);

                                                }

                                            });
                                }

                            }, Throwable::printStackTrace);
                }
            }
        }).setLoadingUIProvider(new ImageWatcher.LoadingUIProvider() {
            BaseViewHolder mHolder;

            @Override
            public View initialView(Context context, int position) {
                final View inflate = LayoutInflater.from(context).inflate(R.layout.layout_image_watcher_loading, null);
                mHolder = new BaseViewHolder(inflate);
                return inflate;
            }

            @Override
            public void start(int pos, View loadView) {
                final OneOSFile file = map.get(list.get(pos));
                if (file != null) {
                    String url = OneOSAPIs.genDownloadUrl(loginSession, file.getAllPath());
                    String tag = GlideCacheConfig.getImageName(url);
                    mHolder.setAssociatedObject(tag);
                    ProgressInterceptor.addListeners(tag, (imageUrl, bytesRead, totalBytes, isDone, exception) -> {
                        Logger.LOGD(TAG, "glide progress:", String.format("%s : %s/%s", imageUrl, bytesRead, totalBytes));
                        final int progress = (int) (bytesRead * 100f / totalBytes + 0.5f);
                        if (progress > 0) {
                            for (BaseViewHolder holder : weakHashMap.keySet()) {
                                if (Objects.equals(holder.getAssociatedObject(), imageUrl)) {
                                    holder.setProgress(R.id.circleProgressView, progress);
                                    break;
                                }
                            }
                        }
                    });
                }
                weakHashMap.put(mHolder, pos);
                mHolder.setGone(R.id.circleProgressView, true);
                loadView.setVisibility(View.VISIBLE);
            }

            @Override
            public void stop(int pos, View loadView) {
                weakHashMap.remove(mHolder);
                loadView.setVisibility(View.GONE);
            }
        })
                .show(list, position);
    }

    public static void openLocalPicture(@NonNull FragmentActivity activity, View view, int position, final ArrayList<File> picList) {
        List<Uri> list = new ArrayList<>();
        for (File oneOSFile : picList) {
            final Uri uri = Uri.parse(oneOSFile.getPath());
            list.add(uri);
        }
        ImageWatcherHelper.with(activity, new ImageWatcher.Loader() {
            @Override
            public void load(Context context, Uri uri, ImageWatcher.LoadCallback lc) {
                final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
                Glide.with(context)
                        .asDrawable()
                        .load(uri)
                        .centerInside()
                        .override(displayMetrics.widthPixels, displayMetrics.heightPixels)
                        .into(new CustomTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                lc.onResourceReady(resource);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                lc.onLoadFailed(errorDrawable);
                            }

                            @Override
                            public void onLoadStarted(@Nullable Drawable placeholder) {
                                lc.onLoadStarted(placeholder);
                            }
                        });
            }
        }).show(list, position);

    }

    public static boolean openLocalFile(@NonNull Context context, File file) {
        if (file.exists() && file.isFile()) {
            try {
                Intent intent = new Intent();
                String type = MIMETypeUtils.getMIMEType(file.getName());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Intent.ACTION_VIEW);
//                /* intent file MimeType */
//                intent.setDataAndType(Uri.fromFile(file), type);
                addDataAndTypeIntoIntent(file, intent);
                context.startActivity(intent);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                ToastHelper.showLongToastSafe(R.string.error_app_not_found_to_open_file);
                return true;
            }
        } else {
            DialogUtils.showNotifyDialog(context, R.string.tips, R.string.file_not_found, R.string.ok, null);
        }
        return false;
    }

    public static ImageWatcherHelper mImageWatcherHelper;

    public static void show(String url, String fileName, FragmentActivity activity) {
//        if (MIMETypeUtils.isImageFile(fileName)) {
//            if (mImageWatcherHelper == null)
//                mImageWatcherHelper = ImageWatcherHelper.with(activity, new ImageWatcher.Loader() {
//                    @Override
//                    public void load(Context context, Uri uri, ImageWatcher.LoadCallback lc) {
//
//                        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
//                        Glide.with(context)
//                                .asDrawable()
//                                .load(uri)
//                                .centerInside()
//                                .override(displayMetrics.widthPixels, displayMetrics.heightPixels)
//                                .into(new CustomTarget<Drawable>() {
//                                    @Override
//                                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//                                        lc.onResourceReady(resource);
//                                    }
//
//                                    @Override
//                                    public void onLoadCleared(@Nullable Drawable placeholder) {
//                                    }
//
//                                    @Override
//                                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
//                                        lc.onLoadFailed(errorDrawable);
//                                    }
//
//                                    @Override
//                                    public void onLoadStarted(@Nullable Drawable placeholder) {
//                                        lc.onLoadStarted(placeholder);
//                                    }
//                                });
//
//                    }
//                })
//                        .setTranslucentStatus(StatusBarUtils.getStatusBarOffsetPx(activity));
//            mImageWatcherHelper.show(Collections.singletonList(Uri.parse(url)), 0);
//        } else {
        FileUtils.openFileByOtherApp(activity, Uri.parse(url), fileName, new ResultCallback() {
            @Override
            public void onFailure(Throwable e, Object e2) {
                ToastHelper.showToast(R.string.error_app_not_found_to_open_file);
            }
        });
//        }
    }

//    /**
//     * Notification system scans the specified file
//     */
//    public static void asyncScanFile(File mFile) {
//        if (mFile == null) {
//            return;
//        }
//
//        try {
//            Intent scanIntent;
//            if (mFile.isDirectory()) {
//                scanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_DIR");
//            } else {
//                scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//            }
//            scanIntent.setData(Uri.fromFile(mFile));
//            Utils.getApp().sendBroadcast(scanIntent);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * encodeSHA_256 file to Base64 String
     *
     * @param path file path
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String encodeFileToBase64(@NonNull String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        inputFile.read(buffer);
        inputFile.close();
        return Base64.encodeToString(buffer, Base64.DEFAULT);
    }

    /**
     * get file name by path
     */
    @Nullable
    public static String getFileName(@Nullable String fullname) {
        String filename = null;
        if (null != fullname) {
            fullname = fullname.trim();
            int index = fullname.lastIndexOf("/");
            filename = fullname.substring(index + 1);
        }
        return filename;
    }

    public static boolean isGifFile(@Nullable String name) {
        if (name != null) {
            name = name.toLowerCase();
            return name.toLowerCase().endsWith(".gif");
        }

        return false;
    }

    public static boolean isPictureFile(@Nullable String name) {
        if (name != null) {
            return MIMETypeUtils.isImageFile(name);
//            name = name.toLowerCase();
//            return name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".gif")
//                    || name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".bmp");
        }

        return false;
    }

    public static boolean isVideoFile(@Nullable String name) {
        if (name != null) {
            return MIMETypeUtils.isVideoFile(name);
//            name = name.toLowerCase();
//            return name.toLowerCase().endsWith(".mp4") || name.toLowerCase().endsWith(".avi") || name.toLowerCase().endsWith(".rmvb")
//                    || name.toLowerCase().endsWith(".3gp") || name.toLowerCase().endsWith(".rm") || name.toLowerCase().endsWith(".asf")
//                    || name.toLowerCase().endsWith(".wmv") || name.toLowerCase().endsWith(".flv") || name.toLowerCase().endsWith(".mov")
//                    || name.toLowerCase().endsWith(".mkv");
        }

        return false;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isPictureOrVideo(File file) {
        return isPictureFile(file.getName()) || isVideoFile(file.getName());
    }

    public static boolean isPicOrVideo(String fileName) {
        return isPictureFile(fileName) || isVideoFile(fileName);
    }


//--------------------------------在线打开文档，先下载到本地--------------------------------

/*

    public static void gotoDownload(LoginSession loginSession, final BaseActivity activity, final String url, final String fileName) {
        final ProgressDialog mProgressDialog;
        // 下载失败
        final int DOWNLOAD_ERROR = 2;
        // 下载成功
        final int DOWNLOAD_SUCCESS = 1;
        //private  File file1;
        final String savePath;
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);

        savePath = loginSession.getDownloadPath() + "/" + fileName;
       Logger.LOGD(TAG, "savePath == " + savePath);

        new Thread() {
            */
/**
 * 下载完成后  直接打开文件
 *//*

            Handler handler = new Handler(Looper.getMainLooper()) {
                public void handleMessage(android.os.Message msg) {
                    switch (msg.what) {
                        case DOWNLOAD_SUCCESS:

                            File file = (File) msg.obj;
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            String type = MIMETypeUtils.getMIMEType(fileName);
                            intent.setDataAndType(Uri.fromFile(file), type);
                            activity.startActivity(intent);
                            //Utils.getApp().startActivity(Intent.createChooser(intent, "标题"));
                            mProgressDialog.dismiss();
                            */
/**
 * 弹出选择框   把本activity销毁
 *//*

                            break;
                        case DOWNLOAD_ERROR:
                            ToastHelper.showToast(R.string.download_failed);
                            break;
                    }
                }
            };

            public void run() {
                File docFile = new File(savePath);
                if (docFile.exists()) {
                    Message msg = Message.obtain();
                    msg.obj = docFile;
                    msg.what = DOWNLOAD_SUCCESS;
                    handler.sendMessage(msg);
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.show();
                    }
                });
                File downloadfile = downLoad(activity, url, savePath, mProgressDialog);
               Logger.LOGD(TAG, "========" + savePath);
                Message msg = Message.obtain();
                if (downloadfile != null) {
                    msg.obj = downloadfile;
                    msg.what = DOWNLOAD_SUCCESS;
                } else {
                    msg.what = DOWNLOAD_ERROR;
                }
                handler.sendMessage(msg);
                mProgressDialog.dismiss();
            }
        }.start();
    }


    */

    /**
     * 传入文件 url  文件路径  和 弹出的dialog  进行 下载文档
     *//*

    @Nullable
    public static File downLoad(BaseActivity activity, String serverpath, String savePath, final ProgressDialog pd) {
        try {
            URL url = new URL(serverpath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            if (conn.getResponseCode() == 200) {
                int max = conn.getContentLength();
               Logger.LOGD(TAG, "max size = " + max);
                String size = */
    /*FileUtils.fmtFileSize(max)*//*
"KB";
                if (max <= 0) {
                    return null;
                }
                pd.setProgressNumberFormat("%1d " + size + "/%2d " + size);
                pd.setMax(max / 1024);
                InputStream is = conn.getInputStream();
                File file = new File(savePath);
                FileOutputStream fos = new FileOutputStream(file);
                int len = 0;
                byte[] buffer = new byte[1024];
                int total = 0;
                int progress = 0;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    total += Math.abs(len);
                   Logger.LOGD(TAG, "progress == " + (int) ((total * 1f / max) * 100f));
                   Logger.LOGD(TAG, "progress size = " + total + " max " + max);
                    if ((int) ((total * 1f / max) * 100f) > progress) {
                        progress = (int) ((total / max) * 100f);
                        final int finalTotal = total;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pd.setProgress(finalTotal / 1024);

                            }
                        });
                    }
                }
                fos.flush();
                fos.close();
                is.close();
                return file;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
*/


    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /**
     * 获取文件的MD5值
     *
     * @param file 文件路径
     * @return md5
     */
    @NonNull
    public static String getFileMd5(@Nullable File file) {
        MessageDigest messageDigest;
        //MappedByteBuffer byteBuffer = null;
        FileInputStream fis = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            if (file == null) {
                return "";
            }
            if (!file.exists()) {
                return "";
            }
            int len = 0;
            fis = new FileInputStream(file);
            //普通流读取方式
            byte[] buffer = new byte[1024 * 1024 * 10];
            while ((len = fis.read(buffer)) > 0) {
                //该对象通过使用 update（）方法处理数据
                messageDigest.update(buffer, 0, len);
            }
            BigInteger bigInt = new BigInteger(1, messageDigest.digest());
            String md5 = bigInt.toString(16);
            while (md5.length() < 32) {
                md5 = "0" + md5;
            }
            return md5;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                    fis = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * FileChannel 获取文件的MD5值
     *
     * @param file 文件路径
     * @return md5
     */
    @NonNull
    public static String getFileMd5ByFileChannel(@Nullable File file) {
        MessageDigest messageDigest;
        FileInputStream fis = null;
        FileChannel ch = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            if (file == null) {
                return "";
            }
            if (!file.exists()) {
                return "";
            }
            fis = new FileInputStream(file);
            ch = fis.getChannel();
            int size = 1024 * 1024 * 10;
            long part = file.length() / size + (file.length() % size > 0 ? 1 : 0);
            System.err.println("文件分片数" + part);
            for (int j = 0; j < part; j++) {
                MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, j * size, j == part - 1 ? file.length() : (j + 1) * size);
                messageDigest.update(byteBuffer);
                byteBuffer.clear();
            }
            BigInteger bigInt = new BigInteger(1, messageDigest.digest());
            String md5 = bigInt.toString(16);
            while (md5.length() < 32) {
                md5 = "0" + md5;
            }
            return md5;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                    fis = null;
                }
                if (ch != null) {
                    ch.close();
                    ch = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * RandomAccessFile 获取文件的MD5值
     *
     * @param file 文件路径
     * @return md5
     */
    @NonNull
    public static String getFileMd5ByRandomAccessFile(@Nullable File file) {
        MessageDigest messageDigest;
        RandomAccessFile randomAccessFile = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            if (file == null) {
                return "";
            }
            if (!file.exists()) {
                return "";
            }
            randomAccessFile = new RandomAccessFile(file, "r");
            byte[] bytes = new byte[1024 * 1024 * 10];
            int len = 0;
            while ((len = randomAccessFile.read(bytes)) != -1) {
                messageDigest.update(bytes, 0, len);
            }
            BigInteger bigInt = new BigInteger(1, messageDigest.digest());
            String md5 = bigInt.toString(16);
            while (md5.length() < 32) {
                md5 = "0" + md5;
            }
            return md5;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                    randomAccessFile = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * 获取下载目录
     */
    @Nullable
    public static String getDownloadDir() {
        return getDir(DOWNLOAD_DIR);
    }

    /**
     * 获取缓存目录
     */
    @Nullable
    public static String getCacheDir() {
        return getDir(CACHE_DIR);
    }

    /**
     * 获取crash目录
     */
    @Nullable
    public static String getCrashDir() {
        return getDir(CRASH_DIR);
    }

    /**
     * 获取icon目录
     */
    @Nullable
    public static String getIconDir() {
        return getDir(ICON_DIR);
    }

    /**
     * 获取应用目录，当SD卡存在时，获取SD卡上的目录，当SD卡不存在时，获取应用的cache目录
     */
    public static String getDir(String name) {

        StringBuilder sb = new StringBuilder();
        if (isExternalStorageWritable()) {
            sb.append(getExternalStoragePath());
        } else {
            sb.append(getCachePath());
        }
        sb.append(name);
        sb.append(File.separator);
        String path = sb.toString();
        if (createDirs(path)) {
            return path;
        } else {
            return null;
        }
    }

    /**
     * 获取SD下的应用目录
     */
    public static String getExternalStoragePath() {
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        sb.append(File.separator);
        sb.append(ROOT_DIR);
        sb.append(File.separator);
        return sb.toString();
    }

    /**
     * 获取应用的cache目录
     */
    public static String getCachePath() {
        File f = Utils.getApp().getCacheDir();
        if (null == f) {
            return null;
        } else {
            return f.getAbsolutePath() + "/";
        }
    }

    /**
     * 创建文件夹
     */
    public static boolean createDirs(@NonNull String dirPath) {
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory()) {
            return file.mkdirs();
        }
        return true;
    }

    /**
     * 复制文件，可以选择是否删除源文件
     */
    public static boolean copyFile(@NonNull String srcPath, @NonNull String destPath,
                                   boolean deleteSrc) {
        File srcFile = new File(srcPath);
        File destFile = new File(destPath);
        return copyFile(srcFile, destFile, deleteSrc);
    }

    /**
     * 复制文件，可以选择是否删除源文件
     */
    public static boolean copyFile(File srcFile, @NonNull File destFile,
                                   boolean deleteSrc) {
        if (!srcFile.exists() || !srcFile.isFile()) {
            return false;
        }
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            int i = -1;
            while ((i = in.read(buffer)) > 0) {
                out.write(buffer, 0, i);
                out.flush();
            }
            if (deleteSrc) {
                srcFile.delete();
            }
        } catch (Exception e) {
            LogUtils.e(e);
            return false;
        } finally {
            IOUtils.close(out);
            IOUtils.close(in);
        }
        return true;
    }

    /**
     * 判断文件是否可写
     */
    public static boolean isWriteable(@NonNull String path) {
        try {
            if (TextUtils.isEmpty(path)) {
                return false;
            }
            File f = new File(path);
            return f.exists() && f.canWrite();
        } catch (Exception e) {
            LogUtils.e(e);
            return false;
        }
    }

    /**
     * 修改文件的权限,例如"777"等
     */
    public static void chmod(String path, String mode) {
        try {
            String command = "chmod " + mode + " " + path;
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(command);
        } catch (Exception e) {
            LogUtils.e(e);
        }
    }

    /**
     * 把数据写入文件
     *
     * @param is       数据流
     * @param path     文件路径
     * @param recreate 如果文件存在，是否需要删除重建
     * @return 是否写入成功
     */
    public static boolean writeFile(@Nullable InputStream is, @NonNull String path,
                                    boolean recreate) {
        boolean res = false;
        File f = new File(path);
        FileOutputStream fos = null;
        try {
            if (recreate && f.exists()) {
                f.delete();
            }
            if (!f.exists() && null != is) {
                File parentFile = new File(f.getParent());
                parentFile.mkdirs();
                int count = -1;
                byte[] buffer = new byte[1024];
                fos = new FileOutputStream(f);
                while ((count = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, count);
                }
                res = true;
            }
        } catch (Exception e) {
            LogUtils.e(e);
        } finally {
            IOUtils.close(fos);
            IOUtils.close(is);
        }
        return res;
    }

    /**
     * 把字符串数据写入文件
     *
     * @param content 需要写入的字符串
     * @param path    文件路径名称
     * @param append  是否以添加的模式写入
     * @return 是否写入成功
     */
    public static boolean writeFile(@NonNull byte[] content, @NonNull String path, boolean append) {
        boolean res = false;
        File f = new File(path);
        RandomAccessFile raf = null;
        try {
            if (f.exists()) {
                if (!append) {
                    f.delete();
                    f.createNewFile();
                }
            } else {
                f.createNewFile();
            }
            if (f.canWrite()) {
                raf = new RandomAccessFile(f, "rw");
                raf.seek(raf.length());
                raf.write(content);
                res = true;
            }
        } catch (Exception e) {
            LogUtils.e(e);
        } finally {
            IOUtils.close(raf);
        }
        return res;
    }

    /**
     * 把字符串数据写入文件
     *
     * @param content 需要写入的字符串
     * @param path    文件路径名称
     * @param append  是否以添加的模式写入
     * @return 是否写入成功
     */
    public static boolean writeFile(String content, @NonNull String path, boolean append) {
        return writeFile(content.getBytes(), path, append);
    }

    /**
     * 把键值对写入文件
     *
     * @param filePath 文件路径
     * @param key      键
     * @param value    值
     * @param comment  该键值对的注释
     */
    public static void writeProperties(@NonNull String filePath, String key,
                                       String value, String comment) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(filePath)) {
            return;
        }
        FileInputStream fis = null;
        FileOutputStream fos = null;
        File f = new File(filePath);
        try {
            if (!f.exists() || !f.isFile()) {
                f.createNewFile();
            }
            fis = new FileInputStream(f);
            Properties p = new Properties();
            p.load(fis);// 先读取文件，再把键值对追加到后面
            p.setProperty(key, value);
            fos = new FileOutputStream(f);
            p.store(fos, comment);
        } catch (Exception e) {
            LogUtils.e(e);
        } finally {
            IOUtils.close(fis);
            IOUtils.close(fos);
        }
    }

    /**
     * 根据值读取
     */
    public static String readProperties(@NonNull String filePath, String key,
                                        String defaultValue) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(filePath)) {
            return null;
        }
        String value = null;
        FileInputStream fis = null;
        File f = new File(filePath);
        try {
            if (!f.exists() || !f.isFile()) {
                f.createNewFile();
            }
            fis = new FileInputStream(f);
            Properties p = new Properties();
            p.load(fis);
            value = p.getProperty(key, defaultValue);
        } catch (IOException e) {
            LogUtils.e(e);
        } finally {
            IOUtils.close(fis);
        }
        return value;
    }

    /**
     * 把字符串键值对的map写入文件
     */
    public static void writeMap(@NonNull String filePath, @Nullable Map<String, String> map,
                                boolean append, String comment) {
        if (map == null || map.size() == 0 || TextUtils.isEmpty(filePath)) {
            return;
        }
        FileInputStream fis = null;
        FileOutputStream fos = null;
        File f = new File(filePath);
        try {
            if (!f.exists() || !f.isFile()) {
                f.createNewFile();
            }
            Properties p = new Properties();
            if (append) {
                fis = new FileInputStream(f);
                p.load(fis);// 先读取文件，再把键值对追加到后面
            }
            p.putAll(map);
            fos = new FileOutputStream(f);
            p.store(fos, comment);
        } catch (Exception e) {
            LogUtils.e(e);
        } finally {
            IOUtils.close(fis);
            IOUtils.close(fos);
        }
    }

    /**
     * 把字符串键值对的文件读入map
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Map<String, String> readMap(@NonNull String filePath,
                                              String defaultValue) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        Map<String, String> map = null;
        FileInputStream fis = null;
        File f = new File(filePath);
        try {
            if (!f.exists() || !f.isFile()) {
                f.createNewFile();
            }
            fis = new FileInputStream(f);
            Properties p = new Properties();
            p.load(fis);
            map = new HashMap<String, String>((Map) p);// 因为properties继承了map，所以直接通过p来构造一个map
        } catch (Exception e) {
            LogUtils.e(e);
        } finally {
            IOUtils.close(fis);
        }
        return map;
    }

    /**
     * 改名
     */
    public static boolean copy(@NonNull String src, @NonNull String des, boolean delete) {
        File file = new File(src);
        if (!file.exists()) {
            return false;
        }
        File desFile = new File(des);
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(file);
            out = new FileOutputStream(desFile);
            byte[] buffer = new byte[1024];
            int count = -1;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
                out.flush();
            }
        } catch (Exception e) {
            LogUtils.e(e);
            return false;
        } finally {
            IOUtils.close(in);
            IOUtils.close(out);
        }
        if (delete) {
            file.delete();
        }
        return true;
    }

    /**
     * 根据文件的名称判断文件的Mine值
     */
    public static String getMediaType(String fileName) {
        FileNameMap map = URLConnection.getFileNameMap();
        String contentTypeFor = map.getContentTypeFor(fileName);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }

    public static boolean putDiskCache(@NonNull String key, String value) {
        File directory = new File(getCachePath() + "httpNas");
        boolean success = true;
        if (!directory.exists()) {
            success = directory.mkdir();
        }
        DiskLruCache diskLruCache = null;
        try {
            Logger.LOGD(TAG, "putDiskCache -->" + Thread.currentThread());
            diskLruCache = DiskLruCache.open(directory, AppConstants.DISK_LRU_CACHE_APP_VERSION, 1, 200 * 1000 * 1000);
            String keyMD5 = Md5Utils.encode(key);
            DiskLruCache.Editor editor = diskLruCache.edit(keyMD5);
            if (editor != null) {
                editor.set(0, value);
                editor.commit();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (diskLruCache != null) {
                try {
                    diskLruCache.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @MainThread
    @NonNull
    public static <T> LiveData<T> loadDiskCache(@NonNull String key) {
        MutableLiveData<T> mutableLiveData = new MutableLiveData();
        AppExecutors.Companion.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                String diskCache = getDiskCache(key);
                Timber.d("diskCache : %s", diskCache);
                @Nullable T value = EmptyUtils.isEmpty(diskCache) ?
                        null
                        : GsonUtils.decodeJSONCatchException(diskCache, new TypeToken<T>() {
                }.getType());
                mutableLiveData.postValue(value);
            }
        });
        return mutableLiveData;
    }

    public static String getDiskCache(@NonNull String key) {
        File directory = new File(getCachePath() + "httpNas");
        if (directory.exists()) {
            DiskLruCache diskLruCache = null;
            try {
                Logger.LOGD(TAG, "getDiskCache -->" + Thread.currentThread());
                diskLruCache = DiskLruCache.open(directory, AppConstants.DISK_LRU_CACHE_APP_VERSION, 1, 200 * 1000 * 1000);
                String keyMD5 = Md5Utils.encode(key);
                DiskLruCache.Snapshot snapshot = diskLruCache.get(keyMD5);
                if (snapshot != null) {
                    return snapshot.getString(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (diskLruCache != null) {
                    try {
                        diskLruCache.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }


    public static boolean pathIsParent(String parentPath, String childPath) {
        if (parentPath != null && childPath != null &&
                parentPath.length() < childPath.length()) {
            while (childPath.lastIndexOf(separator) > 0) {
                childPath = childPath.substring(0, childPath.lastIndexOf(separator));
                if (Objects.equals(parentPath, childPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean pathEquals(String path, String path2) {
        return Objects.equals(path, path2);
    }

    public static boolean pathEqualsIgnoreLastSeparator(String path, String path2) {
        if (Objects.equals(path, path2)) {
            return true;
        }
        if (path == null || path2 == null) {
            return false;
        }
        boolean pathEndWithSeparator = path.endsWith(File.separator);
        boolean path2EndWithSeparator = path2.endsWith(File.separator);
        if (pathEndWithSeparator != path2EndWithSeparator) {
            if (!path2EndWithSeparator) {
                path2 = path2 + File.separator;
            }
            if (!pathEndWithSeparator) {
                path = path + File.separator;
            }
            return Objects.equals(path, path2);
        } else {
            return false;
        }

    }

    public static boolean pathIsPrefix(String keyPath, String prefix) {
        if (Objects.equals(keyPath, prefix)) return true;
        if (keyPath != null && prefix != null) {
            if (Objects.equals("/", prefix) || Objects.equals("public/", prefix)) {
                return keyPath.startsWith(prefix);
            }
            while (prefix.length() > 1 && prefix.endsWith(separator)) {
                prefix = prefix.substring(0, prefix.length() - 1);
            }
            if (keyPath.length() > prefix.length()) {
                while (keyPath.lastIndexOf(separator) > 0) {
                    keyPath = keyPath.substring(0, keyPath.lastIndexOf(separator));
                    if (Objects.equals(keyPath, prefix)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static long getCacheSize(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            return getCacheSizeByAndroidO(context, context.getPackageName());
        } else {
            final File cacheDir = context.getCacheDir();
            Logger.LOGD(TAG, "cacheDir :" + cacheDir.getAbsolutePath());
            final long appCacheDirSize = getDirSize(cacheDir.getAbsolutePath());
            final long extCacheDirSize = isExternalStorageWritable()
                    && context.getExternalCacheDir() != null
                    && context.getExternalCacheDir().exists()
                    ? getDirSize(context.getExternalCacheDir().getAbsolutePath())
                    : 0;
            return appCacheDirSize + extCacheDirSize;
        }
    }

    private static long getDirSize(String absolutePath) {
        File file = new File(absolutePath);
        long dirSize = 0;
        if (file.exists()) {
            if (file.isDirectory()) {
                final File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    for (File subFile : files) {
                        if (subFile.isDirectory()) {
                            dirSize += getDirSize(subFile.getAbsolutePath());
                        } else {
                            dirSize += subFile.length();
                        }
                    }
                }
            } else {
                dirSize += file.length();
            }
        }
        return dirSize;
    }

    public static long getStorageSize(String path) {
        long size;
        try {
            StatFs stat = new StatFs(path);
            size = stat.getTotalBytes();
        } catch (Throwable e) {
            size = -1; // system error computing the available storage size
            e.printStackTrace();
        }
        return size;
    }

    public static long getAvailableStorageSize(String path) {
        long size;
        try {
            StatFs stat = new StatFs(path);
            size = stat.getAvailableBytes();
        } catch (Throwable e) {
            size = -1; // system error computing the available storage size
            e.printStackTrace();
        }
        return size;
    }

    @MainThread
    public static void clearCache(Application application, Callback<Boolean> consumer) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    deleteDir(Utils.getApp().getCacheDir());
                    if (isExternalStorageWritable()) {
                        final File[] externalCacheDirs = Utils.getApp().getExternalCacheDirs();
                        if (externalCacheDirs != null)
                            for (File externalCacheDir : externalCacheDirs) {
                                deleteFilesInDir(externalCacheDir);
                            }
                    }
                    if (consumer != null) {
                        consumer.result(true);
                    }
                    DynamicDBHelper.INSTANCE(application).clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }


    public static boolean deleteFilesInDir(final File dir) {
        if (dir == null) return false;
        // dir doesn't exist then return true
        if (!dir.exists()) return true;
        // dir isn't a directory then return false
        if (!dir.isDirectory()) return false;
        File[] files = dir.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                if (file.isFile()) {
                    if (!file.delete()) return false;
                } else if (file.isDirectory()) {
                    if (!deleteDir(file)) return false;
                }
            }
        }
        return true;
    }

    public static boolean deleteDir(final File dir) {
        if (dir == null) return false;
        // dir doesn't exist then return true
        if (!dir.exists()) return true;
        // dir isn't a directory then return false
        if (!dir.isDirectory()) return false;
        File[] files = dir.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                if (file.isFile()) {
                    if (!file.delete()) return false;
                } else if (file.isDirectory()) {
                    if (!deleteDir(file)) return false;
                }
            }
        }
        return dir.delete();
    }

    public static boolean isApkFile(@NotNull String name) {
        return name.endsWith(".apk");
    }

    @Nullable
    public static String getParentPath(@NotNull String path) {
        if (path.endsWith(File.separator)) {
            path = path.substring(0, path.length() - 1);
        }
        int startIndex = path.lastIndexOf(File.separator) + 1;
        return path.substring(0, startIndex);
    }
}
