package net.sdvn.nascommon.model.oneos;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Objects;

@Keep
public class DevAttrInfo implements Serializable {
    public DevInfo hd;
    public DevInfo sys;
    public String devId;

    @Keep
    public static class DevInfo implements Serializable {
        /**
         * partnerId :
         * appId :
         * deviceCode :
         * devicesn :
         */

        public String partnerId;
        public String appId;
        public int deviceCode;
        public String devicesn;

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DevInfo devInfo = (DevInfo) o;
            return deviceCode == devInfo.deviceCode &&
                    Objects.equals(partnerId, devInfo.partnerId) &&
                    Objects.equals(appId, devInfo.appId) &&
                    Objects.equals(devicesn, devInfo.devicesn);
        }

        public boolean sameManufacturer(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DevInfo devInfo = (DevInfo) o;
            return Objects.equals(partnerId, devInfo.partnerId) &&
                    Objects.equals(appId, devInfo.appId);
        }

        @Override
        public int hashCode() {

            return Objects.hash(partnerId, appId, deviceCode, devicesn);
        }
    }
}

