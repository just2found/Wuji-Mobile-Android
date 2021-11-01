package net.sdvn.nascommon.model;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/25.
 */
//{T @StringRes,@String}
@Keep
public class FileTypeItem {

    private int title;
    private int normalIcon = 0;
    private int pressedIcon = 0;
    @Nullable
    private Object flag = null;
    private Object mExt2;

    public FileTypeItem(int title, int norIcon, int preIcon, Object flag, Object ext2) {
        this.normalIcon = norIcon;
        this.title = title;
        this.pressedIcon = preIcon;
        this.flag = flag;
        this.mExt2 = ext2;
    }

    public FileTypeItem(int title, int norIcon, int preIcon, Object flag) {
        this(title, norIcon, preIcon, flag, null);
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getNormalIcon() {
        return normalIcon;
    }

    public void setNormalIcon(int normalIcon) {
        this.normalIcon = normalIcon;
    }

    public int getPressedIcon() {
        return pressedIcon;
    }

    public void setPressedIcon(int pressedIcon) {
        this.pressedIcon = pressedIcon;
    }

    @Nullable
    public Object getFlag() {
        return flag;
    }

    public void setFlag(Object flag) {
        this.flag = flag;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileTypeItem that = (FileTypeItem) o;
        return title == that.title &&
                normalIcon == that.normalIcon &&
                pressedIcon == that.pressedIcon &&
                Objects.equals(flag, that.flag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, normalIcon, pressedIcon, flag);
    }

    public Object getExt2() {
        return mExt2;
    }

    public void setExt2(Object ext2) {
        mExt2 = ext2;
    }
}
