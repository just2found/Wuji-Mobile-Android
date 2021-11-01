package net.sdvn.nascommon.model.oneos;

import androidx.annotation.Keep;

import java.util.List;

@Keep
public class UpdateInfo {
    /**
     * basever : 5.0.0
     * baseverno : 50000
     * log : ["测试升级"]
     * needup : true
     * notice :
     * online : true
     * time : 2025-01-23
     * url :
     * ver : 5.9.9
     * verno : 50909
     */

    private String basever;
    private int baseverno;
    private boolean needup;
    private String notice;
    private boolean online;
    private String time;
    private String url;
    private String ver;
    private int verno;
    private List<String> log;

    public String getBasever() {
        return basever;
    }

    public void setBasever(String basever) {
        this.basever = basever;
    }

    public int getBaseverno() {
        return baseverno;
    }

    public void setBaseverno(int baseverno) {
        this.baseverno = baseverno;
    }

    public boolean isNeedup() {
        return needup;
    }

    public void setNeedup(boolean needup) {
        this.needup = needup;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public int getVerno() {
        return verno;
    }

    public void setVerno(int verno) {
        this.verno = verno;
    }

    public List<String> getLog() {
        return log;
    }

    public void setLog(List<String> log) {
        this.log = log;
    }


}
