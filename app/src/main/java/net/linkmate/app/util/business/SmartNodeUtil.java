package net.linkmate.app.util.business;

import net.linkmate.app.bean.DeviceBean;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.cmapi.Device;
import net.sdvn.cmapi.global.Constants;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.loader.GetSubnetHttpLoader;
import net.sdvn.common.internet.loader.SetAccessFlagHttpLoader;
import net.sdvn.common.internet.loader.SetDnsHttpLoader;
import net.sdvn.common.internet.loader.SetSubnetHttpLoader;
import net.sdvn.common.internet.protocol.SubnetList;
import net.sdvn.common.internet.protocol.entity.SubnetEntity;

import java.util.List;

public class SmartNodeUtil {

    //访问互联网
    public static boolean isAccessInternet(Device device) {
        int feature = device.getFeature();
        return (feature & Constants.DF_ACCESS_INTERNET) != 0;
    }

    //使用子网
    public static boolean isAccessSubnet(Device device) {
        int feature = device.getFeature();
        return (feature & Constants.DF_ACCESS_SUBNET) != 0;
    }

    //选择\取消节点
    public static boolean onSelect(DeviceBean device, boolean isSelect) {
        boolean isSN = device.getDeviceType() == Constants.DT_SMARTNODE;
        if (isSN) {
            Boolean result;
            if (isSelect) {
                result = CMAPI.getInstance().addSmartNode(device.getId());
            } else {
                result = CMAPI.getInstance().removeSmartNode(device.getId());
            }
            return result;
        }
        return false;
    }

    //获取设置的子网信息
    public static void getSubnet(DeviceBean bean, HttpLoader.HttpLoaderStateListener stateListener,
                                 ResultListener<SubnetList> resultListener) {
        GetSubnetHttpLoader httpLoader = new GetSubnetHttpLoader(SubnetList.class);
        httpLoader.setParams(bean.getId());
        httpLoader.setHttpLoaderStateListener(stateListener);
        httpLoader.executor(resultListener);
    }

    //提交节点设置
    public static void submitAccessFlag(DeviceBean bean, boolean accessInternet, boolean accessSubnet,
                                        HttpLoader.HttpLoaderStateListener stateListener, ResultListener resultListener) {
        SetAccessFlagHttpLoader httpLoader = new SetAccessFlagHttpLoader(GsonBaseProtocol.class);
        httpLoader.setParams(bean.getId(), accessInternet, accessSubnet);
        httpLoader.setHttpLoaderStateListener(stateListener);
        httpLoader.executor(resultListener);
    }

    //提交dns设置
    public static void submitDns(DeviceBean bean, String dns, HttpLoader.HttpLoaderStateListener stateListener, ResultListener resultListener) {
        SetDnsHttpLoader httpLoader = new SetDnsHttpLoader(GsonBaseProtocol.class);
        httpLoader.setParams(bean.getId(), dns);
        httpLoader.setHttpLoaderStateListener(stateListener);
        httpLoader.executor(resultListener);
    }

    //提交子网设置
    public static void submitSubnet(DeviceBean bean, List<SubnetEntity> subnet,
                              HttpLoader.HttpLoaderStateListener stateListener, ResultListener resultListener) {
        SetSubnetHttpLoader httpLoader = new SetSubnetHttpLoader(GsonBaseProtocol.class);
        httpLoader.setParams(bean.getId(), subnet);
        httpLoader.setHttpLoaderStateListener(stateListener);
        httpLoader.executor(resultListener);
    }
}
