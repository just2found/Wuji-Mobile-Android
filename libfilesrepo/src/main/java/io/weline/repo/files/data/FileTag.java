package io.weline.repo.files.data;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 *  
 * <p>
 * Created by admin on 2020/10/16,11:34
 */
@Keep
public class FileTag implements Serializable {

    /**
     * id : 3
     * uid : 0
     * name : pics
     * color : 1
     */

    @SerializedName("id")
    private int id;
    @SerializedName("uid")
    private int uid;
    @SerializedName("name")
    private String name;
    @SerializedName("color")
    private int color;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public static final String TAG_FAVORITE="favorite";
}
