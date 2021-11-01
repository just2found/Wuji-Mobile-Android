package net.sdvn.common.internet.protocol;

import androidx.annotation.Keep;

import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.protocol.entity.NetMember;

import java.util.List;

/**
 * Created by yun on 2018/1/17.
 */
@Keep
public class NetMembersList extends GsonBaseProtocol {
    public NetMembers data;

    @Keep
    public static class NetMembers {
        public List<NetMember> members;
    }
}
