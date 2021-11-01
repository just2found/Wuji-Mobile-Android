package net.linkmate.app.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import net.sdvn.cmapi.Device;

public class VNodeBean extends Device implements MultiItemEntity {
    private int type = 0;//-1.标题类型 0.普通
    private String name;
    private String id;
    private boolean isSelected;
    private boolean isOnline;

    @Override
    public int getItemType() {
        return type == -1 ? 1 : 2;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
