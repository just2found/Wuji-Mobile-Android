package net.sdvn.nascommon.model.phone.media;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class AlbumFolder implements Parcelable {

    /**
     * Folder name.
     */
    private String name;
    /**
     * Image list in folder.
     */
    private ArrayList<AlbumFile> mAlbumFiles = new ArrayList<>();
    /**
     * checked.
     */
    private boolean isChecked;
    /*
     * folder's path*/
    private String path;

    public AlbumFolder() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<AlbumFile> getAlbumFiles() {
        return mAlbumFiles;
    }

    public void addAlbumFile(AlbumFile albumFile) {
        mAlbumFiles.add(albumFile);
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    protected AlbumFolder(Parcel in) {
        name = in.readString();
        mAlbumFiles = in.createTypedArrayList(AlbumFile.CREATOR);
        isChecked = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeTypedList(mAlbumFiles);
        dest.writeByte((byte) (isChecked ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AlbumFolder> CREATOR = new Creator<AlbumFolder>() {
        @Override
        public AlbumFolder createFromParcel(@NonNull Parcel in) {
            return new AlbumFolder(in);
        }

        @Override
        public AlbumFolder[] newArray(int size) {
            return new AlbumFolder[size];
        }
    };

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

}