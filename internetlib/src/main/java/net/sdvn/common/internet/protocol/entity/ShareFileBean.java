package net.sdvn.common.internet.protocol.entity;

import androidx.annotation.Keep;

@Keep
public class ShareFileBean {
    private String name;
    private String size;
    private String path;
    private String type;
    private String loginname;
    private String id;

    public ShareFileBean(String name, String size, String path, String type, String loginname, String id) {
        this.name = name;
        this.size = size;
        this.path = path;
        this.type = type;
        this.loginname = loginname;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLoginname() {
        return loginname;
    }

    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
