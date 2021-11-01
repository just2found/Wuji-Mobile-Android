package net.sdvn.nascommon.model.oneos;

import androidx.annotation.Keep;

@Keep
public class ShareFileInfo {
    public FileBean file;
    public StateBean state;
//    public DeviceBean device;

    @Keep
    public static class FileBean {
        public String token;
        public String sharetoken;
        public String deviceid;
        public String username;
        public long fid;
        public String type;
        public String name;
        public String dir;
        public String to_name;
        public String to_dir;

    }

    @Keep
    public static class StateBean {
        public long size;
        public long length;
        public int state;
        public int err_code;
        public long join_at;
        public long start_at;
        public long end_at;
        public long speed;
    }

  /*  @Keep
    public static class DeviceBean {
        public String domain;
        public String id;
        public String name;
        public String userid;
        public String vip;
    }
*/

}
