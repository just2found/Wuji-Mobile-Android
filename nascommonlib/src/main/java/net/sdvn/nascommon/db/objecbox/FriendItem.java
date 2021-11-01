package net.sdvn.nascommon.db.objecbox;

import android.text.TextUtils;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Transient;

/**
 * Created by yun on 18/06/06.
 */
@Keep
@Entity
public class FriendItem {
    private String userId;
    private int iconResId;
    private String nickname;
    @Id
    private long id;
    @NonNull
    private String username;
    private String iconPath;
    private String preTel;
    private String phone;
    @Transient
    public Boolean isSelected;

    public FriendItem(int iconResId, String nickname, @NonNull String username,
                      String iconPath, String preTel, String phone) {
        this.iconResId = iconResId;
        this.nickname = nickname;
        this.username = username;
        this.iconPath = iconPath;
        this.preTel = preTel;
        this.phone = phone;
    }

    public FriendItem() {
    }

    public FriendItem(@NonNull String username) {
        this.username = username;
    }

    public int getIconResId() {
        return this.iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @NonNull
    public String getUsername() {
        return this.username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    public String getIconPath() {
        return this.iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }


    public String getPreTel() {
        return preTel;
    }

    public void setPreTel(String preTel) {
        this.preTel = preTel;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void updateSelf(@NonNull FriendItem info) {
        if (!TextUtils.isEmpty(info.iconPath))
            this.iconPath = info.iconPath;
        if (info.iconResId > 0)
            this.iconResId = info.iconResId;
        if (!TextUtils.isEmpty(info.nickname))
            this.nickname = info.nickname;
        if (!TextUtils.isEmpty(info.phone))
            this.phone = info.phone;
        if (!TextUtils.isEmpty(info.preTel))
            this.preTel = info.preTel;

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
