package net.linkmate.app.util.business;

import net.sdvn.cmapi.CMAPI;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.listener.ListResultListener;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.loader.BindDeviceHttpLoader;
import net.sdvn.common.internet.loader.DeviceClearBindInfoHttpLoader;
import net.sdvn.common.internet.loader.DeviceSharedUsersHttpLoader;
import net.sdvn.common.internet.loader.UnbindDeviceHttpLoader;
import net.sdvn.common.internet.loader.V2BindDevicesHttpLoader;
import net.sdvn.common.internet.protocol.SharedUserList;
import net.sdvn.common.internet.protocol.SnBindResult;
import net.sdvn.common.internet.protocol.UnbindDeviceResult;

import java.util.ArrayList;
import java.util.List;

public class DeviceUserUtil {


    public static void deleteThisDeviceSingle(String deviceId, HttpLoader.HttpLoaderStateListener loaderStateListener
            , ResultListener<UnbindDeviceResult> listener) {
        UnbindDeviceHttpLoader httpLoader = new UnbindDeviceHttpLoader();
        httpLoader.setHttpLoaderStateListener(loaderStateListener);
        httpLoader.unbindSingle(deviceId, CMAPI.getInstance().getBaseInfo().getUserId(), listener);
    }
    public static void deleteThisDevice(String deviceId, HttpLoader.HttpLoaderStateListener loaderStateListener
            , ListResultListener<UnbindDeviceResult> listener) {
        ArrayList<String> userids = new ArrayList<>();
        userids.add(CMAPI.getInstance().getBaseInfo().getUserId());
        unbindDevice(deviceId, userids, loaderStateListener, listener);
    }

    public static void unbindDevice(String deviceId, final List<String> userids, HttpLoader.HttpLoaderStateListener loaderStateListener
            , ListResultListener<UnbindDeviceResult> listener) {
        UnbindDeviceHttpLoader httpLoader = new UnbindDeviceHttpLoader();
        httpLoader.setHttpLoaderStateListener(loaderStateListener);
        httpLoader.unbind(deviceId, userids, listener);
    }

    public static void deviceClearBinds(String deviceId, HttpLoader.HttpLoaderStateListener loaderStateListener
            , ResultListener listener) {
        DeviceClearBindInfoHttpLoader httpLoader = new DeviceClearBindInfoHttpLoader(GsonBaseProtocol.class);
        httpLoader.setParams(deviceId);
        httpLoader.setHttpLoaderStateListener(loaderStateListener);
        httpLoader.executor(listener);
    }

    public static void shareUsers(String deviceId, HttpLoader.HttpLoaderStateListener loaderStateListener
            , ResultListener listener) {
        DeviceSharedUsersHttpLoader httpLoader = new DeviceSharedUsersHttpLoader(SharedUserList.class);
        httpLoader.setParams(deviceId);
        httpLoader.setHttpLoaderStateListener(loaderStateListener);
        httpLoader.executor(listener);
    }

    public static void bindDeviceBySn(String sn, String appId, HttpLoader.HttpLoaderStateListener loaderStateListener
            , ResultListener listener) {
        BindDeviceHttpLoader httpLoader = new BindDeviceHttpLoader(SnBindResult.class);
        httpLoader.setParams(BindDeviceHttpLoader.Type.TYPE_SCAN, sn,
                null, appId);
        httpLoader.setHttpLoaderStateListener(loaderStateListener);
        httpLoader.executor(listener);
    }

    public static void bindDeviceV2BySn(String sn, String appId, HttpLoader.HttpLoaderStateListener loaderStateListener
            , ResultListener listener) {
        V2BindDevicesHttpLoader httpLoader = new V2BindDevicesHttpLoader(SnBindResult.class);
        httpLoader.setParams(BindDeviceHttpLoader.Type.TYPE_SCAN, sn,
                null, appId);
        httpLoader.setHttpLoaderStateListener(loaderStateListener);
        httpLoader.executor(listener);
    }

    public static void bindDeviceBySC(String shareCode, HttpLoader.HttpLoaderStateListener loaderStateListener
            , ResultListener listener) {
        BindDeviceHttpLoader bindDeviceHttpLoader = new BindDeviceHttpLoader(SnBindResult.class);
        bindDeviceHttpLoader.setParams(BindDeviceHttpLoader.Type.TYPE_SHARE_CODE,
                null, shareCode, "");
        bindDeviceHttpLoader.setHttpLoaderStateListener(loaderStateListener);
        bindDeviceHttpLoader.executor(listener);
    }
}
