package net.sdvn.nascommon.model.upgrade;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * Created by gaoyun@eli-tech.com on 2016/11/4.
 */
@Keep
public class AppVersionInfo {
    @Nullable
    private String name;
    @Nullable
    private String version;
    @Nullable
    private String link;
    @Nullable
    private String time;
    @Nullable
    private String oneOs;
    @Nullable
    private ArrayList<String> logs;

    public AppVersionInfo(String name, String version, String link, String time, String oneos, ArrayList<String> logs) {
        this.name = name;
        this.version = version;
        this.link = link;
        this.time = time;
        this.oneOs = oneos;
        this.logs = logs;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Nullable
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Nullable
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Nullable
    public String getOneOs() {
        return oneOs;
    }

    public void setOneOs(String oneos) {
        this.oneOs = oneos;
    }

    @Nullable
    public ArrayList<String> getLogs() {
        return logs;
    }

    public void setLogs(ArrayList<String> logs) {
        this.logs = logs;
    }
}
