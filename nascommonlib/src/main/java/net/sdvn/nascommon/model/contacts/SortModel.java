package net.sdvn.nascommon.model.contacts;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

@Keep
public class SortModel extends Contact {

    /**
     * 显示数据拼音的首字母
     */
    public String sortLetters;

    public SortToken sortToken = new SortToken();

    public SortModel(String name, String number, String sortKey) {
        super(name, number, sortKey);
    }

    public SortModel() {

    }

    @NonNull
    @Override
    public String toString() {
        return "SortModel [sortLetters=" + sortLetters + ", sortToken=" + sortToken.toString() + "]";
    }
}
