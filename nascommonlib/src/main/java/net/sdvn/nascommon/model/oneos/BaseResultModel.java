package net.sdvn.nascommon.model.oneos;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yun on 2018/3/28.
 */
@Keep
public class BaseResultModel<T> {


    /**
     * error : {"code":1,"msg":"xxxx"}
     * result : true
     */
    @SerializedName("error")
    private ErrorBean error;
    @SerializedName("result")
    private boolean result;
    @SerializedName("data")
    public T data;

    @Nullable
    public ErrorBean getError() {
        return error;
    }

    public void setError(ErrorBean error) {
        this.error = error;
    }

    public boolean isSuccess() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    @Keep
    public static class ErrorBean {
        /**
         * code : 1
         * msg : xxxx
         */
        @SerializedName("code")
        private int code;
        @SerializedName("msg")
        private String msg;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "BaseResultModel{" +
                "error=" + error +
                ", result=" + result +
                ", data=" + data +
                '}';
    }
}
