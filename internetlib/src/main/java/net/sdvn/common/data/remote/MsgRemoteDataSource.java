package net.sdvn.common.data.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.reflect.TypeToken;

import net.sdvn.common.Local;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.GsonBaseProtocolV2;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.core.InitParamsV2AgApiHttpLoader;
import net.sdvn.common.internet.listener.ResultListener;
import net.sdvn.common.internet.protocol.DataPages;
import net.sdvn.common.internet.protocol.entity.MsgCommon;
import net.sdvn.common.internet.utils.Utils;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  
 * <p>
 * Created by admin on 2020/10/18,00:44
 */
public class MsgRemoteDataSource {
    @NonNull
    public HttpLoader getMessages(final long unreadtime,
                                  final int pos, final int pagesize,
                                  @NonNull final String ticket,
                                  @NonNull ResultListener<GsonBaseProtocolV2<DataPages<MsgCommon>>> listener) {
        InitParamsV2AgApiHttpLoader v2AgApiHttpLoader = new InitParamsV2AgApiHttpLoader(GsonBaseProtocol.class) {
            @Override
            public void initParams(Object... objs) {
                setAction("getmsg");
                this.bodyMap = new ConcurrentHashMap<>();
                put("ticket", ticket);
                put("lg", Local.getApiLanguage());
                put("unreadtime", unreadtime);
                put("pos", pos);
                put("pagesize", pagesize);
            }
        };
        Type type = new TypeToken<GsonBaseProtocolV2<DataPages<MsgCommon>>>() {
        }.getType();
        v2AgApiHttpLoader.executor(type, listener);
        return v2AgApiHttpLoader;
    }

    @NonNull
    public HttpLoader processMessages(@NonNull final String msgID, final boolean agree,
                                      @NonNull final String ticket,
                                      @NonNull ResultListener<GsonBaseProtocol> listener) {
        return processMessages(msgID, agree, ticket, null, listener);
    }

    @NonNull
    public HttpLoader processMessages(@NonNull final String msgID, final boolean agree,
                                      @NonNull final String ticket,
                                      @Nullable final String data,
                                      @NonNull ResultListener<GsonBaseProtocol> listener) {
        InitParamsV2AgApiHttpLoader v2AgApiHttpLoader = new InitParamsV2AgApiHttpLoader(GsonBaseProtocol.class) {
            @Override
            public void initParams(Object... objs) {
                setAction("confirmmsg");
                this.bodyMap = new ConcurrentHashMap<>();
                put("ticket", ticket);
                put("msgid", msgID);
                put("agree", agree);
                if (data != null) {
                    String randomNum = Utils.genRandomNum(16);
                    String source = "password=" + Utils.md5(data) + "&random=" + randomNum;
                    String sha256 = Utils.sha256(source);
                    put("random", randomNum);
                    put("signature", sha256);
                }
            }
        };
        v2AgApiHttpLoader.executor(listener);
        return v2AgApiHttpLoader;
    }
}
