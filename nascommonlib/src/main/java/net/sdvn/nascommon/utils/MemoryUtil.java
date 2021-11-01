package net.sdvn.nascommon.utils;

import android.os.StatFs;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MemoryUtil {

    public static final String SELF_DIR_NAME = "self";
    public static final String EMULATED_DIR_NAME = "emulated";
    public static final String EMULATED_DIR_KNOX = "knox-emulated";
    public static final String SDCARD0_DIR_NAME = "sdcard0";
    public static final String CONTAINER = "container";
    private static final String ERROR = "error";

    public boolean isExternalStoragePresent() {
        return getStorageListSize() != 1;
    }

    /**
     * Returns an the number of the files inside '/storage' directory
     *
     * @return int - number of storages present except the redundant once
     */
    public int getStorageListSize() {
        File storageDir = new File("/storage");
        List<File> volumeList = new ArrayList<>();
        Collections.addAll(volumeList, storageDir.listFiles());
        // segregate the list
        final Iterator<File> iterator = volumeList.iterator();
        while (iterator.hasNext()) {
            final File next = iterator.next();
            if (next != null) {
                String storageName = next.getName();
                if ((SELF_DIR_NAME).equals(storageName) ||
                        EMULATED_DIR_NAME.equals(storageName) ||
                        EMULATED_DIR_KNOX.equals(storageName) ||
                        SDCARD0_DIR_NAME.equals(storageName) ||
                        CONTAINER.equals(storageName)) {
                    iterator.remove();
                }
            }
        }
        return volumeList.size();
    }


    /**
     * calculate available/free size of any directory
     *
     * @param path path of the storage
     * @return size in bytes
     */
    public long getAvailableMemorySize(@NonNull String path) {
        File file = new File(path);
        StatFs stat = new StatFs(file.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    /**
     * calculate total size of any directory
     *
     * @param path path of the storage
     * @return size in bytes
     */
    public long getTotalMemorySize(@NonNull String path) {
        File file = new File(path);
        StatFs stat = new StatFs(file.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    /**
     * mainly to format the available bytes into user readable string
     *
     * @param size long - value gained from the getTotalMemorySize() and getAvailableMemorySize()
     *             using StatFs
     * @return a formatted string with KiB, MiB, GiB name
     */
    @NonNull
    public String formatSize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = "KiB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MiB";
                size /= 1024;
                if (size >= 1024) {
                    suffix = "GiB";
                    size /= 1024;
                }
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    public long suffixedSize(long size, @NonNull String suffix) {

        switch (suffix) {
            case "KiB":
                return size / 1024;
            case "MiB":
                return (long) (size / Math.pow(1024, 2));
            case "GiB":
                return (long) (size / Math.pow(1024, 3));
            default:
                return 0;
        }
    }
}