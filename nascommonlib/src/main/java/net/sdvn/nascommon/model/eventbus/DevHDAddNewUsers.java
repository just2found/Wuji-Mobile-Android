package net.sdvn.nascommon.model.eventbus;

import androidx.annotation.Keep;

import net.sdvn.common.internet.protocol.entity.ShareUser;
import net.sdvn.nascommon.model.DeviceModel;
import net.sdvn.nascommon.model.oneos.DevAttrInfo;

import java.util.List;

@Keep
public class DevHDAddNewUsers {
    public DeviceModel deviceModelNew;
    public DeviceModel deviceModelOld;
    public List<ShareUser> newUsers;
    public DevAttrInfo devAttrInfo;
}