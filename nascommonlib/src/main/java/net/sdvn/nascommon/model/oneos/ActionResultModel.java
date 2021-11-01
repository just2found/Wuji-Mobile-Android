package net.sdvn.nascommon.model.oneos;

import androidx.annotation.Keep;

import net.sdvn.nascommon.model.FileManageAction;

/**
 * Created by yun on 2018/3/28.
 */
@Keep
public class ActionResultModel<T> extends BaseResultModel<T> {
    public FileManageAction action;
}
