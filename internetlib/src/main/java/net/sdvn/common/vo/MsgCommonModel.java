package net.sdvn.common.vo;

import androidx.annotation.Keep;

import io.objectbox.annotation.Entity;

/**
 *  
 * <p>
 * Created by admin on 2020/10/22,11:15
 */
@Keep
@Entity
public class MsgCommonModel extends MsgModel<String> {
    public static final int MESSAGE_STATUS_AGREE = 1;
    public static final int MESSAGE_STATUS_DISAGREE = 2;
    public static final int MESSAGE_STATUS_WAIT = 0;
    private String content;
    private String params;

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public void setContent(String content) {
        this.content = content;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public boolean isWaitConfirm() {
        return getConfirmAck() == MESSAGE_STATUS_WAIT;
    }
}
