package net.sdvn.nascommon.model;

import androidx.annotation.NonNull;

public class FileListChangeObserver {

    private static class FileListChangeObserverHolder {
        @NonNull
        private static FileListChangeObserver instance = new FileListChangeObserver();
    }

    /**
     * 私有的构造函数
     */
    private FileListChangeObserver() {
    }

    @NonNull
    public static FileListChangeObserver getInstance() {
        return FileListChangeObserverHolder.instance;
    }

    private long changeTime;

    public void FileListChange() {
        changeTime = System.currentTimeMillis();
    }

    public boolean hadNewFileList(long lastChangeTime) {
        return lastChangeTime <= changeTime || System.currentTimeMillis() - lastChangeTime >= 10 * 60 * 1000;
    }
}
