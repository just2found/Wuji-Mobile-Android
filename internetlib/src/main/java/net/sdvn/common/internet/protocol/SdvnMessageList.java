package net.sdvn.common.internet.protocol;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.protocol.entity.SdvnMessage;

import java.util.List;

@Keep
public class SdvnMessageList extends GsonBaseProtocol {
    @SerializedName("newslist")
    private List<SdvnMessage> newslist;

    public List<SdvnMessage> getNewslist() {
        return newslist;
    }

    public void setNewslist(List<SdvnMessage> newslist) {
        this.newslist = newslist;
    }
}
