package io.weline.internetdb.vo;

import androidx.annotation.Keep;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.weline.internetdb.converter.ListConverter;

@Keep
@Entity
public class BindDeviceModel {
    @Id
    public long id;
    public String userId;
    public String devId;
    public String devDomain;
    public String devSN;
    public int devClass;
    public int devType;
    public String devName;
    public String devMarkName;
    public String mgrLevel;
    public String ownerUserId;
    public String firstName;
    public String lastName;
    public boolean scanConfirm;
    public boolean enableShare;
    public String location;
    public String comment;
    @Convert(converter = ListConverter.class, dbType = String.class)
    public List<String> networks;
    public String gainMBPUrl;

    public long enTimestamp;
    public boolean isEN;
    public String mbpRatio;
    public String maxMbpRatio;
    public boolean chRatioAble;
    public String minMbpRatio;
    public String mbpChValue;
    public long mbpChTime;
    public float gb2cRatio;
    public float gb2cChValue;
    public float maxGb2cRatio;
    public float minGb2cRatio;

    @Nullable
    public String dateTime;

    @NotNull
    public boolean isOwner() {
        return Objects.equals(mgrLevel, "0");
    }
}
