package net.sdvn.nascommon.model.phone.comp;

import androidx.annotation.NonNull;

import net.sdvn.nascommon.model.phone.LocalFile;

import java.util.Comparator;

/**
 * Comparator for File Date
 */
public class FileDateComparator implements Comparator<LocalFile> {

    @Override
    public int compare(@NonNull LocalFile lf1, @NonNull LocalFile lf2) {
        if (lf1 == lf2) {
            return 0;
        }

        if (lf1.getDate() < lf2.getDate()) {
            return 1;
        } else if (lf1.getDate() > lf2.getDate()) {
            return -1;
        } else {
            return 0;
        }
    }
}
