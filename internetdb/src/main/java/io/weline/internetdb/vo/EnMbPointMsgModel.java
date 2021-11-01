package io.weline.internetdb.vo;

import androidx.annotation.Keep;

import net.sdvn.common.internet.protocol.entity.FlowMbpointRatioModel;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.weline.internetdb.converter.ContentModelConverter;

@Keep
@Entity
public class EnMbPointMsgModel extends MsgModel<FlowMbpointRatioModel> {

    @Convert(converter = ContentModelConverter.class, dbType = String.class)
    private FlowMbpointRatioModel content;

    @Override
    public FlowMbpointRatioModel getContent() {
        return content;
    }

    @Override
    public void setContent(FlowMbpointRatioModel content) {
        this.content = content;
    }


}
