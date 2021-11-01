package net.linkmate.app.util.business;

import androidx.annotation.NonNull;

import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.loader.AddNetMemberHttpLoader;
import net.sdvn.common.internet.loader.BindNetworkHttpLoader;
import net.sdvn.common.internet.loader.CreateNetworkHttpLoader;
import net.sdvn.common.internet.loader.GetNetMembersHttpLoader;
import net.sdvn.common.internet.loader.RemoveNetHttpLoader;
import net.sdvn.common.internet.loader.RemoveNetMembersHttpLoader;
import net.sdvn.common.internet.protocol.NetMembersList;

import java.util.List;

public class NetManagerUtil {

    public static void createNet(String networkname, HttpLoader.HttpLoaderStateListener loaderStateListener
            , ResultListener listener) {
        CreateNetworkHttpLoader httpLoader = new CreateNetworkHttpLoader(GsonBaseProtocol.class);
        httpLoader.setParams(networkname);
        httpLoader.setHttpLoaderStateListener(loaderStateListener);
        httpLoader.executor(listener);
    }

    public static void removeNet(String networkid, HttpLoader.HttpLoaderStateListener loaderStateListener
            , ResultListener listener) {
        RemoveNetHttpLoader httpLoader = new RemoveNetHttpLoader(GsonBaseProtocol.class);
        httpLoader.setParams(networkid);
        httpLoader.setHttpLoaderStateListener(loaderStateListener);
        httpLoader.executor(listener);
    }

    public static void addNetMember(String networkid, String username, HttpLoader.HttpLoaderStateListener loaderStateListener
            , ResultListener listener) {
        AddNetMemberHttpLoader loader = new AddNetMemberHttpLoader(GsonBaseProtocol.class);
        loader.setParams(networkid, username);
        loader.setHttpLoaderStateListener(loaderStateListener);
        loader.executor(listener);
    }

    public static void getMembers(String networkid, HttpLoader.HttpLoaderStateListener loaderStateListener
            , ResultListener listener) {
        GetNetMembersHttpLoader httpLoader = new GetNetMembersHttpLoader(NetMembersList.class);
        httpLoader.setParams(networkid);
        httpLoader.setHttpLoaderStateListener(loaderStateListener);
        httpLoader.executor(listener);
    }

    public static void removeMembers(@NonNull String networkid, @NonNull List<String> userids, HttpLoader.HttpLoaderStateListener loaderStateListener
            , ResultListener listener) {
        RemoveNetMembersHttpLoader httpLoader = new RemoveNetMembersHttpLoader(GsonBaseProtocol.class);
        httpLoader.setParams(networkid, userids);
        httpLoader.setHttpLoaderStateListener(loaderStateListener);
        httpLoader.executor(listener);
    }

    public static void bindNetworkBySC(String shareCode, HttpLoader.HttpLoaderStateListener loaderStateListener
            , ResultListener listener) {
        BindNetworkHttpLoader bindNetworkHttpLoader = new BindNetworkHttpLoader(GsonBaseProtocol.class);
        bindNetworkHttpLoader.setParams(shareCode);
        bindNetworkHttpLoader.setHttpLoaderStateListener(loaderStateListener);
        bindNetworkHttpLoader.executor(listener);
    }
}
