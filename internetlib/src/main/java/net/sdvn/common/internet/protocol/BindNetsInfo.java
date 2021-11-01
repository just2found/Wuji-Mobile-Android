package net.sdvn.common.internet.protocol;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import net.sdvn.common.internet.core.GsonBaseProtocolV2;
import net.sdvn.common.internet.protocol.entity.BindNetModel;

import java.util.List;

/**
 *  
 * <p>
 * Created by admin on 2020/10/18,00:29
 */
@Keep
public class BindNetsInfo extends GsonBaseProtocolV2<BindNetsInfo.DataModel> {
    @Keep
    public static class DataModel {
        @Keep
        @SerializedName("list")
        private List<BindNetModel> list;

        public List<BindNetModel> getList() {
            return list;
        }

        public void setList(List<BindNetModel> list) {
            this.list = list;
        }


    }
}