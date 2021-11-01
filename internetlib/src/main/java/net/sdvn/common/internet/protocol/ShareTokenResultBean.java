package net.sdvn.common.internet.protocol;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import net.sdvn.common.internet.core.GsonBaseProtocol;

import java.util.List;

@Keep
public class ShareTokenResultBean extends GsonBaseProtocol {
    @SerializedName("sharetokens")
    private List<SharetokensBean> sharetokens;

    public List<SharetokensBean> getSharetokens() {
        return sharetokens;
    }

    public void setSharetokens(List<SharetokensBean> sharetokens) {
        this.sharetokens = sharetokens;
    }

    @Keep
    public static class SharetokensBean {
        /**
         * id :
         * sharetoken :
         */
        @SerializedName("id")
        private String id;
        @SerializedName("sharetoken")
        private String sharetoken;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSharetoken() {
            return sharetoken;
        }

        public void setSharetoken(String sharetoken) {
            this.sharetoken = sharetoken;
        }
    }
}
