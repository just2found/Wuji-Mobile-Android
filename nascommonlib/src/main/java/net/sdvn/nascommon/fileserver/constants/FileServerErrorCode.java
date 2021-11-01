package net.sdvn.nascommon.fileserver.constants;


import net.sdvn.nascommon.model.UiUtils;
import net.sdvn.nascommonlib.R;

public class FileServerErrorCode {
    public static final int ERR_DEVICE_OFFLINE = -45005;   //设备不在线
    public static final int ERR_DEVICE_BOUND = -45002;   //设备已被其他用户绑定
    public static final int ERR_DEVICE_NOT_FOUND = -45004;   //设备不存在

    public static final int MSG_OK = 0; //正确
    public static final int MSG_ERROR_PARAM = 1;   //参数错误
    public static final int MSG_ERROR_CREATE_TASK = 2;  //创建共享失败
    public static final int MSG_ERROR_NO_TASK = 3;  //  没有这个共享
    public static final int MSG_ERROR_DIR = 4;  // 打开目录失败
    public static final int MSG_ERROR_CONNECT = 5;  //  链接失败
    public static final int MSG_ERROR_NO_PERM = 6;  //权限错误
    public static final int MSG_ERROR_TASK_RUNNING = 7;  // 任务运行中错误
    public static final int MSG_ERROR_EXCEED_DOWNLOADS = 8;  //  超过最大下载次数
    public static final int MSG_ERROR_CANCEL_SHARED = 9;  // 取消共享失败 （as返回错误）
    public static final int MSG_ERROR_BIND_NETWORK = 10;  // 绑定源设备出错 （as返回错误）
    public static final int MSG_ERROR_USER_NOT_EXIST = 11;//用户不存在
    public static final int MSG_ERROR_ILLEGAL_OPERATION = 12;//用户非法操作(对已经停止或者完成的任务，再执行停止，会报这个错误)
    public static final int MSG_ERROR_GET_DEVICE_IP = 13; //获取设备ip出错
    public static final int MSG_ERROR_BROKEN_NETWORK = 14;//网络错误
    public static final int MSG_ERROR_DL_NOT_RUNNING = 15;//下载不是在运行状态，不能添加新的路径
    public static final int MSG_ERROR_SOURCE_FILE_OPEN = 16; //源文件打开失败
    public static final int MSG_ERROR_SOURCE_FILE_CHANGE = 17;// 源文件尺寸改变
    public static final int MSG_ERROR_TEMP_FILE = 18; //临时文件被删
    public static final int MSG_ERROR_AUTH = 19;//  内部认证错误
    public static final int MSG_ERROR_TARGET_FILE = 20;//   写目标文件失败
    public static final int MSG_ERROR_TOKEN = 21;// 无效的token
    public static final int MSG_ERROR_MORE_SHARED_TICKET = 22;// 已经存在同样共享下载
    public static final int MSG_ERROR_SHARED_TICKET_OVERTIME = 23;// 共享过期
    public static final int MSG_ERROR_AUTH_PASSWORD = 24;//  密码验证失败
    public static final int MSG_ERROR_INVAILD_TICKET2 = 25;// 无效的Token
    public static final int MSG_ERROR_DISK_FULL = 26;// 磁盘爆满

    public static String getString(int status) {
        int resId = getResId(status);
        return UiUtils.formatWithError(resId, status);
    }

    public static int getResId(int status) {
        int resId = R.string.unknown_exception;
        switch (status) {

            case MSG_ERROR_PARAM:
                resId = R.string.tip_params_error;
                break;
            case MSG_ERROR_CREATE_TASK:
                resId = R.string.tip_error_create_share;
                break;
            case MSG_ERROR_NO_TASK:
            case MSG_ERROR_INVAILD_TICKET2:
                resId = R.string.tip_error_share_not_found;
                break;
            case MSG_ERROR_DIR:
                resId = R.string.tip_error_open_dir;
                break;
            case MSG_ERROR_CONNECT:
                resId = R.string.tip_connect_error;
                break;
            case MSG_ERROR_NO_PERM:
                resId = R.string.permission_denied;
                break;
            case MSG_ERROR_TASK_RUNNING:
                resId = R.string.tip_error_task_running;
                break;
            case MSG_ERROR_EXCEED_DOWNLOADS:
                resId = R.string.msg_error_exceed_downloads;
                break;
            case MSG_ERROR_CANCEL_SHARED:
                resId = R.string.msg_error_cancel_shared;
                break;
            case MSG_ERROR_BIND_NETWORK:
                resId = R.string.msg_error_bind_network;
                break;
            case MSG_ERROR_USER_NOT_EXIST:
                resId = R.string.msg_error_user_not_exist;
                break;
            case MSG_ERROR_ILLEGAL_OPERATION:
                resId = R.string.msg_error_illegal_operation;
                break;
            case MSG_ERROR_GET_DEVICE_IP:
                resId = R.string.msg_error_get_device_ip;
                break;
            case MSG_ERROR_BROKEN_NETWORK:
                resId = R.string.msg_error_broken_network;
                break;
            case MSG_ERROR_DL_NOT_RUNNING:
                resId = R.string.msg_error_dl_not_running;
                break;
            case MSG_ERROR_SOURCE_FILE_OPEN:
                resId = R.string.msg_error_source_file_open;
                break;
            case MSG_ERROR_SOURCE_FILE_CHANGE:
                resId = R.string.msg_error_source_file_change;
                break;
            case MSG_ERROR_TEMP_FILE:
                resId = R.string.msg_error_temp_file;
                break;
            case MSG_ERROR_AUTH:
                resId = R.string.msg_error_auth;
                break;
            case MSG_ERROR_TARGET_FILE:
                resId = R.string.msg_error_target_file;
                break;
            case MSG_ERROR_TOKEN:
                resId = R.string.msg_error_token;
                break;
            case MSG_ERROR_MORE_SHARED_TICKET:
                resId = R.string.msg_error_more_shared_ticket;
                break;
            case MSG_ERROR_SHARED_TICKET_OVERTIME:
                resId = R.string.msg_error_shared_ticket_overtime;
                break;
            case MSG_ERROR_DISK_FULL:
                resId = R.string.msg_error_disk_full;
                break;

        }
        return resId;
    }
}
