package net.sdvn.common.internet.protocol;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import net.sdvn.common.internet.core.GsonBaseProtocol;

import java.util.List;

/**
 * Created by yun on 2018/3/14.
 */
@Keep
public class UnbindDeviceResult extends GsonBaseProtocol {


    /**
     * errmsg : invaild user
     * result : 1
     * sn : SP03BV100000013A
     * userid : 1125968626319594
     */

    @SerializedName("userid")
    private String userid;
    private List<UnbindsBean> unbinds;


    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public List<UnbindsBean> getUnbinds() {
        return unbinds;
    }

    public void setUnbinds(List<UnbindsBean> unbinds) {
        this.unbinds = unbinds;
    }

    @Keep
    public static class UnbindsBean {
        /**
         * errmsg : success
         * result : 0
         * sn : SP03BV100000013A
         */

        @SerializedName("errmsg")
        private String errmsgX;
        @SerializedName("result")
        private int resultX;
        @SerializedName("sn")
        private String sn;

        public String getErrmsgX() {
            return errmsgX;
        }

        public void setErrmsgX(String errmsgX) {
            this.errmsgX = errmsgX;
        }

        public int getResultX() {
            return resultX;
        }

        public void setResultX(int resultX) {
            this.resultX = resultX;
        }

        public String getSn() {
            return sn;
        }

        public void setSn(String sn) {
            this.sn = sn;
        }
    }
}

