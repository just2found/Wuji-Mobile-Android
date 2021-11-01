package io.weline.internetdb.data;

import io.objectbox.annotation.Transient;

public class SMsgContent {
    /**
     * username : Chen Shuo
     * newsId : 6f5131a14631419b84b6cdc5b5d9934b
     * message : Chen Shuo(179500885@qq.com) 申请加入网络 tnet001
     * status : 0
     * type : apply2net
     * date : 2018-09-27 10:15:47.0
     */
    private String username;
    private String message;
    private String status; //0:等待  1:接受  2:拒绝
    @Transient
    private boolean isSelect;

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}