package io.weline.repo.api

import io.weline.repo.files.R
import libs.source.common.utils.Utils

/** 

Created by admin on 2021/1/8,15:55

 */
const val V5_ERR_OPERATOR = -40000//操作错误|
const val V5_ERR_NOT_LOGIN = -40001//没登陆或者验证失败|
const val V5_ERR_ERROR_PARAMS = -40002//输入的参数错误|
const val V5_ERR_CREATE_USER_FAILED = -40003//用户创建失败|
const val V5_ERR_NOT_EXIST_DATA = -40004//不存在的数据|
const val V5_ERR_USER_EXISTED = -40005//用户存在|
const val V5_ERR_FILE_NOT_EXISTED = -40006//目标文件不存在|
const val V5_ERR_FILE_IS_FOLDER = -40007//文件为目录|
const val V5_ERR_SESSION_EXP = -40008//会话验证失败|
const val V5_ERR_FILE_EXISTED = -40009//文件已存在|

//const val V5_ERR_DENIED_PERMISSION =-40010//没有权限|          此项与oneapi冲突
//const val V5_ERR_DB_UPDATING =-40011//DB正在更新|              此项与oneapi冲突
const val V5_ERR_DISK_FORMATTING = -40012//磁盘正在格式化中...|
const val V5_ERR_MANIFEST_NOT_EXISTED = -40013//清单文件不存在|
const val V5_ERR_USER_NOT_EXISTED = -40014//用户不存在|
const val V5_ERR_FILE_TAG_EXISTED = -40015//标签已存在|
const val V5_ERR_FILE_TAG_NOT_EXISTED = -40016//标签不存在|
const val V5_ERR_DISK_FORMAT_FAILED = -40017//格式化失败|
const val V5_ERR_SYS_ERROR = -40018//系统错误|
const val V5_ERR_UNABLE_TO_TRANSFER_ITSELF = -40019//无法转移自身|
const val V5_ERR_USER_TARGET_IS_ADMIN = -40020//目标已经是管理员|
const val V5_ERR_DISK_NOT_FOUND = -40021//没有磁盘|
const val V5_ERR_OPERATOR_NOT_SUPPORT = -40022//不支持该操作|
const val V5_ERR_FILE_TARGET_IS_FILE = -40023//目标为文件|
const val V5_ERR_DISK_IS_FULL = -40024//磁盘空间已满|
const val V5_ERR_USER_SPACE_FULL = -40025//用户配额空间已满|
const val V5_ERR_DB_ERROR = -40026//数据库错误|
const val V5_ERR_DISK_NOT_MOUNTED = -40030//磁盘未挂载|
const val V5_ERR_DISK_UNFORMATTED = -40031//磁盘未格式化|
const val V5_ERR_DISK_RAID_NOT_EXISTED = -40032//raid不存在|
const val V5_ERR_DISK_LVM_NOT_EXISTED = -40033//lvm不存在|
const val V5_ERR_DISK_LVM_STOP_DEV = -40034//lvm不能停止设备|
const val V5_ERR_DISK_RAID_STOP_DEV = -40035//raid不能停止设备|
const val V5_ERR_DISK_BUILDING = -40041//磁盘构建中
const val V5_ERR_DISK_MAIN_NOT_EXIST = -40042//主磁盘不存在
const val V5_ERR_DISK_NOT_SELECTED_MAIN = -40043//不是选择的主磁盘
const val V5_ERR_DISK_NOT_SELECT_MAIN = -40044//没有选择主磁盘
const val V5_ERR_DISK_MAIN_CANNOT_MOUNTED = -40045//不能挂载主磁盘
const val V5_ERR_DISK_MAIN_ERROR = -40046//主磁盘错误
const val V5_ERR_DISK_NUM_TO_RAID = -40047//磁盘数目错误，无法创建Raid
const val V5_ERR_FILE_INITIALIZING = -40048//文件基础服务初始化中...

const val V5_ERR_FILE_GUEST_NO_HOME = -40050 //|   guest用户无home目录   |
const val V5_ERR_FILE_GET_dup_LIST_first = -40051 //|  需先获取重复文件列表   |
const val V5_ERR_FILE_DIR_NOT_EXIST = -40052 //|       目录不存在        |
const val V5_ERR_FILE_DEL_DUP = -40053 //|    删除重复文件错误     |
const val V5_ERR_FILE_FOUND_IN_DIR_IS_0 = -40054 //| 指定目录找到文件数量为0 |
const val V5_ERR_FILE_UNSUPPORTED_FORMAT = -40055 //|      不支持的格式       |
const val V5_ERR_FILE_NOT_ON_DISK = -40056 //|      不在磁盘中...      |

const val V5_ERR_FILE_HAVE_THIS_TAG = -40057 //|  文件已经有此标签  |
const val V5_ERR_FILE_NOT_EXIT_THIS_TAG = -40058 //| 该文件不存在此标签 |

const val V5_ERR_USER_NOT_BIND = -45111//用户未绑定此设备|
const val V5_ERR_FILE_CAN_NOT_COPY_INTO_SELF = -45112//不能循环拷贝自身|
const val V5_ERR_DENIED_PERMISSION = -40098//没有权限|
const val V5_ERR_DB_UPDATING = -40099//DB正在更新|

//这些是离线下载的
const val DOWNLOAD_OFFLINE_PARSING_FAILED = -3//BT文件解析失败...
const val DOWNLOAD_OFFLINE_ALREADY_EXISTS = -4//该下载任务已存在
const val DOWNLOAD_OFFLINE_QUEUE_FULL = -5 //下载队列已满
const val DOWNLOAD_OFFLINE_INFORMATION_ERROR = -6//下载信息错误
const val DOWNLOAD_OFFLINE_DOWNLOADED = -7//该文件已下载完毕，无需重新下载
const val DOWNLOAD_OFFLINE_WRONG_WAY = -10//请求方式错误
const val DOWNLOAD_OFFLINE_ERROR_PARAMS = -11//请求参数错误
const val DOWNLOAD_OFFLINE_ADD_FAILED = -12//非bt/magnet类下载若无法进行下载将即时返回如下 // error : 1 url不可达、2 登陆失败、3 链接超时、4 存储空间不足
const val DOWNLOAD_OFFLINE_COMMUNICATION_FAILURE = -20//后台通讯失败
const val DOWNLOAD_OFFLINE_BACKGROUND_PARSING_FAILED = -21 //后台数据解析失败
const val DOWNLOAD_OFFLINE_NO_PERMISSION = -30 //无操作权限

const val DOWNLOAD_OFFLINE_URL = 1//url不可达
const val DOWNLOAD_OFFLINE_LOGIN = 2//2 登陆失败
const val DOWNLOAD_OFFLINE_TIME = 3//3 链接超时
const val DOWNLOAD_OFFLINE_SPACE = 4//4 存储空间不足
const val DOWNLOAD_OFFLINE_UNKOWN = 5//5 未知错误(非1，2，3，4时就是5)


const val ERROR_40201 = -40201  //	密码不允许含有空白字符,必须包含字母、数字和特殊字符,且长度在6-20之间
const val ERROR_40202 = -40202    //密保问题不允许含有空白字符,且长度为1-32之间
const val ERROR_40203 = -40203    //密保答案不允许含有空白字符,且长度为1-32之间
const val ERROR_40205 = -40205     //保险箱不存在
const val ERROR_40206 = -40206    //保险箱已初始化
const val ERROR_40207 = -40207     //保险箱未解锁
const val ERROR_40208 = -40208    //保险箱已打开 (已经解锁)
const val ERROR_40209 = -40209   //保险箱密码错误
const val ERROR_40210 = -40210       //密保答案错误
const val ERROR_40212 = -40212   //口令不存在或无效
const val ERROR_40213 = -40213   //新密码不能与新密码相同
const val ERROR_40214 = -40214   //新密保不能与旧密保相同
const val WELINE_WRONG_PASSWORD = 116

/**--DLNA/SAMBA-------------------------------------------------------**/
const val ERROR_40401 = -40401   //该类型平台不支持此操作
const val ERROR_40402 = -40402    //该服务不支持此操作
const val ERROR_40403 = -40403   //设备不支持该服务
//群组空间的
const val ERROR_40500 =-40500//Msg: "群名已存在"
const val ERROR_40501 = -40501// Msg: "创建群已满"
const val ERROR_40504 = -40504  //目标未加入该群

//这些是自己添加的
const val UNKNOWN = 1000//未知错误
const val LOGIN_SESSION_NULL = 1001//没有获取到SESSION
const val NOT_V5 = 1101//不是V5设备
const val RETURN_PARAMETER_ERROR = 1002//返回的参数错误，构造对象失败


object V5HttpErrorNo {
    @JvmStatic
    fun getResourcesId(errorCode: Int?,addErrorNo:Boolean =false): String {
        val context = Utils.getApp()
        var info: String? = null
        when (errorCode) {
            DOWNLOAD_OFFLINE_BACKGROUND_PARSING_FAILED,
            DOWNLOAD_OFFLINE_WRONG_WAY, DOWNLOAD_OFFLINE_INFORMATION_ERROR, DOWNLOAD_OFFLINE_UNKOWN
            -> {
                info = context.getString(R.string.download_failed)
            }
            DOWNLOAD_OFFLINE_COMMUNICATION_FAILURE, V5_ERR_SYS_ERROR, DOWNLOAD_OFFLINE_BACKGROUND_PARSING_FAILED -> {
                info = context.getString(R.string.system_error)
            }

            DOWNLOAD_OFFLINE_PARSING_FAILED -> {
                info = context.getString(R.string.parse_bt_failed)
            }
            DOWNLOAD_OFFLINE_DOWNLOADED, DOWNLOAD_OFFLINE_ALREADY_EXISTS -> {
                info = context.getString(R.string.task_already_exists)
            }
            DOWNLOAD_OFFLINE_QUEUE_FULL -> {
                info = context.getString(R.string.queue_full_try_later)
            }
            DOWNLOAD_OFFLINE_ADD_FAILED -> {  //
                info = context.getString(R.string.add_user_failed)
            }
            DOWNLOAD_OFFLINE_URL -> { //url不可达
                info = context.getString(R.string.input_url)
            }
            DOWNLOAD_OFFLINE_LOGIN -> {//2 登陆失败
                info = context.getString(R.string.tip_login_again)
            }
            DOWNLOAD_OFFLINE_TIME -> {//3 链接超时
                info = context.getString(R.string.socket_timeout)
            }
            DOWNLOAD_OFFLINE_SPACE -> {//4 存储空间不足
                info = context.getString(R.string.server_space_insufficient)
            }
            ERROR_40209 -> {
                info = context.getString(R.string.tip_password_error)
            }
            ERROR_40210 -> {
                info = context.getString(R.string.question_ans_erro)
            }
            WELINE_WRONG_PASSWORD -> {
                info = context.getString(R.string.tip_password_error)
            }

            ERROR_40212, ERROR_40207 -> {
                info = context.getString(R.string.validation_information_failure)
            }
            ERROR_40213 -> {
                info = context.getString(R.string.cannot_same_old_password)
            }
            ERROR_40214 -> {
                info = context.getString(R.string.cannot_same_old_protection)
            }
            ERROR_40403 ->{
                info = context.getString(R.string.this_device_does_not_support_this_feature)
            }
            V5_ERR_OPERATOR -> {// = -40000//操作错误|
                info = context.getString(R.string.operate_failed)
            }
            V5_ERR_NOT_LOGIN -> {  //  -40001//没登陆或者验证失败|

            }
            V5_ERR_ERROR_PARAMS, DOWNLOAD_OFFLINE_ERROR_PARAMS -> {  //  -40002//输入的参数错误|
                info = context.getString(R.string.tip_params_error)
            }
            V5_ERR_CREATE_USER_FAILED -> {  //  -40003//用户创建失败|

            }
            V5_ERR_NOT_EXIST_DATA -> {  // -40004//不存在的数据|

            }
            V5_ERR_USER_EXISTED -> {  // -40005//用户存在|

            }
            V5_ERR_FILE_NOT_EXISTED -> {  //  -40006//目标文件不存在|
                info = context.getString(R.string.file_not_found)
            }
            V5_ERR_FILE_IS_FOLDER -> {  //  -40007//文件为目录|

            }
            V5_ERR_SESSION_EXP -> {  // -40008//会话验证失败|
                info = context.getString(R.string.msg_error_session)
            }
            V5_ERR_FILE_EXISTED -> {  //  -40009//文件已存在|
                info = context.getString(R.string.tip_file_exists)
            }
            V5_ERR_DENIED_PERMISSION, DOWNLOAD_OFFLINE_NO_PERMISSION -> {  //  -40010//没有权限|
                info = context.getString(R.string.error_manage_perm_deny)
            }
            V5_ERR_DB_UPDATING -> {  //  -40011//DB正在更新|
            }
            V5_ERR_DISK_FORMATTING -> {  //  -40012//磁盘正在格式化中...|

            }
            V5_ERR_MANIFEST_NOT_EXISTED -> {  //  -40013//清单文件不存在|
            }
            V5_ERR_USER_NOT_EXISTED -> {  // -40014//用户不存在|
            }
            V5_ERR_FILE_TAG_EXISTED -> {  //  -40015//标签已存在|
            }
            V5_ERR_FILE_TAG_NOT_EXISTED -> {  //  -40016//标签不存在|
            }
            V5_ERR_DISK_FORMAT_FAILED -> {  //  -40017//格式化失败|
            }
            V5_ERR_SYS_ERROR -> {  //  -40018//系统错误|
            }
            V5_ERR_UNABLE_TO_TRANSFER_ITSELF -> {  //  -40019//无法转移自身|
            }
            V5_ERR_USER_TARGET_IS_ADMIN -> {  //  -40020//目标已经是管理员|
            }
            V5_ERR_DISK_NOT_FOUND -> {  //  -40021//没有磁盘|
                info = context.getString(R.string.tip_no_sata)
            }
            V5_ERR_OPERATOR_NOT_SUPPORT -> {  //  -40022//不支持该操作|

            }
            V5_ERR_FILE_TARGET_IS_FILE -> {  // -40023//目标为文件|
            }
            V5_ERR_DISK_IS_FULL -> {  //  -40024//磁盘空间已满|
                info = context.getString(R.string.msg_error_disk_full)
            }
            V5_ERR_USER_SPACE_FULL -> {  // -40025//用户配额空间已满|
            }
            V5_ERR_DB_ERROR -> {  //  -40026//数据库错误|
            }
            V5_ERR_DISK_NOT_MOUNTED -> {  //  -40030//磁盘未挂载|
                info = context.getString(R.string.disk_status_abnormal)
            }
            V5_ERR_DISK_UNFORMATTED -> {  //  -40031//磁盘未格式化|
            }
            V5_ERR_DISK_RAID_NOT_EXISTED -> {  //  -40032//raid不存在|
            }
            V5_ERR_DISK_LVM_NOT_EXISTED -> {  //  -40033//lvm不存在|
            }
            V5_ERR_DISK_LVM_STOP_DEV -> {  //  -40034//lvm不能停止设备|
            }
            V5_ERR_DISK_RAID_STOP_DEV -> {  //  -40035//raid不能停止设备|
            }
            V5_ERR_DISK_BUILDING -> {  //  -40041//磁盘构建中
            }
            V5_ERR_DISK_MAIN_NOT_EXIST -> {  //  -40042//主磁盘不存在
            }
            V5_ERR_DISK_NOT_SELECTED_MAIN -> {  //  -40043//不是选择的主磁盘
            }
            V5_ERR_DISK_NOT_SELECT_MAIN -> {  //  -40044//没有选择主磁盘
            }
            V5_ERR_DISK_MAIN_CANNOT_MOUNTED -> {  //  -40045//不能挂载主磁盘
            }
            V5_ERR_DISK_MAIN_ERROR -> {  //  -40046//主磁盘错误
            }
            V5_ERR_DISK_NUM_TO_RAID -> {  //  -40047//磁盘数目错误，无法创建Raid
            }
            V5_ERR_FILE_INITIALIZING -> {  //  -40048//文件基础服务初始化中...
            }
            V5_ERR_USER_NOT_BIND -> {  //  -45111//用户未绑定此设备|
            }
            V5_ERR_FILE_CAN_NOT_COPY_INTO_SELF -> {  //  -45112//不能循环拷贝自身|
            }
            ERROR_40500 -> {  // "群名已存在"
                info = context.getString(R.string.group_already_exists)
            }
            ERROR_40501 -> {  // "创建群已满"
                info = context.getString(R.string.number_of_groups_reached_the_limit)
            }
            ERROR_40504-> {  //"目标未加入该群"
                info = context.getString(R.string.msg_error_user_no_exist)
            }

        }


        return if (addErrorNo) {
            "${info ?: context.getString(R.string.ec_request)}($errorCode)"
        } else {
            info ?:"${context.getString(R.string.ec_request)}$errorCode"
        }

    }
}