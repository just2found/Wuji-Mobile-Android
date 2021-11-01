package io.weline.repo.torrent;

import com.google.gson.annotations.SerializedName;

public class BtBaseResult<T> {

    /**
     * status : 0
     * msg :
     * result : {}
     */

    @SerializedName("result")
    private int status;
    @SerializedName("msg")
    private String msg;
    @SerializedName("data")
    private T result;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public boolean isSuccessful() {
        return status == 0;
    }

    public BtBaseResult(T t) {
        this.result = t;
        status = 0;
    }

    public BtBaseResult(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }
}
