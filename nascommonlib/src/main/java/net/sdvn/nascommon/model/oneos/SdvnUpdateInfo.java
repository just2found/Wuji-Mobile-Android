package net.sdvn.nascommon.model.oneos;

import androidx.annotation.Keep;

import java.util.List;

@Keep
public class SdvnUpdateInfo {


    /**
     * changelogchs :
     * changelogcht :
     * changelogen :
     * domains : ["app.memenet.net","app.cifernet.net"]
     * enabled : true
     * errmsg : success
     * files : [{"downloadurl":"http://106.75.150.134:88/sdvnnas/MM_Rom_5.0.99_0103.APP","filename":"MM_Rom_5.0.99_0103.APP","filesize":654,"hash":"b3546dd85858fe6b0875ed35ec9953f3"},{"downloadurl":"http://106.75.150.134:88/sdvnnas/MM_Rom_5.0.99_0103.APP.md5","filename":"MM_Rom_5.0.99_0103.APP.md5","filesize":57,"hash":"decb29eb1616d50168bfc3672502006a"}]
     * result : 0
     * type : 1
     * version : 5.0.99
     */

    private String changelogchs;
    private String changelogcht;
    private String changelogen;
    private boolean enabled;
    private String errmsg;
    private int result;
    private int type;
    private String version;
    private List<String> domains;
    private List<FilesBean> files;

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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }

    public List<FilesBean> getFiles() {
        return files;
    }

    public void setFiles(List<FilesBean> files) {
        this.files = files;
    }

    @Keep
    public static class FilesBean {
        /**
         * downloadurl : http://106.75.150.134:88/sdvnnas/MM_Rom_5.0.99_0103.APP
         * filename : MM_Rom_5.0.99_0103.APP
         * filesize : 654
         * hash : b3546dd85858fe6b0875ed35ec9953f3
         */

        private String downloadurl;
        private String filename;
        private int filesize;
        private String hash;

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
    }
}
