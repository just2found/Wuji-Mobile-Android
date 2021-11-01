package net.sdvn.nascommon.model.oneos;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by gaoyun@eli-tech.com on 2016/4/20.
 */
@Keep
public class OneOSInfo implements Serializable {
    private static final long serialVersionUID = -3828678511569062187L;
    @Nullable
    private String version = null;
    @Nullable
    private String model = null;
    private boolean needsUp = false;
    @Nullable
    private String build = null;
    @Nullable
    private String product = null;

    public OneOSInfo(String version, String model, boolean needsUp, String product, String build, int verno) {
        this.version = version;
        this.model = model;
        this.needsUp = needsUp;
        this.build = build;
        this.product = product;
        this.verno = verno;
    }

    private int verno;

    public OneOSInfo(String version, String model, boolean needsUp, String product, String build) {
        this.version = version;
        this.model = model;
        this.needsUp = needsUp;
        this.product = product;
        this.build = build;
    }

    @Nullable
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Nullable
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Nullable
    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public boolean isNeedsUp() {
        return needsUp;
    }

    public void setNeedsUp(boolean needsUp) {
        this.needsUp = needsUp;
    }

    @Nullable
    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public int getVerno() {
        return verno;
    }

    public void setVerno(int verno) {
        this.verno = verno;
    }
}
