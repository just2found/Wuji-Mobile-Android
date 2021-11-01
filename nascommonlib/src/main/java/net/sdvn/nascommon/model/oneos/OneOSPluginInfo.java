package net.sdvn.nascommon.model.oneos;


import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * OneSpace plugin information
 *
 * @author shz
 * @since V 1.6.21
 */
@Keep
public class OneOSPluginInfo {
    @Keep
    public enum State {
        GETTING,
        ON,
        OFF,
        UNKNOWN
    }

    private boolean isTitle;
    @Nullable
    private String pack = null;
    @Nullable
    private String version = null;
    @Nullable
    private String name = null;
    private boolean canDel = false;
    private boolean canStat = false;
    private boolean canOff = false;
    private State stat = State.GETTING;
    @Nullable
    private String logo = null;
    @Nullable
    private String url = null;

    public OneOSPluginInfo(boolean isTitle, String name) {
        this.isTitle = isTitle;
        this.name = name;
    }

    public OneOSPluginInfo(@Nullable JSONObject jsonObj) {
        if (null != jsonObj) {
            try {
                this.pack = jsonObj.getString("pack");
                this.name = jsonObj.getString("name");
                this.version = jsonObj.getString("ver");
                this.canDel = jsonObj.getBoolean("candel");
                this.logo = jsonObj.getString("logo");
                this.canStat = jsonObj.getBoolean("canstat");
                this.canOff = jsonObj.getBoolean("canoff");
                this.url = jsonObj.getString("url");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean getIsTitle() {
        return isTitle;
    }

    public void setIsTitle(boolean isTitle) {
        this.isTitle = isTitle;
    }

    @Nullable
    public String getPack() {
        return pack;
    }

    public void setPack(String pack) {
        this.pack = pack;
    }

    @Nullable
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCanDel() {
        return canDel;
    }

    public void setCanDel(boolean canDel) {
        this.canDel = canDel;
    }

    public boolean isCanStat() {
        return canStat;
    }

    public void setCanStat(boolean canStat) {
        this.canStat = canStat;
    }

    public boolean isCanOff() {
        return canOff;
    }

    public void setCanOff(boolean canOff) {
        this.canOff = canOff;
    }

    public State getStat() {
        return stat;
    }

    public void setStat(State stat) {
        this.stat = stat;
    }

    /**
     * If the plugin has been opened
     *
     * @return if opened
     */
    public boolean isOn() {
        return (this.stat == State.ON);
    }

    @Nullable
    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
