package net.sdvn.nascommon.model.oneos.vo;

import com.chad.library.adapter.base.entity.SectionMultiEntity;

import net.sdvn.nascommonlib.R;

public class HeaderEntity extends SectionMultiEntity {

    public HeaderEntity(String header) {
        super(true, header);
    }

    @Override
    public int getItemType() {
        return R.layout.layout_timeline_header;
    }
}
