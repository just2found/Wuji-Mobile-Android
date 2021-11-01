package net.sdvn.nascommon.model.oneos;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

@Keep
public class OneStat {

    /**
     * brand : memenet
     * hd : {"mount":"ok","msg":[{"info":{"ATA Version is":"  ATA/ATAPI-7 ","Device Model":"    WDC WD1600AAJS-07PSA0","Firmware Version":"05.06H05","LU WWN Device Id":"5 0014ee 1007edf48","Model Family":"    Western Digital Caviar Blue Serial ATA","SMART support is":"Enabled","Sector Size":"     512 bytes logical/physical","Serial Number":"   WD-WCAP93622112","User Capacity":"   160,041,885,696 bytes [160 GB]","end":"end","slot":0},"name":"/dev/sda"}]}
     * hd : {"mount":"ok","msg":[{"info":{"          State ":"active","     Array Size ":"976760768 (931.51 GiB 1000.20 GB)","     Raid Level ":"raid1","   Raid Devices ":"2","  Spare Devices ":"0","  Total Devices ":"2","  Used Dev Size ":"976760768 (931.51 GiB 1000.20 GB)"," Active Devices ":"2"," Failed Devices ":"0","/dev/sda1":"active sync","/dev/sdb1":"active sync","Working Devices ":"2","end":"end"},"name":"/dev/md0"},{"info":{"ATA Version is":"  ACS-3 T13/2161-D revision 3b","Device Model":"    WDC WD10EZEX-75WN4A0","Firmware Version":"01.01A01","Form Factor":"     3.5 inches","LU WWN Device Id":"5 0014ee 20eb9590f","Model Family":"    Western Digital Blue","Rotation Rate":"   7200 rpm","SATA Version is":" SATA 3.1, 6.0 Gb/s ","SMART support is":"Enabled","Sector Sizes":"    512 bytes logical, 4096 bytes physical","Serial Number":"   WD-WCC6Y4XZ8A1J","User Capacity":"   1,000,204,886,016 bytes [1.00 TB]","end":"end","slot":0},"name":"/dev/sda"},{"info":{"ATA Version is":"  ACS-3 T13/2161-D revision 3b","Device Model":"    WDC WD10EZEX-75WN4A0","Firmware Version":"01.01A01","Form Factor":"     3.5 inches","LU WWN Device Id":"5 0014ee 2b9651df7","Model Family":"    Western Digital Blue","Rotation Rate":"   7200 rpm","SATA Version is":" SATA 3.1, 6.0 Gb/s ","SMART support is":"Enabled","Sector Sizes":"    512 bytes logical, 4096 bytes physical","Serial Number":"   WD-WCC6Y0DNU3SR","User Capacity":"   1,000,204,886,016 bytes [1.00 TB]","end":"end","slot":1},"name":"/dev/sdb"}]}
     * memenet : {"appId":"CN6SDL3H5K4UL55YP77L","deviceCode":203138,"devicesn":"MCSM3A720003","partnerId":"Y1DMATNYSMZPOKC3R8NJ"}
     * mysql : {"state":"ok"}
     * sn : MCSM3A720003
     * sys : {"build":"20191230","model":"one2017","product":"h1n1","verno":51009,"version":"5.1.9"}
     * user : {"bind":"yes"}
     */

    @SerializedName("brand")
    private String brand;
    @SerializedName("hd")
    private HdModel hd;
    @SerializedName("memenet")
    private MemenetModel memenet;
    @SerializedName("mysql")
    private MysqlModel mysql;
    @SerializedName("sn")
    private String sn;
    @SerializedName("sys")
    private SysModel sys;
    @SerializedName("user")
    private UserModel user;


    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public HdModel getHd() {
        return hd;
    }

    public void setHd(HdModel hd) {
        this.hd = hd;
    }

    public MemenetModel getMemenet() {
        return memenet;
    }

    public void setMemenet(MemenetModel memenet) {
        this.memenet = memenet;
    }

    public MysqlModel getMysql() {
        return mysql;
    }

    public void setMysql(MysqlModel mysql) {
        this.mysql = mysql;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public SysModel getSys() {
        return sys;
    }

    public void setSys(SysModel sys) {
        this.sys = sys;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    @Keep
    public static class HdModel {
        public boolean isOk() {
            return Objects.equals(mount, "ok");
        }

        /**
         * mount : ok
         * msg : [{"info":{"ATA Version is":"  ATA/ATAPI-7 ","Device Model":"    WDC WD1600AAJS-07PSA0","Firmware Version":"05.06H05","LU WWN Device Id":"5 0014ee 1007edf48","Model Family":"    Western Digital Caviar Blue Serial ATA","SMART support is":"Enabled","Sector Size":"     512 bytes logical/physical","Serial Number":"   WD-WCAP93622112","User Capacity":"   160,041,885,696 bytes [160 GB]","end":"end","slot":0},"name":"/dev/sda"}]
         */

        @SerializedName("mount")
        private String mount;
        @SerializedName("msg")
        private List<MsgModel> msg;

        public String getMount() {
            return mount;
        }

        public void setMount(String mount) {
            this.mount = mount;
        }

        @Keep
        public List<MsgModel> getMsg() {
            return msg;
        }

        public void setMsg(List<MsgModel> msg) {
            this.msg = msg;
        }

        @Keep
        public static class MsgModel {
            /**
             * info : {"ATA Version is":"  ATA/ATAPI-7 ","Device Model":"    WDC WD1600AAJS-07PSA0","Firmware Version":"05.06H05","LU WWN Device Id":"5 0014ee 1007edf48","Model Family":"    Western Digital Caviar Blue Serial ATA","SMART support is":"Enabled","Sector Size":"     512 bytes logical/physical","Serial Number":"   WD-WCAP93622112","User Capacity":"   160,041,885,696 bytes [160 GB]","end":"end","slot":0}
             * name : /dev/sda
             */

            @SerializedName("info")
            private InfoModel info;
            @SerializedName("name")
            private String name;

            public InfoModel getInfo() {
                return info;
            }

            public void setInfo(InfoModel info) {
                this.info = info;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            @Keep
            public static class InfoModel {
                @SerializedName("ATA Version is")
                private String _$ATAVersionIs306; // FIXME check this code
                @SerializedName("Device Model")
                private String _$DeviceModel18; // FIXME check this code
                @SerializedName("Firmware Version")
                private String _$FirmwareVersion4; // FIXME check this code
                @SerializedName("LU WWN Device Id")
                private String _$LUWWNDeviceId301; // FIXME check this code
                @SerializedName("Model Family")
                private String _$ModelFamily53; // FIXME check this code
                @SerializedName("SMART support is")
                private String _$SMARTSupportIs290; // FIXME check this code
                @SerializedName("Sector Size")
                private String _$SectorSize297; // FIXME check this code
                @SerializedName("Serial Number")
                private String _$SerialNumber25; // FIXME check this code
                @SerializedName("User Capacity")
                private String _$UserCapacity319; // FIXME check this code
                @SerializedName("end")
                private String end;
                @SerializedName("slot")
                private int slot;

                public String get_$ATAVersionIs306() {
                    return _$ATAVersionIs306;
                }

                public void set_$ATAVersionIs306(String _$ATAVersionIs306) {
                    this._$ATAVersionIs306 = _$ATAVersionIs306;
                }

                public String get_$DeviceModel18() {
                    return _$DeviceModel18;
                }

                public void set_$DeviceModel18(String _$DeviceModel18) {
                    this._$DeviceModel18 = _$DeviceModel18;
                }

                public String get_$FirmwareVersion4() {
                    return _$FirmwareVersion4;
                }

                public void set_$FirmwareVersion4(String _$FirmwareVersion4) {
                    this._$FirmwareVersion4 = _$FirmwareVersion4;
                }

                public String get_$LUWWNDeviceId301() {
                    return _$LUWWNDeviceId301;
                }

                public void set_$LUWWNDeviceId301(String _$LUWWNDeviceId301) {
                    this._$LUWWNDeviceId301 = _$LUWWNDeviceId301;
                }

                public String get_$ModelFamily53() {
                    return _$ModelFamily53;
                }

                public void set_$ModelFamily53(String _$ModelFamily53) {
                    this._$ModelFamily53 = _$ModelFamily53;
                }

                public String get_$SMARTSupportIs290() {
                    return _$SMARTSupportIs290;
                }

                public void set_$SMARTSupportIs290(String _$SMARTSupportIs290) {
                    this._$SMARTSupportIs290 = _$SMARTSupportIs290;
                }

                public String get_$SectorSize297() {
                    return _$SectorSize297;
                }

                public void set_$SectorSize297(String _$SectorSize297) {
                    this._$SectorSize297 = _$SectorSize297;
                }

                public String get_$SerialNumber25() {
                    return _$SerialNumber25;
                }

                public void set_$SerialNumber25(String _$SerialNumber25) {
                    this._$SerialNumber25 = _$SerialNumber25;
                }

                public String get_$UserCapacity319() {
                    return _$UserCapacity319;
                }

                public void set_$UserCapacity319(String _$UserCapacity319) {
                    this._$UserCapacity319 = _$UserCapacity319;
                }

                public String getEnd() {
                    return end;
                }

                public void setEnd(String end) {
                    this.end = end;
                }

                public int getSlot() {
                    return slot;
                }

                public void setSlot(int slot) {
                    this.slot = slot;
                }
            }
        }
    }

    @Keep
    public static class MemenetModel {
        /**
         * appId : CN6SDL3H5K4UL55YP77L
         * deviceCode : 203138
         * devicesn : MCSM3A720003
         * partnerId : Y1DMATNYSMZPOKC3R8NJ
         */

        @SerializedName("appId")
        private String appId;
        @SerializedName("deviceCode")
        private int deviceCode;
        @SerializedName("devicesn")
        private String devicesn;
        @SerializedName("partnerId")
        private String partnerId;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public int getDeviceCode() {
            return deviceCode;
        }

        public void setDeviceCode(int deviceCode) {
            this.deviceCode = deviceCode;
        }

        public String getDevicesn() {
            return devicesn;
        }

        public void setDevicesn(String devicesn) {
            this.devicesn = devicesn;
        }

        public String getPartnerId() {
            return partnerId;
        }

        public void setPartnerId(String partnerId) {
            this.partnerId = partnerId;
        }
    }

    @Keep
    public static class MysqlModel {
        /**
         * state : ok
         */

        @SerializedName("state")
        private String state;

        public String getState() {
            return state;
        }

        public boolean isOk() {
            return Objects.equals(state, "ok");
        }

        public void setState(String state) {
            this.state = state;
        }
    }

    @Keep
    public static class SysModel {
        /**
         * build : 20191230
         * model : one2017
         * product : h1n1
         * verno : 51009
         * version : 5.1.9
         */

        @SerializedName("build")
        private String build;
        @SerializedName("model")
        private String model;
        @SerializedName("product")
        private String product;
        @SerializedName("verno")
        private int verno;
        @SerializedName("version")
        private String version;

        public String getBuild() {
            return build;
        }

        public void setBuild(String build) {
            this.build = build;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

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

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    @Keep
    public static class UserModel {
        /**
         * bind : yes
         */

        @SerializedName("bind")
        private String bind;

        public String getBind() {
            return bind;
        }

        public void setBind(String bind) {
            this.bind = bind;
        }
    }
}
