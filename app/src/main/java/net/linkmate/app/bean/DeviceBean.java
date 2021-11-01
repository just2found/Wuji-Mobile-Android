package net.linkmate.app.bean;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import net.linkmate.app.R;
import net.sdvn.cmapi.Device;
import net.sdvn.cmapi.LocalDevice;
import net.sdvn.cmapi.global.Constants;
import net.sdvn.cmapi.util.CommonUtils;
import net.sdvn.common.Local;
import net.sdvn.common.internet.protocol.entity.HardWareDevice;
import net.sdvn.common.internet.protocol.entity.MGR_LEVEL;
import net.sdvn.common.vo.InNetDeviceModel;
import net.sdvn.nascommon.db.objecbox.DeviceInfo;
import net.sdvn.nascommon.model.UiUtils;

import java.util.List;

import io.weline.devhelper.IconHelper;

public class DeviceBean extends Device implements MultiItemEntity {
    private int type = 0;//-1.标题类型 0.sn 1.nas 2.client，3 云设备（2021/4/22新增）
    private int mnglevel = 3;//0.owner 1.manager 2.normal 3.待确认
    public boolean isNew = true;
    public boolean isSN = false;
    @Nullable
    private HardWareDevice hardData;
    @Nullable
    private LocalDevice localData;
    @Nullable
    private InNetDeviceModel enServer;
    private DeviceInfo devGlobalModel;

    @Nullable
    public InNetDeviceModel getEnServer() {
        return enServer;
    }

    public void setEnServer(@Nullable InNetDeviceModel enServer) {
        this.enServer = enServer;
        initType();

    }

    public DeviceBean(Device device) {
        refreshData(device);
        this.mnglevel = 3;
    }

    public DeviceBean(String name, String user, int type, int mnglevel) {
        this.setName(name);
        this.setOwner(user);
        this.type = type;
        this.mnglevel = mnglevel;
    }

    public void refreshData(Device device) {
        super.refreshData(device);
        isSN = getDeviceType() == Constants.DT_SMARTNODE;
        initType();
    }

    public boolean isNas() {
        return UiUtils.isNas(getDevClass()) || UiUtils.isNasByFeature(getFeature());
    }

    public boolean isVNode() {
        return deviceType == Constants.DT_V_NODE;
    }

    public int getDevClass() {
        int devClass = super.getDevClass();
        if (devClass == 0) {
            if (hardData != null) {
                devClass = hardData.getOstype();
            } else if (enServer != null) {
                devClass = enServer.getDeviceClass() == null ? 0 : enServer.getDeviceClass();
            } else if (localData != null) {
                devClass = localData.getDeviceCode();
            }
        }
        return devClass;
    }

    private int initType() {
        if (type == 3) return type;//云设备
        if (isSN || isVNode() || isNas() || isEn()) {
            type = 0;
        } else {
            if (CommonUtils.getManufacturer(getDevClass()) == 0) {
                //osType = devClass
                switch (getOsType()) {
                    case Constants.OT_ONESPACE:
                    case Constants.OT_BS_WIFI:
                    case Constants.OT_AMBARELLA_CAMERA:
                    case Constants.OT_M1_STATION:
                    case Constants.OT_NANOPI_M1:
                    case Constants.OT_NANOPI_M2:
                    case Constants.OT_NANOPI_NEO:
                        type = 0;
                        break;
                    case Constants.OT_OSX:
                    case Constants.OT_LINUX:
                    case Constants.OT_WINDOWS:
                    case Constants.OT_ANDROID:
                    case Constants.OT_IOS:
                    case Constants.OT_MINIPC:
                    default:
                        type = 2;
                        break;
                }
            } else {
                int dt = CommonUtils.getDeviceType(getDevClass());
                switch (dt) {
                    case Constants.DT_ROUTER:
                    case Constants.DT_NAS:
                    case Constants.DT_CAMERA:
                    case Constants.DT_OpenWRT:
                    case Constants.DT_SN:
                        type = 0;
                        break;
                    case Constants.DT_MACOS:
                    case Constants.DT_LINUX:
                    case Constants.DT_WINDOWS:
                    case Constants.DT_ANDROID:
                    case Constants.DT_IOS:
                    default:
                        type = 2;
                        break;
                }
            }
        }
        return type;
    }

    public static int getIcon(@NonNull DeviceBean bean) {
        if(bean.getTypeValue() == 3)
            return  R.drawable.icon_device_cloud;
        else if (bean.isVNode())
            return R.drawable.icon_node;
        else return IconHelper.getIconByeDevClass(bean.getDevClass(), true, true);
    }

    public static int getIconSimple(@NonNull DeviceBean bean) {
        if(bean.getTypeValue() == 3)
            return  R.drawable.icon_device_cloud_simple;
        else if (bean.isVNode())
            return R.drawable.icon_device_nodes_simple;
        else return IconHelper.getIconByeDevClassSimple(bean.getDevClass(), true, true);
    }

    public String getName() {
        if (hardData != null)
            return hardData.getDevicename();
        else if (localData != null)
            return localData.getName();
        else
            return super.getName();
    }

    public void setName(String name) {
        if (hardData == null)
            super.setName(name);
        else
            hardData.setDevicename(name);
    }

    public String getOwnerName() {
        return getOwner();
    }

    public String getOwner() {
        String owner = super.getOwner();
        if (hardData != null && (!TextUtils.isEmpty(hardData.getFirstname())
                || !TextUtils.isEmpty(hardData.getLastname()))) {
            return hardData.getOwner();
        } else if (enServer != null && (!TextUtils.isEmpty(enServer.getFirstName())
                || !TextUtils.isEmpty(enServer.getLastName()))) {
            return Local.getLocalName(enServer.getLastName(), enServer.getFirstName());
        }
        if (!TextUtils.isEmpty(owner)) {
            return owner;
        }
        return "N/A";
    }

    @NonNull
    public String getNotNullStr(@Nullable String req) {
        return req != null ? req : "";
    }

    public int getType() {
        if (hardData == null || type == 3)
            return type;
        else {
//            if (hardData.getDevicetype() == Constants.DT_SMARTNODE || isVNode())
            return 0;
//            else
//                return 1;
        }
    }

    public int getTypeValue(){
        return type;
    }

    public boolean isSNConfigurable() {
        return deviceType == Constants.DT_SMARTNODE;
    }

    public int getMnglevel() {
        if (hardData == null)
            return mnglevel;
        else
            return Integer.valueOf(hardData.getMgrlevel());
    }

    public String getDatetime() {
        if (hardData == null)
            return "N/A";
        else
            return hardData.getDatetime();
    }


    public String getId() {
        if (!TextUtils.isEmpty(super.getId())) {
            return super.getId();
        }
        if (hardData != null && hardData.getDeviceid() != null)
            return hardData.getDeviceid();
        else if (localData != null && localData.getDeviceId() != null)
            return localData.getDeviceId();
        return "";
    }

    public String getDeviceSn() {
        if (hardData != null && !TextUtils.isEmpty(hardData.getDevicesn()))
            return hardData.getDevicesn();
        else if (localData != null && !TextUtils.isEmpty(localData.getDeviceSn()))
            return localData.getDeviceSn();
        else
            return "N/A";
    }

    public boolean isOwner() {
        return mnglevel == 0;
    }


    public boolean isAdmin() {
        return mnglevel == 1;
    }

    public boolean hasAdminRights() {
        return isOwner() || isAdmin();
    }

    public boolean isBind() {
        return isAdmin() || isOwner() || isCommon();
    }

    private boolean isCommon() {
        return mnglevel == 2;
    }

    public boolean isOnline() {
        if (super.isOnline()) {
            return true;
        }
        if (localData != null)
            return localData.getStatus() == Constants.CS_CONNECTED;
        return false;
    }

    @Nullable
    public HardWareDevice getHardData() {
        return hardData;
    }

    /**
     * 是否是等待同意状态
     *
     * @return
     */
    public Boolean isPendingAccept() {
        return hardData != null && hardData.getMgrlevel().equals(MGR_LEVEL.UNCONFIRMED);
    }

    public void setHardData(@Nullable HardWareDevice hardData) {
        this.hardData = hardData;
        if (hardData != null) {
            isSN = hardData.getDevicetype() == Constants.DT_SMARTNODE;
            try {
                mnglevel = Integer.valueOf(hardData.getMgrlevel());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        initType();
    }

    @Nullable
    public LocalDevice getLocalData() {
        return localData;
    }

    public void setLocalData(@Nullable LocalDevice localData) {
        this.localData = localData;
        initType();
    }

    @Override
    public int getItemType() {
        return getType() == -1 ? 1 : 2;
    }

    public boolean inNetwork(String netid) {
        return getHardData() != null && getHardData().getNetworkId() != null && getHardData().getNetworkId().equals(netid);
    }

    @Nullable
    public List<String> getNetworks() {
        return getHardData() != null ? getHardData().getNetworkIds() : null;
    }

    public CharSequence getVersion() {
        String appVersion = getAppVersion();
        if (!TextUtils.isEmpty(appVersion)) {
            return appVersion;
        }
        if (localData != null) {
            return localData.getVer();
        }
        return null;
    }

    public String getVip() {
        String vip = super.getVip();
        if (!TextUtils.isEmpty(vip)) {
            return vip;
        }
        if (localData != null) {
            return localData.getVip();
        }
        return null;
    }

    public String getPriIp() {
        String priIp = super.getPriIp();
        if (!TextUtils.isEmpty(priIp)) {
            return priIp;
        }
        if (localData != null) {
            return localData.getDeviceIp();
        }
        return null;
    }

    public boolean isEn() {
        return hardData != null && hardData.isEN();
    }

    public void setGlobalMode(@Nullable DeviceInfo data) {
        this.devGlobalModel = data;
    }

    public DeviceInfo getDevGlobalModel() {
        return devGlobalModel;
    }
}
