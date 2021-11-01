package net.sdvn.nascommon.model.phone;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import net.sdvn.nascommon.model.phone.comp.FileDateComparator;
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommon.utils.IOUtils;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LocalSortTask extends AsyncTask<Integer, Integer, String[]> implements LifecycleObserver {
    private static final String TAG = LocalSortTask.class.getSimpleName();
    public static final String IMAGE = "image";
    public static final String VIDEO = "video";
    public static final String GIF = "gif";
    @Nullable
    private LinkedList<String> mExtensionList = null;
    private WeakReference<Context> mActivityWeakReference;
    private LocalFileType type;
    @Nullable
    private String filter;
    @NonNull
    private List<LocalFile> mFileList = new ArrayList<>();
    @NonNull
    private List<String> mSectionList = new ArrayList<>();
    private onLocalSortListener mListener;
    private String fmtDate;
    private Lifecycle lifecycle;

    public LocalSortTask(@NonNull Context context, @Nullable LifecycleOwner lifecycleOwner, @NonNull LocalFileType type,
                         @org.jetbrains.annotations.Nullable String filter, onLocalSortListener mListener) {
        mActivityWeakReference = new WeakReference<>(context);
        if (lifecycleOwner != null) {
            setLifecycle(lifecycleOwner.getLifecycle());
        }
        this.type = type;
        //文件名筛选
        this.filter = filter;
        //开始和完成的回调
        this.mListener = mListener;
        fmtDate = context.getResources().getString(R.string.fmt_time_line);
    }

    public void setLifecycle(Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
        this.lifecycle.addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void unregister() {
        Logger.p(Logger.Level.INFO, Logger.Logd.DEBUG, TAG, "Lifecycle.Event.ON_DESTROY");
        cancel(true);
        if (mActivityWeakReference != null)
            mActivityWeakReference.clear();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mListener.onStart(type);
        mFileList.clear();
        mSectionList.clear();
    }

    @Nullable
    @Override
    protected String[] doInBackground(Integer... params) {
        try {
            getExtension(type);
            getSortList(type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String[] result) {
        super.onPostExecute(result);
        if (mListener != null) {
            mListener.onComplete(type, mFileList, mSectionList);
        }
    }

    private static final String[] PROJECTIONS_VIDEO = new String[]{
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Video.Media.DURATION,
    };
    private static final String[] PROJECTIONS_IMAGE = new String[]{
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.Images.Media.DATE_TAKEN
    };
    private static final String[] PROJECTIONS = new String[]{
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATA
    };

    private void getSortList(LocalFileType type) throws Exception {
        if (mActivityWeakReference.get() == null) {
            Logger.LOGE(TAG, "getSortList getActivity is null");
            return;
        }
        Logger.LOGD("{TIME}", "begin: " + System.currentTimeMillis());
        ContentResolver mResolver = mActivityWeakReference.get().getApplicationContext().getContentResolver();
        int column_index = 0;
        Cursor cursor;
        boolean isMedia = true;
        String selectionSize = MediaStore.MediaColumns.SIZE + "> " + 0;
        boolean isVideo = false;
        boolean isImage = false;
        if (type == LocalFileType.AUDIO) {
            cursor = mResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, PROJECTIONS,
                    selectionSize, null, MediaStore.Files.FileColumns.DATE_MODIFIED + " desc");
            if (cursor != null) {
                column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            }
        } else if (type == LocalFileType.VIDEO) {
            cursor = mResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, PROJECTIONS_VIDEO,
                    selectionSize, null, MediaStore.Files.FileColumns.DATE_MODIFIED + " desc");
            if (cursor != null) {
                column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            }
            isVideo = true;
        } else if (type == LocalFileType.PICTURE) {
            cursor = mResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, PROJECTIONS_IMAGE,
                    selectionSize, null, MediaStore.Files.FileColumns.DATE_MODIFIED + " desc");
            if (cursor != null) {
                column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            }
            isImage = true;
        } else {
            Uri uri = MediaStore.Files.getContentUri("external");
            String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE + " AND " + selectionSize;
            cursor = mResolver.query(uri, PROJECTIONS, selection, null,
                    MediaStore.Files.FileColumns.DATE_MODIFIED + " desc");
            if (cursor != null) {
                column_index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
            }
            isMedia = false;
        }
        Logger.LOGD("{TIME}", "end: " + System.currentTimeMillis());

        if (cursor == null) {
            return;
        } else if (cursor.getCount() == 0) {
            IOUtils.close(cursor);
            return;
        } else if (cursor.moveToFirst()) {
            final int idCol = cursor.getColumnIndex(MediaStore.MediaColumns._ID);
            final int pathCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
            final int nameCol = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
            final int dateCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED);
            final int mimeTypeCol = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE);
            final int sizeCol = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE);
            int widthCol = -1;
            int heightCol = -1;
            int dateTakenCol = -1;
            if (isImage || isVideo) {
                widthCol = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH);
                heightCol = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT);
                dateTakenCol = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            }
            int durationCol = -1;
            if (isVideo) {
                durationCol = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);
            }
            do {
                final long id = cursor.getLong(idCol);
                final String typeMime = cursor.getString(mimeTypeCol);
                final long size = cursor.getLong(sizeCol);
                final String name = cursor.getString(nameCol);
                final long dateTime = cursor.getLong(dateCol) * 1000;

                final String path;

                long dateTaken = 0;
                int width = 0;
                int height = 0;
                if (isImage || isVideo) {
                    dateTaken = cursor.getLong(dateTakenCol);
                    width = cursor.getInt(widthCol);
                    height = cursor.getInt(heightCol);
                }
                long duration = 0;
                if (isVideo) {
                    duration = cursor.getLong(durationCol);
                }
                LocalFile file;
//                if (android.os.Build.VERSION.SDK_INT < 29) {
                path = cursor.getString(pathCol);
                file = new LocalFile(new File(path));
//                } else {
//                    final Uri contentUri;
//                    if (isImage || isGif) {
//                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                    } else if (isVideo) {
//                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//                    } else {
//                        contentUri = MediaStore.Files.getContentUri("external");
//                    }
//
//                    path = contentUri.buildUpon().appendPath(String.valueOf(id)).build().toString();
//                    file = new LocalFile(path);
//                }
                if ((!TextUtils.isEmpty(filter) && !name.contains(filter))
                        || name.startsWith(".")
                        || size <= 0) {
                    continue;
                }
                file.setIsfile(true);
                file.setLastModifyTime(dateTime);
                if (isMedia) {
                    /** get the video/audio/image files */
//                    Logger.LOGD("{TIME}", "1 path=", path, " date=", dateTaken, " duration=", duration, "pathCol=", cursor.getString(pathCol));
                    if (dateTaken > 0) {
                        file.setDate(dateTaken);
                    } else {
                        file.setDate(dateTime);
                    }
                    file.setPicOrVideo(isVideo || isImage);
                    file.setDuration(duration);
                    mFileList.add(file);
                } else {
                    /** get other type files */
                    if (name.contains(".")) {
                        String extension = name.substring(name.lastIndexOf(".")).toUpperCase();
                        if (null == mExtensionList || mExtensionList.contains(extension)) {
                            file.setDate(dateTime);
                            mFileList.add(file);
                        }
                    }
                }

            } while (cursor.moveToNext());
        }
        IOUtils.close(cursor); // needs to close cursor

        Logger.LOGD("{TIME}", "2: " + System.currentTimeMillis());
//            if (type != LocalFileType.PICTURE) {
//            if (type == LocalFileType.PICTURE) {
//                for (LocalFile f : mFileList) {
//                    File file = f.getFile();
//                    long date = getPhotoDate(file);
//                    f.setDate(date);
//                }
//            } else {
//                for (LocalFile f : mFileList) {
//                    File file = f.getFile();
//                    f.setDate(file.lastModified());
//                }
//            }

        Logger.LOGD("{TIME}", "3: " + System.currentTimeMillis());
        if (type != LocalFileType.PRIVATE && type != LocalFileType.DOWNLOAD) {
            //按Date大小排序
            Collections.sort(mFileList, new FileDateComparator());
            String tmpDate = "";
            int section = -1;
            for (int i = 0; i < mFileList.size(); i++) {
                LocalFile file = mFileList.get(i);
                file.getTime();
                String date = FileUtils.formatTime(file.getDate(), fmtDate);
                if (!tmpDate.equals(date)) {
                    tmpDate = date;
                    section++;
                    mSectionList.add(date);
                }
                file.setSection(section);
            }
        }
        Logger.LOGD("{TIME}", "4: " + System.currentTimeMillis());
    }
//        }


    private boolean fileFilter(LocalFile file) {
        return file.exists() && file.isFile()
                && file.length() > 0 && !file.isHidden();
    }

    private LinkedList<String> getExtension(LocalFileType type) {
        if (type == LocalFileType.PRIVATE || type == LocalFileType.DOWNLOAD) {
            return null;
        }

        mExtensionList = new LinkedList<>();
        if (type == LocalFileType.AUDIO) {
            mExtensionList.add(".MP3");
            mExtensionList.add(".WMA");
            mExtensionList.add(".WAV");
            mExtensionList.add(".AAC");
            mExtensionList.add(".APE");
            mExtensionList.add(".FLAC");
        } else if (type == LocalFileType.VIDEO) {
            mExtensionList.add(".AVI");
            mExtensionList.add(".ASF");
            mExtensionList.add(".WMV");
            mExtensionList.add(".3GP");
            mExtensionList.add(".FLV");
            mExtensionList.add(".RMVB");
            mExtensionList.add(".RM");
            mExtensionList.add(".MP4");
            mExtensionList.add(".MKV");
        } else if (type == LocalFileType.PICTURE) {
            mExtensionList.add(".BMP");
            mExtensionList.add(".JPEG");
            mExtensionList.add(".JPG");
            mExtensionList.add(".PNG");
            mExtensionList.add(".GIF");
            mExtensionList.add(".HEIC");
        } else if (type == LocalFileType.DOC) {
            mExtensionList.add(".DOC");
            mExtensionList.add(".XLS");
            mExtensionList.add(".TXT");
            mExtensionList.add(".PPT");
            mExtensionList.add(".PDF");
            mExtensionList.add(".NUMBERS");
            mExtensionList.add(".PAGES");
        } else if (type == LocalFileType.ZIP) {
            mExtensionList.add(".RAR");
            mExtensionList.add(".TAR");
            mExtensionList.add(".TAR.GZ");
            mExtensionList.add(".TAR.BZ2");
            mExtensionList.add(".7Z");
            mExtensionList.add(".ISO");
            mExtensionList.add(".JAR");
            mExtensionList.add(".ZIP");
        } else if (type == LocalFileType.APP) {
            mExtensionList.add(".APK");
        }

        return mExtensionList;
    }

    /**
     * get photo date
     *
     * @param file
     * @return photo date
     */
    private long getPhotoDate(@NonNull File file) {
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            if (exif != null) {
                String dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
                if (dateTime != null) {
                    return FileUtils.parseFmtTime(dateTime, "yyyy:MM:dd HH:mm:ss");
                } else {
                    return file.lastModified();
                }
            }
        } catch (Exception e) {
            Logger.LOGE(TAG, "Get Photo Date Exception", e);
            return file.lastModified();
        }

        return 0;
    }

    public interface onLocalSortListener {
        void onStart(LocalFileType type);

        void onComplete(LocalFileType type, List<LocalFile> fileList, List<String> sectionList);
    }
}