package net.sdvn.nascommon.model.oneos.comp;

import net.sdvn.nascommon.model.oneos.OneOSFile;

import java.util.Comparator;

/**
 * Comparator for File Name
 */
public class OneOSFileTimeComparator implements Comparator<OneOSFile> {

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

        if (file1.getTime() < file2.getTime()) {
            return 1;
        } else if (file1.getTime() > file2.getTime()) {
            return -1;
        } else {
            return 0;
        }
    }
}