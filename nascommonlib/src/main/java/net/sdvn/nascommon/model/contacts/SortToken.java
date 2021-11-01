package net.sdvn.nascommon.model.contacts;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

/**
 * @author xiaobo.cui 2014年11月26日 上午10:40:15
 */
@Keep
public class SortToken {
    /**
     * 简拼
     */
    public String simpleSpell = "";
    /**
     * 全拼
     */
    public String wholeSpell = "";
    /**
     * 中文全名
     */
    @NonNull
    public String chName = "";

    @NonNull
    @Override
    public String toString() {
        return "[simpleSpell=" + simpleSpell + ", wholeSpell=" + wholeSpell + ", chName=" + chName + "]";
    }
}
