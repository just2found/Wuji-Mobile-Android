package net.sdvn.nascommon.model.phone.media;

import android.os.AsyncTask;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class MediaReadTask extends AsyncTask<Void, Void, MediaReadTask.ResultWrapper> {

    public static final int FUNCTION_CHOICE_IMAGE = 0;
    public static final int FUNCTION_CHOICE_VIDEO = 1;
    public static final int FUNCTION_CHOICE_ALBUM = 2;

    public interface Callback {
        /**
         * Callback the results.
         *
         * @param albumFolders album folder list.
         */
        void onScanCallback(ArrayList<AlbumFolder> albumFolders, ArrayList<AlbumFile> checkedFiles);
    }

    static class ResultWrapper {
        private ArrayList<AlbumFolder> mAlbumFolders;
        private ArrayList<AlbumFile> mAlbumFiles;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FUNCTION_CHOICE_ALBUM, FUNCTION_CHOICE_IMAGE, FUNCTION_CHOICE_VIDEO})
    public @interface FunctionChoiceType {
    }

    private int mFunction;
    private List<AlbumFile> mCheckedFiles;
    private MediaReader mMediaReader;
    private Callback mCallback;

    public MediaReadTask(@FunctionChoiceType int function, List<AlbumFile> checkedFiles, MediaReader mediaReader, Callback callback) {
        this.mFunction = function;
        this.mCheckedFiles = checkedFiles;
        this.mMediaReader = mediaReader;
        this.mCallback = callback;
    }

    @NonNull
    @Override
    protected ResultWrapper doInBackground(Void... params) {
        ArrayList<AlbumFolder> albumFolders;
        switch (mFunction) {
            case FUNCTION_CHOICE_IMAGE: {
                albumFolders = mMediaReader.getAllImage();
                break;
            }
            case FUNCTION_CHOICE_VIDEO: {
                albumFolders = mMediaReader.getAllVideo();
                break;
            }
            case FUNCTION_CHOICE_ALBUM: {
                albumFolders = mMediaReader.getAllMedia();
                break;
            }
            default: {
                throw new AssertionError("This should not be the case.");
            }
        }

        ArrayList<AlbumFile> checkedFiles = new ArrayList<>();

        if (mCheckedFiles != null && !mCheckedFiles.isEmpty()) {
            List<AlbumFile> albumFiles = albumFolders.get(0).getAlbumFiles();
            for (AlbumFile checkAlbumFile : mCheckedFiles) {
                for (int i = 0; i < albumFiles.size(); i++) {
                    AlbumFile albumFile = albumFiles.get(i);
                    if (checkAlbumFile.equals(albumFile)) {
                        albumFile.setChecked(true);
                        checkedFiles.add(albumFile);
                    }
                }
            }
        }
        ResultWrapper wrapper = new ResultWrapper();
        wrapper.mAlbumFolders = albumFolders;
        wrapper.mAlbumFiles = checkedFiles;
        return wrapper;
    }

    @Override
    protected void onPostExecute(@NonNull ResultWrapper wrapper) {
        mCallback.onScanCallback(wrapper.mAlbumFolders, wrapper.mAlbumFiles);
    }
}