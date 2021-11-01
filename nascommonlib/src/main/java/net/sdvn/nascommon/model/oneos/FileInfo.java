package net.sdvn.nascommon.model.oneos;

import androidx.annotation.Keep;

@Keep
public class FileInfo {
    public long id;
    public String name;
    public String dir;
    public String path;
    public String type;
    public int uid;
    public int gid;
    public long size;
    public long time;
    public String perm;
    public long cttime;
    public long duration;
    public int month;
    public long udtime;
    public String md5;
}
