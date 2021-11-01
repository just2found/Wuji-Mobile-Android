package net.sdvn.common.internet.protocol;

import androidx.annotation.Keep;

import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.protocol.entity.HardWareDevice;

import java.util.List;

/**
 * @author Raleigh.Luo
 * date：21/4/22 09
 * describe：云设备
 */
@Keep
public class CloudDeviceBeans extends GsonBaseProtocol {
    private CloudDeviceBeanList data = null;

    public CloudDeviceBeanList getData() {
        return data;
    }

    public void setData(CloudDeviceBeanList data) {
        this.data = data;
    }

    @Keep
    public class CloudDeviceBeanList {
        private boolean enable = false;//能否取消禁用

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        private List<HardWareDevice> list = null;

        public List<HardWareDevice> getList() {
            return list;
        }

        public void setList(List<HardWareDevice> list) {
            this.list = list;
        }
    }

}
