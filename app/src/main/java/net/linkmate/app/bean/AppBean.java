package net.linkmate.app.bean;

public class AppBean {
    public int appId;
    public String name;
    public int iconRes;
    public boolean tipsEnable;

    public AppBean(int appId, String name, int iconRes, boolean tipsEnable) {
        this.appId = appId;
        this.name = name;
        this.iconRes = iconRes;
        this.tipsEnable = tipsEnable;
    }

    public AppBean(String name, int iconRes) {
        this.name = name;
        this.iconRes = iconRes;
        this.tipsEnable = true;
    }
}
