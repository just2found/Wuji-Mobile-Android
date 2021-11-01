package net.sdvn.common.internet.protocol.entity;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

@Keep
public class SubnetEntity {

    /**
     * domain : 32
     * net : 10.117.0.114
     * mask : 255.255.255.255
     */

    @SerializedName("domain")
    private String domain;
    @SerializedName("net")
    private String net;
    @SerializedName("mask")
    private String mask;

    public SubnetEntity(String net, String mask) {
        this.net = net;
        this.mask = mask;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SubnetEntity) {
            return Objects.equals(this.getNet(), ((SubnetEntity) obj).getNet())
                    && Objects.equals(this.getMask(), ((SubnetEntity) obj).getMask());
        }
        return false;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getNet() {
        return net;
    }

    public void setNet(String net) {
        this.net = net;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }
}
