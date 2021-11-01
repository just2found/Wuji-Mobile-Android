package net.sdvn.common.internet.protocol.ad;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 *  
 * <p>
 * Created by admin on 2020/7/30,17:22
 */
@Keep
public class Ads {

    @SerializedName("list")
    private List<AdModel> list;

    public List<AdModel> getList() {
        return list;
    }

    public void setList(List<AdModel> list) {
        this.list = list;
    }

}
