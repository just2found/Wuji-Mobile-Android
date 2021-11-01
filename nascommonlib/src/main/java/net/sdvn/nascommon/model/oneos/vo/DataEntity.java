package net.sdvn.nascommon.model.oneos.vo;

import com.chad.library.adapter.base.entity.SectionMultiEntity;

public class DataEntity<T> extends SectionMultiEntity<T> {

    private int mItemType;

    public DataEntity(T t, int itemType) {
        super(t);
        mItemType = itemType;
    }

    @Override
    public int getItemType() {
        return mItemType;
    }
}