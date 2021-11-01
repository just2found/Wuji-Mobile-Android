package net.sdvn.common.internet.protocol;

import androidx.annotation.Keep;

import net.sdvn.common.internet.core.GsonBaseProtocol;

import java.util.List;

@Keep
public class GetShareFilesResultBean extends GsonBaseProtocol {

    private List<FilesBean> files;

    public List<FilesBean> getFiles() {
        return files;
    }

    public void setFiles(List<FilesBean> files) {
        this.files = files;
    }

    @Keep
    public static class FilesBean {
        /**
         * path : /picture/IMG_20181020_171741.jpg
         * type : 0
         * id : 50063
         */

        public String path;
        public String type;
        public String id;
        /**
         * name :
         * size :
         * expire :
         * sharetoken :
         * from :
         * to :
         */

        private String name;
        private String size;
        private String expiredate;
        private String sharetoken;
        private String from;
        private String fromname;
        private String to;
        private String toname;
        private String deviceid;

        /**
         * expiredate : 1545820786
         */

//        private long expiredate;
        public String getFromName() {
            return fromname;
        }

        public void setFromName(String fromname) {
            this.fromname = fromname;
        }

        public String getToName() {
            return toname;
        }

        public void setToName(String toname) {
            this.toname = toname;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getExpire() {
            return expiredate;
        }

        public void setExpire(String expire) {
            this.expiredate = expire;
        }

        public String getSharetoken() {
            return sharetoken;
        }

        public void setSharetoken(String sharetoken) {
            this.sharetoken = sharetoken;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getDeviceid() {
            return deviceid;
        }

        public void setDeviceid(String deviceid) {
            this.deviceid = deviceid;
        }
    }
}
