package net.sdvn.nascommon.iface;


import net.sdvn.nascommon.constant.AppConstants;

public class Result<D> {
    public int code;
    public String msg;
    public D data;

    public Result(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Result(D data) {
        this(AppConstants.CODE_SUCC, AppConstants.MSG_SUCC);
        this.data = data;
    }

    public boolean isSuccess() {
        return code == AppConstants.CODE_SUCC;
    }

}
