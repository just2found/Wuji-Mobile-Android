package net.sdvn.common.internet.protocol;

import com.google.gson.annotations.SerializedName;

import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.protocol.entity.SubnetEntity;

import java.util.List;

public class SubnetList extends GsonBaseProtocol {
    @SerializedName("subnet")
    private List<SubnetEntity> subnet;

    public List<SubnetEntity> getSubnet() {
        return subnet;
    }

    public void setSubnet(List<SubnetEntity> subnet) {
        this.subnet = subnet;
    }
}
