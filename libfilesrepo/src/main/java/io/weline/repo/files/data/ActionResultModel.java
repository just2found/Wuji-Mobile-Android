package io.weline.repo.files.data;

import androidx.annotation.Keep;

/**
 * Created by yun on 2018/3/28.
 */
@Keep
public class ActionResultModel<T> extends BaseResultModel<T> {
    public FileManageAction action;
}
