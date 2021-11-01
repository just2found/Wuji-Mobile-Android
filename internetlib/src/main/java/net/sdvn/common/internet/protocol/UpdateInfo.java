package net.sdvn.common.internet.protocol;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import net.sdvn.common.internet.core.GsonBaseProtocol;

import java.util.List;

@Keep
public class UpdateInfo extends GsonBaseProtocol implements Parcelable {


    public static final Creator<UpdateInfo> CREATOR = new Creator<UpdateInfo>() {
        @Override
        public UpdateInfo createFromParcel(Parcel in) {
            return new UpdateInfo(in);
        }

        @Override
        public UpdateInfo[] newArray(int size) {
            return new UpdateInfo[size];
        }
    };
    /**
     * enabled : true
     * files : [{"downloadurl":"http://app.memenet.net:88/izzbie/mm3700_rom_5.1.6_0508.app",
     * "filename":"mm3700_rom_5.1.6_0508.app",
     * "filesize":5398546,
     * "hash":"240b5d18cd6ce7d8cec9adb23b2d79ff"}]
     * version : 5.1.6
     * type : 1
     * domains : ["app.memenet.net",
     * "app.cifernet.net"]
     * changelogchs :
     * changelogcht :
     * changelogen :
     */

    @SerializedName("enabled")
    private boolean enabled;
    @SerializedName("version")
    private String version;
    @SerializedName("type")
    private int type;
    @SerializedName("changelogchs")
    private String changelogchs;
    @SerializedName("changelogcht")
    private String changelogcht;
    @SerializedName("changelogen")
    private String changelogen;
    @SerializedName("files")
    private List<FilesModel> files;
    @SerializedName("domains")
    private List<String> domains;
    /**
     * mustupgrade : 0
     */

    @SerializedName("mustupgrade")
    private int mustupgrade;

    public UpdateInfo() {
    }

    protected UpdateInfo(Parcel in) {
        enabled = in.readByte() != 0;
        version = in.readString();
        type = in.readInt();
        changelogchs = in.readString();
        changelogcht = in.readString();
        changelogen = in.readString();
        files = in.createTypedArrayList(FilesModel.CREATOR);
        domains = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (enabled ? 1 : 0));
        dest.writeString(version);
        dest.writeInt(type);
        dest.writeString(changelogchs);
        dest.writeString(changelogcht);
        dest.writeString(changelogen);
        dest.writeTypedList(files);
        dest.writeStringList(domains);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getChangelogchs() {
        return changelogchs;
    }

    public void setChangelogchs(String changelogchs) {
        this.changelogchs = changelogchs;
    }

    public String getChangelogcht() {
        return changelogcht;
    }

    public void setChangelogcht(String changelogcht) {
        this.changelogcht = changelogcht;
    }

    public String getChangelogen() {
        return changelogen;
    }

    public void setChangelogen(String changelogen) {
        this.changelogen = changelogen;
    }

    public List<FilesModel> getFiles() {
        return files;
    }

    public void setFiles(List<FilesModel> files) {
        this.files = files;
    }

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }

    public int getMustupgrade() {
        return mustupgrade;
    }

    public void setMustupgrade(int mustupgrade) {
        this.mustupgrade = mustupgrade;
    }

    @Keep
    public static class FilesModel implements Parcelable {
        public static final Creator<FilesModel> CREATOR = new Creator<FilesModel>() {
            @Override
            public FilesModel createFromParcel(Parcel in) {
                return new FilesModel(in);
            }

            @Override
            public FilesModel[] newArray(int size) {
                return new FilesModel[size];
            }
        };
        /**
         * downloadurl : http://app.memenet.net:88/izzbie/mm3700_rom_5.1.6_0508.app
         * filename : mm3700_rom_5.1.6_0508.app
         * filesize : 5398546
         * hash : 240b5d18cd6ce7d8cec9adb23b2d79ff
         */

        @SerializedName("downloadurl")
        private String downloadurl;
        @SerializedName("filename")
        private String filename;
        @SerializedName("filesize")
        private int filesize;
        @SerializedName("hash")
        private String hash;

        protected FilesModel(Parcel in) {
            downloadurl = in.readString();
            filename = in.readString();
            filesize = in.readInt();
            hash = in.readString();
        }

        public String getDownloadurl() {
            return downloadurl;
        }

        public void setDownloadurl(String downloadurl) {
            this.downloadurl = downloadurl;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public int getFilesize() {
            return filesize;
        }

        public void setFilesize(int filesize) {
            this.filesize = filesize;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(downloadurl);
            dest.writeString(filename);
            dest.writeInt(filesize);
            dest.writeString(hash);
        }
    }
}
