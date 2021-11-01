package io.weline.repo.torrent.constants

import android.content.Context
import io.weline.repo.torrent.R
import io.weline.repo.torrent.data.BTItem

/** 

Created by admin on 2020/7/3,16:22

 */
object BTResultCode {

    fun getErrorString(context: Context, code: Int, msg: String?=null): String {
        val resId: Int = when (code) {
            BTResultCode.MSG_OK -> {
                // 0 //正确返回
                R.string.success
            }
            BTResultCode.MSG_ERROR_PARAM -> {
                R.string.msg_error_params_error
            }  //参数错误
            BTResultCode.MSG_NO_EXIST_USER -> {
                R.string.msg_error_user_no_exist
            }  //不存在的用户
            BTResultCode.MSG_ERROR_BT_PATH -> {
                R.string.msg_error_invalid_path
            }  //错误的路径
            BTResultCode.MSG_ERROR_BT_CREATE_SEED -> {
                R.string.msg_error_creat_ft
            } //错误的种子
            BTResultCode.MSG_ERROR_SESSION -> {
                R.string.msg_error_session
            }  //不存在的会话或者已经失效
            BTResultCode.MSG_ERROR_TOKEN -> {
                R.string.msg_error_invalid_token
            }  //错误的token
            BTResultCode.MSG_ERROR_RESUME -> {
                R.string.msg_error_resume
            }  //续传错误
            BTResultCode.MSG_ERROR_NO_SEED -> {
                R.string.msg_error_canceled
            }  //不存在的种子
            BTResultCode.MSG_ERROR_CONNECT_HOST -> {
                R.string.msg_error_connect_host
            }  //链接远端服务器失败
            BTResultCode.MSG_ERROR_NO_PERM -> {
                R.string.msg_error_no_permission
            } //没有权限创建种子
            BTResultCode.MSG_ERROR_VERIFY_PACKAGE -> {
                R.string.msg_error_verify_package
            } //  验证包，出错
            BTResultCode.MSG_ERROR_IS_NOT_EN -> {
                R.string.msg_error_is_not_en
            }//12//不是EN服务器
            BTResultCode.MSG_ERROR_DATA_CANNOT_EMPTY -> {
                R.string.msg_error_data_cannot_empty
            }
            BTResultCode.MSG_ERROR_SEED_ALREADY_EXISTS -> {
                R.string.msg_error_seed_already_exists
            }
            BTResultCode.MSG_ERROR_IS_DOWNLOADING -> {
                R.string.msg_error_is_downloading
            }
            BTResultCode.MSG_ERROR_DISKFULL -> {
                R.string.msg_error_disk_full
            }
            BTResultCode.MSG_ERROR_NO_DISK -> {
                R.string.tip_no_sata
            }
            BTResultCode.MSG_ERROR_NOT_IN_RING -> {
                R.string.msg_error_not_in_same_circle
            }
            else -> {
                R.string.ec_request
            }
        }
        return "${if (resId > 0) {
            context.getString(resId)
        } else {
            msg?:"Unknow"
        }}($code)"
    }

    fun getErrorString(context: Context, btItem: BTItem): String {
        return  getErrorString(context,btItem.status)
    }

    const val MSG_WEB_SOCKET_SUCCESS: String = "SUCCESS"
    const val MSG_WEB_SOCKET_OK = 1000 //正确返回

    const val ERR_HOST_NOT_FOUND: Int = -404
    const val UNKNOWN_EXCEPTION: Int = -402

    const val MSG_OK = 0 //正确返回
    const val MSG_ERROR_PARAM = 1 //参数错误
    const val MSG_NO_EXIST_USER = 2 //不存在的用户
    const val MSG_ERROR_BT_PATH = 3 //错误的路径
    const val MSG_ERROR_BT_CREATE_SEED = 4 //错误的种子
    const val MSG_ERROR_SESSION = 5 //不存在的会话或者已经失效
    const val MSG_ERROR_TOKEN = 6 //错误的token
    const val MSG_ERROR_RESUME = 7 //续传错误
    const val MSG_ERROR_NO_SEED = 8 //不存在的种子
    const val MSG_ERROR_CONNECT_HOST = 9 //链接远端服务器失败
    const val MSG_ERROR_NO_PERM = 10//没有权限创建种子
    const val MSG_ERROR_VERIFY_PACKAGE = 11//
    const val MSG_ERROR_IS_NOT_EN = 12//不是EN服务器
    const val MSG_ERROR_DATA_CANNOT_EMPTY = 13//做种数据不能为空目录或者空文件
    const val MSG_ERROR_SEED_ALREADY_EXISTS = 14//种子已经存在列表中
    const val MSG_ERROR_IS_DOWNLOADING = 15//当前是下载状态
    const val MSG_ERROR_DISKFULL = 16//磁盘空间不够
    const val MSG_ERROR_NO_DISK = 17//没有磁盘
    const val MSG_ERROR_NOT_IN_RING = 18//不在同一个圈子
}