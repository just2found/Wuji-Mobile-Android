package net.sdvn.nascommon.model.oneos.comp;

import net.sdvn.nascommon.model.oneos.OneOSFile;

import java.util.Comparator;

/**
 * Comparator for File Name
 */
public class OneOSFileTimeComparatorV2 implements Comparator<OneOSFile> {
    private boolean mIsAsc;

    public OneOSFileTimeComparatorV2(boolean isAsc) {
        mIsAsc = isAsc;
    }

    @Override
    public int compare(OneOSFile file1, OneOSFile file2) {
        if (file1 == null || file2 == null) {
            return 0;
        }
        if (file1.isDirectory() && !file2.isDirectory()) {
            return -1;
        }

        if (!file1.isDirectory() && file2.isDirectory()) {
            return 1;
        }
        if (mIsAsc) {
            return Long.compare(file1.getTime(), file2.getTime());
        } else {
            return Long.compare(file2.getTime(), file1.getTime());
        }

    }
}