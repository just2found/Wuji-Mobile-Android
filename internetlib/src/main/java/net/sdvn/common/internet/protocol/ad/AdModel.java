package net.sdvn.common.internet.protocol.ad;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Keep
public class AdModel implements Serializable{

    public static final String TYPE_RECHARGE = "recharge";
    /**
     * descr : 测试
     * imgurl : http://www.memenet.net/template/default/images/weline_banner1.png
     * title : 测试
     * type : recharge
     * redirecturl : 2
     */

    @SerializedName("descr")
    private String descr;
    @SerializedName("imgurl")
    private String imgurl;
    @SerializedName("title")
    private String title;
    @SerializedName("type")
    private String type;
    @SerializedName("redirecturl")
    private String redirecturl;

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRedirecturl() {
        return redirecturl;
    }

    public void setRedirecturl(String redirecturl) {
        this.redirecturl = redirecturl;
    }
}