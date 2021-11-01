package io.weline.repo.files.data;

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
    private ErrorBean error = null;
    @SerializedName("result")
    private boolean result  = false;
    @SerializedName("data")
    public T data = null;

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
        private int code = 0;
        @SerializedName("msg")
        private String msg = null;

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

        @Override
        public String toString() {
            return "ErrorBean{" +
                    "code=" + code +
                    ", msg='" + msg + '\'' +
                    '}';
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
