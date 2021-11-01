package net.sdvn.nascommon.model.oneos;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import io.weline.repo.data.model.PermissionsModel;

/**
 * Created by gaoyun@eli-tech.com on 2016/3/24.
 */
@Keep
public class OneOSUser implements Serializable {
    public static final int TYPE_REMOTE = 1 << 1;
    public static final int TYPE_LOCAL = 1 << 2;

    @Nullable
    @SerializedName("username")
    private String name = null;
    @SerializedName("uid")
    private int uid = 0;
    @SerializedName("gid")
    private int gid = 0;
    private long used = 0;
    private long space = 0;
    @SerializedName("admin")
    private int isAdmin = 0;
    @Nullable
    @SerializedName(value = "mark", alternate = {"remark"})
    private String markName = null;
    private boolean isRemote;
    public int type;
    @SerializedName("permissions")
    private List<PermissionsModel> permissions;
    private long total = 0;
    private long addTime = 0;
    public int level = -1;
    public boolean isCurrent = false;

    public OneOSUser(String name, int uid, int gid, int isAdmin) {
        this.name = name;
        this.uid = uid;
        this.gid = gid;
        this.isAdmin = isAdmin;
    }

    public OneOSUser(String name, int uid, int gid, int isAdmin, String markName) {
        this.name = name;
        this.uid = uid;
        this.gid = gid;
        this.isAdmin = isAdmin;
        this.markName = markName;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public long getSpace() {
        return space;
    }

    public void setSpace(long space) {
        this.space = space;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }

    public int getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(int isAdmin) {
        this.isAdmin = isAdmin;
    }

    @Nullable
    public String getMarkName() {
        return markName;
    }

    public void setMarkName(String markName) {
        this.markName = markName;
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        OneOSUser other = (OneOSUser) obj;
        return Objects.equals(this.name, other.name) &&
                (this.uid == other.uid) &&
                (this.gid == other.gid);
    }

    public boolean isRemote() {
        return isRemote;
    }

    public void setRemote(boolean remote) {
        isRemote = remote;
    }

    @Override
    public String toString() {
        return "OneOSUser{" +
                "name='" + name + '\'' +
                ", uid=" + uid +
                ", gid=" + gid +
                ", used=" + used +
                ", space=" + space +
                ", isAdmin=" + isAdmin +
                ", markName='" + markName + '\'' +
                ", isRemote=" + isRemote +
                ", type=" + type +
                '}';
    }

    public List<PermissionsModel> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionsModel> permissions) {
        this.permissions = permissions;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }


}
