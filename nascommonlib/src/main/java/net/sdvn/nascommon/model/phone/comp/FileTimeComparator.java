package net.sdvn.nascommon.model.phone.comp;

import androidx.annotation.NonNull;

import net.sdvn.nascommon.model.oneos.DataFile;

import java.util.Comparator;
import java.util.Objects;

/**
 * Comparator for File LastModified
 */
public class FileTimeComparator implements Comparator<DataFile> {

    @Override
    public int compare(@NonNull DataFile lf1, @NonNull DataFile lf2) {
        String file1 = lf1.getPath();
        String file2 = lf2.getPath();
        if (Objects.equals(file1, file2)) {
            return 0;
        }

        if (lf1.isDirectory() && !lf2.isDirectory())
            return -1;
        if (!lf1.isDirectory() && lf2.isDirectory())
            return 1;
        return Long.compare(lf2.getTime(), lf1.getTime());
    }
}
