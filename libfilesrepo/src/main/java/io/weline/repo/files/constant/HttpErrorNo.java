package io.weline.repo.files.constant;

import android.content.Context;

import io.weline.repo.files.R;
import libs.source.common.utils.Utils;

/**
 Created by admin on 2020/8/1,13:54
 */
public class HttpErrorNo {
    public static final int ERR_JSON_EXCEPTION = 5000;
    public static final int ERR_CONNECT_REFUSED = 5001;
    public static final int ERR_ONEOS_VERSION = 5002;
    public static final int ERR_FORMAT_FAILURE = 5003;
    public static final int ERR_UNABLE_HOST = 0;
    public static final int ERR_REQUEST_TIMEOUT = -403;
    public static final int UNKNOWN_EXCEPTION = -400;
    public static final int ERR_DEVICE_STATUS = -402;//设备状态异常
    public static final int ERR_DEVICE_MYSQL_STATUS = -405;//设备状态异常
    //设备返回错误码
    public static final int ERR_ONE_REQUEST = -40000;   //请求错误
    public static final int ERR_ONE_NO_LOGIN = -40001;   //没有登陆
    public static final int ERR_ONE_PARAM = -40002;   //参数错误
    public static final int ERR_ONE_PERMISION = -40003;   //没有权限
    public static final int ERR_ONE_NO_FOUND = -40004;   //文件不存在/执行失败
    public static final int ERR_ONE_EXISTED = -40005;   //目标文件已存在
    public static final int ERR_ONE_SESSION_EXPIRED = -40008;   //目标文件已存在
    public static final int ERR_ONE_NO_SATA = -40010;   //硬盘不存在
    public static final int ERR_ONE_NO_USERNAME = -41001;   //用户名不存在
    public static final int ERR_ONE_PASSWORD = -41002;   //用户密码错误
    public static final int ERR_ONE_FILELIST = -42000;   //文件表异常，需要网页重新索引
    //-42004 chunk 错误，msg 字段返回当前已接收的文件大小（HTTP 400）
    public static final int ERR_ONE_FILE_UPLOAD_CHUNK = -42004;   //文件表异常，需要网页重新索引
    public static final int ERR_ONE_ENCRYPT_PASSWORD = -42001;   //解压密码错误
    public static final int ERR_ONE_USER_SPACE = -41004;   //用户使用空间不足
    public static final int ERR_ONESERVER_HDERROR = -40011;   //硬盘需要格式化
    public static final int ERR_ONESERVER_DEVICE_OFFLINE = -45005;   //设备不在线
    public static final int ERR_ONESERVER_DEVICE_BOUND = -45002;   //设备已被其他用户绑定
    public static final int ERR_ONESERVER_DEVICE_NOT_FOUND = -45004;   //设备不存在


    private static final String TAG = "HttpErrorNo";

    public static String getResultMsg(int errNo, String errMsg) {
        return getResultMsg(false, errNo, errMsg);
    }

    /**
     * 设备返回请求错误信息处理
     *
     * @param errNo  错误码
     * @param errMsg 错误信息
     * @return
     */
    public static String getResultMsg(boolean isFileOrUserRequest, int errNo, String errMsg) {
        Context context = Utils.getApp();
        String msg = errMsg;
        if (errNo == ERR_ONE_REQUEST) {
            msg = context.getResources().getString(R.string.tip_request_failed);
        } else if (errNo == ERR_ONE_NO_LOGIN) {
            msg = context.getResources().getString(R.string.tip_login_again);
        } else if (errNo == ERR_ONE_PARAM) {
            msg = context.getResources().getString(R.string.tip_params_error);
        } else if (errNo == ERR_ONE_PERMISION) {
            msg = context.getResources().getString(R.string.please_login_with_admin);
        } else if (errNo == ERR_ONE_NO_FOUND) {
            msg = context.getResources().getString(R.string.file_not_found);
        } else if (errNo == ERR_ONE_EXISTED) {
            msg = context.getResources().getString(R.string.tip_file_exists);
        } else if (errNo == ERR_ONE_NO_SATA) {
            msg = context.getResources().getString(R.string.tip_no_sata);
        } else if (errNo == ERR_ONE_NO_USERNAME) {
            msg = context.getResources().getString(R.string.tip_user_no_exist);
        } else if (errNo == ERR_ONE_PASSWORD) {
            msg = context.getResources().getString(R.string.tip_password_error);
        } else if (errNo == ERR_ONE_FILELIST) {
            msg = context.getResources().getString(R.string.tip_filetable_error);
        } else if (errNo == ERR_ONE_ENCRYPT_PASSWORD) {
            msg = context.getResources().getString(R.string.tip_decrypt_pass_error);
        } else if (errNo == ERR_ONE_USER_SPACE) {
            msg = context.getResources().getString(R.string.server_space_insufficient);
        } else if (errNo == ERR_ONESERVER_DEVICE_OFFLINE) {
            msg = context.getResources().getString(R.string.tip_device_offline);
        } else if (errNo == ERR_ONESERVER_HDERROR) {
            msg = context.getResources().getString(R.string.tip_sata_need_format);
        } else if (errNo == ERR_ONESERVER_DEVICE_BOUND) {
            msg = context.getResources().getString(R.string.tip_device_bound);
        } else if (errNo == ERR_ONESERVER_DEVICE_NOT_FOUND) {
            msg = context.getResources().getString(R.string.tip_device_not_found);
        } else if (errNo == ERR_DEVICE_STATUS || errNo == ERR_DEVICE_MYSQL_STATUS) {
            msg = context.getResources().getString(R.string.tip_device_status_exp);
        }
        if (errNo == ERR_REQUEST_TIMEOUT) {
            msg = context.getResources().getString(R.string.tip_request_timeout);
        }

        return String.format("%s(%s)", msg, errNo);
    }
}