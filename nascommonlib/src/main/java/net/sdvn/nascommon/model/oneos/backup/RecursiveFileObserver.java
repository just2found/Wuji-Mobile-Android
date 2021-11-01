package net.sdvn.nascommon.model.oneos.backup;

import android.os.FileObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.db.objecbox.BackupFile;
import net.sdvn.nascommon.utils.FileUtils;
import net.sdvn.nascommon.utils.log.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

public class RecursiveFileObserver /*extends FileObserver*/ {
    private static final String TAG = RecursiveFileObserver.class.getSimpleName();
    /**
     * events observer for backup photos
     */
    public static final int EVENTS_BACKUP_PHOTOS = FileObserver.CREATE | FileObserver.MOVED_TO;

    private Set<SingleFileObserver> mObservers = null;
    private OnObserverCallback mCallback = null;
    //    private BackupFile backupInfo;
//    private String mPath = null;
    private int mMask;
    private Set<BackupFile> backupFiles;

    public RecursiveFileObserver(List<BackupFile> backupFiles, int mask, OnObserverCallback mCallback) {
//        super(path, mask);
//        this.backupInfo = backupInfo;
//        this.mPath = path;
        this.mMask = mask;
        this.mCallback = mCallback;
        this.backupFiles = new HashSet<>();
        this.backupFiles.addAll(backupFiles);
    }

    public void startWatching() {
        if (mObservers != null)
            return;

        mObservers = new HashSet<>();
        for (BackupFile backupFile : backupFiles) {
            addSingleFileObserver(mObservers, backupFile.getPath(), false);
        }
//        addSingleFileObserver(mObservers, mPath, false);
    }

    public void addWatching(BackupFile backupFile) {
        this.backupFiles.add(backupFile);
        if (mObservers != null) {
            addSingleFileObserver(mObservers, backupFile.getPath(), false);
        }
    }

    public void stopWatching() {
        if (mObservers == null)
            return;

        for (SingleFileObserver sfo : mObservers) {
            sfo.stopWatching();
        }

        mObservers.clear();
        mObservers = null;
    }

    public void onEvent(int event, String path) {
        event = event & FileObserver.ALL_EVENTS;

        switch (event) {
            // case FileObserver.ACCESS:
            // Logd.d("RecursiveFileObserver", "ACCESS: " + path);
            // break;
            // case FileObserver.ATTRIB:
            // Logd.d("RecursiveFileObserver", "ATTRIB: " + path);
            // break;
            // case FileObserver.CLOSE_NOWRITE:
            // Logd.d("RecursiveFileObserver", "CLOSE_NOWRITE: " + path);
            // break;
            // case FileObserver.CLOSE_WRITE:
            // Logd.d("RecursiveFileObserver", "CLOSE_WRITE: " + path);
            // break;
            // case FileObserver.DELETE:
            // Logd.d("RecursiveFileObserver", "REMOVE: " + path);
            // break;
            // case FileObserver.DELETE_SELF:
            // Logd.d("RecursiveFileObserver", "DELETE_SELF: " + path);
            // break;
            // case FileObserver.MODIFY:
            // Logd.d("RecursiveFileObserver", "MODIFY: " + path);
            // break;
            // case FileObserver.MOVE_SELF:
            // Logd.d("RecursiveFileObserver", "MOVE_SELF: " + path);
            // break;
            // case FileObserver.MOVED_FROM:
            // Logd.d("RecursiveFileObserver", "MOVED_FROM: " + path);
            // break;
            case FileObserver.CREATE:
                Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, "RecursiveFileObserver", "CREATE: " + path);
                onCreateDir(path);
                break;
            case FileObserver.MOVED_TO:
                Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, "RecursiveFileObserver", "MOVED_TO: " + path);
                onCreateDir(path);
                break;
            // case FileObserver.OPEN:
            // Logd.d("RecursiveFileObserver", "OPEN: " + path);
            // break;
            // default:
            // Logd.d("RecursiveFileObserver", "DEFAULT(" + event + "): " + path);
            // break;
        }
    }

    private void addSingleFileObserver(@NonNull Set<SingleFileObserver> mObserverList, String rootPath,
                                       boolean isCallback) {
        List<SingleFileObserver> addObservers = new ArrayList<SingleFileObserver>();

        Stack<String> stack = new Stack<String>();
        stack.push(rootPath);

        while (!stack.isEmpty()) {
            String parent = stack.pop();
            addObservers.add(new SingleFileObserver(parent, mMask));
            File path = new File(parent);
            if (path.isDirectory()) {
                File[] files = path.listFiles();
                if (null == files)
                    continue;
                for (File file : files) {
                    if (file.isDirectory() && !file.getName().startsWith(".")
                            && !file.getName().equals("..")) {
                        stack.push(file.getPath());
                    } else if (isCallback) {
                        callback(file);
                    }
                }
            } else if (isCallback) {
                callback(path);
            }
        }

        for (SingleFileObserver observer : addObservers) {
            observer.startWatching();
        }

        mObserverList.addAll(addObservers);
    }

    private void onCreateDir(String path) {
        addSingleFileObserver(mObservers, path, true);
    }

    private void callback(@Nullable File file) {
        if (null == mCallback || null == file || !file.isFile() || !FileUtils.isPictureOrVideo(file)) {
            return;
        }
        for (BackupFile backupFile : backupFiles) {
            if (file.getAbsolutePath().startsWith(backupFile.getPath())) {
                mCallback.onAdd(backupFile, file);
            }
        }
//        mCallback.onAdd(backupInfo, file);
        Logger.p(Logger.Level.DEBUG, Logger.Logd.DEBUG, TAG, "Callback to Add file: " + file.getAbsolutePath());
    }

    /**
     * Monitor single directory and dispatch activeUsers events to its parent, with full path.
     *
     * @author uestc.Mobius <mobius@toraleap.com>
     * @version 2011.0121
     */
    class SingleFileObserver extends FileObserver {
        private String mRootPath;

        public SingleFileObserver(String path) {
            this(path, ALL_EVENTS);
            mRootPath = path;
        }

        public SingleFileObserver(String path, int mask) {
            super(path, mask);
            mRootPath = path;
        }

        @Override
        public void onEvent(int event, String path) {
            String newPath = mRootPath + "/" + path;
            RecursiveFileObserver.this.onEvent(event, newPath);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SingleFileObserver that = (SingleFileObserver) o;
            return Objects.equals(mRootPath, that.mRootPath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mRootPath);
        }
    }

    public interface OnObserverCallback {
        void onAdd(BackupFile backupInfo, File file);
    }
}