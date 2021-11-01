package net.sdvn.nascommon.fileserver;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;
@Keep
public class FileShareBaseResult<T> {

    /**
     * status : 0
     * msg :
     * result : {}
     */

    @SerializedName("status")
    private int status;
    @SerializedName("msg")
    private String msg;
    @SerializedName("result")
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

}
