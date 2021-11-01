package net.sdvn.common.internet;


import android.text.TextUtils;

public class SdvnHttpErrorNo {
    public static final int EC_NOT_NET = -401;
    public static final int EC_REQUEST = -402;
    public static final int EC_TIME_OUT = -403;

    public static final int EC_EXCEPTION = 100;     //异常抛出错误
    public static final int EC_INVALID_TICKET = 101;//ticket无效
    public static final int EC_INVALID_PARTNERID_OR_APPID = 102;//partnerid或appid无效
    public static final int EC_INVALID_PARAMS = 103;//参数错误
    public static final int EC_INVALID_TOKEN = 104;//token无效
    public static final int EC_INVALID_DOWNLAODTOKEN = 105;//downloadtoken无效
    public static final int EC_INVALID_SHARETOKEN = 106;//sharetoken无效
    public static final int EC_NO_PERMISSION = 107;//非法操作 无权限
    public static final int EC_DEVICE_OFFLINE = 108;//设备不在线
    public static final int EC_AUXCODE = 109;    //验证码错误
    public static final int EC_INVALID_QRCODE = 110;    //二维码无效
    public static final int EC_QRCODE_NOT_SCANED = 111;    //二维码未扫描
    public static final int EC_INVALID_ACCOUNT = 112;    //没有此用户
    public static final int EC_SEND_AUXCODE = 113;    //发送验证码失败
    public static final int EC_INVALID_SHARECODE = 114;    //分享码失效
    public static final int EC_VIP_CONFLICT = 115;    //vip与ticket不符合
    public static final int EC_SIG = 116;    //sig校验失败
    public static final int EC_INVALID_DEVICE = 117;    //没有此设备
    public static final int EC_QRCODE_TIMEOUT = 118;    //二维码超时
    public static final int EC_USERID_CONFILICT = 119;    //userid与ticket不符合
    public static final int EC_INVALID_SHARE_T2 = 120;    //t2无效
    public static final int EC_ALREADY_BOUND = 121;    //用户已与设备绑定
    public static final int EC_INVALID_CHARS = 122;    //用户已与设备绑定

    public static final int EC_NULL_PARTNERID = 201;//partid is empty
    public static final int EC_NULL_APPID = 202;//appid is empty
    public static final int EC_NULL_RANDOM = 203;//random is empty
    public static final int EC_NULL_REQUEST_PACKET = 204;//requset packet is empty
    public static final int EC_NULL_SIG = 205;//sig is empty
    public static final int EC_NULL_USERID = 211;//userid is empty
    public static final int EC_NULL_DEVICEID = 212;//deviceid is empty
    public static final int EC_NULL_DEVICESN = 213;//devicesn is empty
    public static final int EC_NULL_USERNAME = 214;//username is empty
    public static final int EC_NULL_PASSWD = 215;//password is empty
    public static final int EC_NULL_USERS = 216;//users is empty
    public static final int EC_NULL_BINDS = 217;//binds is empty
    public static final int EC_NULL_UNBINDS = 218;//unbinds is empty
    public static final int EC_NULL_DEVICES = 219;//devices is empty
    public static final int EC_NULL_NETWORKID = 220;//networkid is empty
    public static final int EC_NULL_NEWSID = 221;//newsid is empty
    public static final int EC_NULL_NEWS_PROCESS = 222;//news process is empty
    public static final int EC_NULL_NET_NAME = 223;//networkname is empty
    public static final int EC_NULL_MEMBERS = 224;//members is empty
    public static final int EC_NULL_MBPOINT = 225;//mbpoint is empty
    public static final int EC_NULL_MGRLEVEL = 226;//mgrlevel is empty/invaild mgrlevel

    //     {"result":300,"errmsg":"email already existed"}
    public static final int EC_INVALID_JSON = 301;//请求数据包无效的json字符串
    public static final int EC_INVALID_FAILE_MODE = 302;//无效的故障模式：？
    public static final int EC_USERNAME_HAS_REGISTERED = 303;//用户名已注册
    public static final int EC_PASSWD_NO_MATCH = 304;//oldpassword不正确
    public static final int EC_NEWS_HAS_BEEN_PROCESSED = 305;//消息已处理
    public static final int EC_DEVICE_HAS_BEEN_BINDED = 306;//设备已绑定
    public static final int EC_HAS_JOINED_THE_NETWORK = 307;//用户已加入网络
    public static final int EC_HAS_APPLYED_TO_JOIN_THE_NETWORK = 308;//用户已申请加入网络
    public static final int EC_INVALID_NEWS_TYPE = 309;    //无效的消息类型
    public static final int EC_THIS_NEWS_DOES_NOT_BELONG_TO_THIS_USER = 310;    //	这不是该用户的消息
    public static final int EC_INVALID_APPLY_NEWS = 311;    //	无效的消息,无法找到申请加入记录,可能已被删除
    public static final int EC_INVALID_BIND_NEWS = 312;    //无效的消息,无法找到申请绑定记录,可能已被删除
    public static final int EC_DEVICE_BIND_APPLY_HAS_BEEN_SUBMIT = 313;//设备绑定申请已提交
    public static final int EC_PHONE_HAS_REGISTERED = 314;//手机号已经存在
    public static final int EC_EMAIL_HAS_REGISTERED = 315;//电子邮件已经存在
    public static final int EC_USER_IS_NOT_BOUND_TO_THIS_DEVICE = 316;//用户没有绑定此设备
    public static final int EC_USER_SET_OUT_OF_RANGE = 320;//用户设置的值超出范围

    public static final int EC_SETTING_INVALID_DELAYTIME = 321;//	invalid delaytime	无效的延迟时间
    public static final int EC_USER_NOT_JOINED_THIS_NETWORK = 322;//	User does not joined this network	用户没有加入此网络
    public static final int EC_DEVICE_NOT_JOINED_THIS_NETWORK = 323;//	Device does not joined this network	设备没有加入此网络
    public static final int EC_DEVICE_JOINED_THIS_NETWORK = 324;//	Device has join this network	设备已经加入此网络
    public static final int EC_INVALID_FEETYPE = 325;//	invalid feetype	无效的费用类型
    public static final int EC_INVALID_FEEID = 326;//	invalid feeid	无效的费用ID
    public static final int EC_INVALID_SHARE_VALUE = 327;//invalid share value	无效的分成值
    public static final int EC_INVALID_RESID = 328;//	invalid resid	无效的资源ID
    //msg
    public static final int EC_MSG_NO_NEED_PROCESSED = 329;//	The message needs no confirmation	消息无需确认
    public static final int EC_MSG_HAS_ALREADY_AGREE = 330;    //The message has been agreed	消息已经同意
    public static final int EC_MSG_HAS_ALREADY_DISAGREE = 331;    //The message has been refused	消息已被拒绝
    public static final int EC_MSG_HAS_ALREADY_EXPIRED = 332;    //The message has expired	消息已过期


    public static final int EC_DISABLED_APPID = 401;//该appid已禁用
    public static final int EC_OVERDUE_APPID = 402;//该appid已经过期
    public static final int EC_API_NOT_AUTHORIZED = 403;//api未经授权
    public static final int EC_OVER_LIMIT_RIGSTER_USER = 404;//注册用户数超过限制：最大为？，当前为？
    public static final int EC_USERS_OVER_LIMIT_BY_SYS = 405;//批量注册用户数超过限制：最大为？，当前为？，增加为？
    public static final int EC_DEVICES_BINDED_ACCOUNT_OVER_LIMITED = 406;//设备绑定帐户超过限制
    public static final int EC_NETWORK_JOINED_ACCOUNT_LIMITED = 407;//网络加入帐户超过限制
    public static final int EC_USER_JOINED_NETWORK_LIMITED = 408;//用户加入网络超过限制
    public static final int EC_USER_CREATE_NETWORK_LIMITED = 409;//用户创建网络超过限制
    public static final int EC_APP_HAS_FORBID_MBPOINT_TRANSFER = 410;//此APP禁止积分转让
    public static final int EC_DEVICE_OWNER_CANNOT_UNBIND_DIRECTLY = 411;//设备拥有者不能直接解绑
    public static final int EC_MUST_DEVICE_OWNER_CAN_GRADE_USER_BIND_LEVEL = 412;//只有设备拥有者才能更改用户绑定级别
    public static final int EC_EN_SERVERS_LIMITED = 418;//The number of EN servers in the circle has reached the upper limit	圈子中的EN服务器数量已达上线
    public static final int EC_MOVING_EN_SERVER = 432;//Before moving the EN server, please cancel the EN service	移除EN服务器前,请先取消EN服务
    public static final int EC_BIND_EN_SERVER = 433;//Unable to bind EN server	无法绑定EN服务器
    public static final int EC_UNBINDING_EN_SERVER = 434;//Before unbinding the EN server, please cancel the EN service first	解绑EN服务器前，请先取消EN服务
    public static final int EC_LEAVING_CIRCLE_CANCEL_EN = 435;//Before leaving the circle, please cancel the EN service in the circle	退出圈子前，请先取消在圈子中的EN服务
    public static final int EC_AT_LEAST_ONE_FEE_TYPE = 436;//At least one item must be activated for this fee type	此费用类型至少需要启用一项
    public static final int EC_CANNOT_DELETE_EN_SERVER_OWNER = 438;//Cannot delete users who provide EN services in circles	无法删除在圈子中提供EN服务的用户

    public static final int EC_INVALID_SIG = 501;//无效sig，请通过appkey和random生成sig
    public static final int EC_NOT_EXISTS_PARTERID = 502;//找不到合作伙伴的合作伙伴
    public static final int EC_APPID_PARTRID_NOT_MATCH = 503;//appid和合作伙伴不匹配
    public static final int EC_NOT_FOUND_USER = 504;//无法通过userid =查找用户？
    public static final int EC_NOT_FOUND_DEVICE_BY_ID = 505;//无法通过deviceid =查找设备？
    public static final int EC_NOT_FOUND_DEVICE_BY_SN = 506;//无法按设备查找设备n =？
    public static final int EC_SN_NOT_EXIST = 507;//设备不存在
    public static final int EC_USER_ATTR_NOT_MATCH = 508;//找到用户attr不匹配：by userid = ?，请求值是partid =？ appid = ?，实际值是partid =？ appid =？
    public static final int EC_DEV_ATTR_NOT_MATCH_BY_ID = 509;//找到设备attr不匹配：by deviceid = ?，请求值为partid =？ appid = ?，实际值是partid =？ appid =？
    public static final int EC_DEV_ATTR_NOT_MATCH_BY_SN = 510;//查找设备属性不匹配：by devicesn = ?，请求值为partnerid =？ appid = ?，实际值是partnerid =？ appid =？
    public static final int EC_NOT_FOUNT_USER_BY_LOGINNAME = 511;//不能通过loginname =查找用户？
    public static final int EC_USER_NOT_ADD_DEV = 512;//用户未绑定到设备
    public static final int EC_USER_NOT_ADMIN = 513;//用户不是设备管理员
    public static final int EC_NETWORKID_NOT_FIND = 514;//无法通过networkid =找到网络？
    public static final int EC_NEWSID_NOT_FIND = 515;//无法通过此newsid =找到消息？
    public static final int EC_NOT_THE_NET_ADMIN = 516;//用户不是网络管理员
    public static final int EC_CAN_NOT_DELETE_DEAULT_NETWORK = 517;//此网络是用户默认网络，无法删除
    public static final int EC_THE_USER_CAN_NOT_BE_SEARCHED = 518;//	用户授权他不能被搜索
    public static final int EC_THE_USER_HAS_JOINED_THE_NETWORK = 519;//	用户已经加入此网络
    public static final int EC_HAS_BEEN_INVITED = 520;//	等待用户同意加入网络
    public static final int EC_CAN_NOT_DELETE_ADMIN = 521;//	不能删除网络管理员
    public static final int EC_DEVICE_NOT_OF_USER = 522;//	设备不属于此用户
    public static final int EC_NO_ENOUGH_SCORE_TO_TRANFER = 523;//	没有足够的积分可够转让
    public static final int EC_FIND_MORE_THAN_ONE_USER = 524;//	找到多于一个用户
    public static final int EC_NO_SUCH_SETTING = 526;//	找到多于一个用户
    public static final int EC_NO_SCORES_TO_RECEIVE = 528;// 没有可领取的积分
    public static final int EC_SCORES_HAVE_BEEN_RECEIVED = 529;//	积分已被领取,请勿重复操作
    public static final int EC_AMOUNT_IS_ABNORMAL = 530;//The amount is abnormal, please buy again	金额异常，请重新购买
    public static final int EC_INSUFFICIENT_SCORE = 531;//Insufficient NB	积分不足
    public static final int EC_MOVE_EN_SERVER_FROM_CIRCLE = 532;//无法添加其他圈子中的EN服务器
    public static final int EC_ALREADY_APPLIED = 540;//Already applied	已申请


    public static final int EC_BATCH_OVER_LIMIT_REGISTER_USER = 601;//用户寄存器的批量大小超过限制：用户大小为？，限制数量为100
    public static final int EC_BATCH_OVER_LIMIT_BINDS_USER = 602;//设备绑定的批量大小超过限制：绑定大小为？，限制数量为100
    public static final int EC_BATCH_OVER_LIMIT_BINDS_DEV = 603;//设备绑定的批量大小超出限制：设备大小为？，限制数量为100
    public static final int EC_BATCH_OVER_LIMIT_UNBINDS_USER = 604;//设备取消绑定的批量大小超过限制：取消绑定大小为？，限制数量为100
    public static final int EC_BATCH_OVER_LIMIT_UNBINDS_DEV = 605;//设备取消绑定的批量大小超过限制：设备大小为？，限制数量为100

    public static final int EC_ORDER_THIRD_SERVER_ERROR = 1001; //连接三方支付服务器异常
    public static final int EC_ORDER_THIRD_DATE_ERROR = 1003;   //三方支付回传数据解析错误
    public static final int EC_ORDER_AMOUNT_ERROR = 1004;       // 三方支付支付金额与本地订单金额不一致
    public static final int EC_ORDER_SERVER_ERROR = 1006;       //服务器内部错误
    public static final int EC_ORDER_ID_ERROR = 1007;           //三方支付回传订单号与本地订单号不一致
    public static final int EC_ORDER_NOT_EXIST = 1008;          //订单信息不存在,非法的回调请求
    public static final int EC_ORDER_SUBMITTED = 1009;          //订单信息订单已经成功，不处理
    public static final int EC_ORDER_TYPE_ERROR = 1010;         //支付类型不支持
    public static final int EC_ORDER_PAY_FAILD = 1011;          //本地接口收到三方支付失败
    public static final int EC_ORDER_USER_ERROR = 1012;         //无效的用户
    public static final int EC_ORDER_AMOUNT_OVER_LIMIT = 1013;  //支付金额超出限制

    public static String ec2String(int error, Object... args) {
        return String.format(ec2String(error), args);
    }

    public static String ec2String(int error) {
        return ec2String(error, "");
    }

    //HTTP 429 Too Many Requests
    public static String ec2String(int error, String errmsg) {
        int resId = ec2ResId(error);
        if (resId == R.string.ec_request && !TextUtils.isEmpty(errmsg)) {
            if (errmsg.contains("429")) {
                return OkHttpClientIns.getContext().getString(R.string.ec_too_many_requests) + "(429)";
            }
            return errmsg + "(" + error + ")";
        } else
            return OkHttpClientIns.getContext().getString(resId) + "(" + error + ")";
    }

    public static int ec2ResId(int error) {
        int errResid;
        switch (error) {
            case EC_NOT_NET:
                errResid = R.string.error_string_no_network;
                break;
            case EC_REQUEST:
                errResid = R.string.ec_request;
                break;
            case EC_TIME_OUT:
                errResid = R.string.ec_timeout;
                break;
            //100
            case EC_EXCEPTION:
                errResid = R.string.ec_exception;
                break;
            case EC_INVALID_TICKET:
                errResid = R.string.ec_invalid_ticket;
                break;
            case EC_INVALID_PARTNERID_OR_APPID:
                errResid = R.string.ec_invalid_partnerid_or_appid;
                break;
            case EC_INVALID_PARAMS:
                errResid = R.string.ec_invalid_params;
                break;
            case EC_INVALID_TOKEN:
                errResid = R.string.ec_invalid_token;
                break;
            case EC_INVALID_DOWNLAODTOKEN:
                errResid = R.string.ec_invalid_downlaodtoken;
                break;
            case EC_INVALID_SHARETOKEN:
                errResid = R.string.ec_invalid_sharetoken;
                break;
            case EC_NO_PERMISSION:
                errResid = R.string.ec_no_permission;
                break;
            case EC_DEVICE_OFFLINE:
                errResid = R.string.ec_device_offline;
                break;
            case EC_AUXCODE:
                errResid = R.string.ec_auxcode;
                break;
            case EC_INVALID_QRCODE:
                errResid = R.string.ec_invalid_qrcode;
                break;
            case EC_QRCODE_NOT_SCANED:
                errResid = R.string.ec_qrcode_not_scaned;
                break;
            case EC_INVALID_ACCOUNT:
                errResid = R.string.ec_invalid_account;
                break;
            case EC_SEND_AUXCODE:
                errResid = R.string.ec_send_auxcode;
                break;
            case EC_INVALID_SHARECODE:
                errResid = R.string.ec_invalid_sharecode;
                break;
            case EC_VIP_CONFLICT:
                errResid = R.string.ec_vip_conflict;
                break;
            case EC_SIG:
                errResid = R.string.ec_sig;
                break;
            case EC_INVALID_DEVICE:
                errResid = R.string.ec_invalid_device;
                break;
            case EC_QRCODE_TIMEOUT:
                errResid = R.string.ec_qrcode_timeout;
                break;
            case EC_USERID_CONFILICT:
                errResid = R.string.ec_userid_confilict;
                break;
            case EC_INVALID_SHARE_T2:
                errResid = R.string.ec_invalid_t2;
                break;
            case EC_ALREADY_BOUND:
                errResid = R.string.ec_already_bound;
                break;
            case EC_INVALID_CHARS:
                errResid = R.string.ec_invalid_chars;
                break;
            //200
            case EC_NULL_PARTNERID:
                errResid = R.string.ec_null_partnerid;
                break;
            case EC_NULL_APPID:
                errResid = R.string.ec_null_appid;
                break;
            case EC_NULL_RANDOM:
                errResid = R.string.ec_null_random;
                break;
            case EC_NULL_REQUEST_PACKET:
                errResid = R.string.ec_null_request_packet;
                break;
            case EC_NULL_SIG:
                errResid = R.string.ec_null_sig;
                break;
            case EC_NULL_USERID:
                errResid = R.string.ec_null_userid;
                break;
            case EC_NULL_DEVICEID:
                errResid = R.string.ec_null_deviceid;
                break;
            case EC_NULL_DEVICESN:
                errResid = R.string.ec_null_devicesn;
                break;
            case EC_NULL_USERNAME:
                errResid = R.string.ec_null_username;
                break;
            case EC_NULL_PASSWD:
                errResid = R.string.ec_null_passwd;
                break;
            case EC_NULL_USERS:
                errResid = R.string.ec_null_users;
                break;
            case EC_NULL_BINDS:
                errResid = R.string.ec_null_binds;
                break;
            case EC_NULL_UNBINDS:
                errResid = R.string.ec_null_unbinds;
                break;
            case EC_NULL_DEVICES:
                errResid = R.string.ec_null_devices;
                break;
            case EC_NULL_NETWORKID:
                errResid = R.string.ec_null_networkid;
                break;
            case EC_NULL_NEWSID:
                errResid = R.string.ec_null_newsid;
                break;
            case EC_NULL_NEWS_PROCESS:
                errResid = R.string.ec_null_news_process;
                break;
            case EC_NULL_NET_NAME:
                errResid = R.string.ec_null_net_name;
                break;
            case EC_NULL_MEMBERS:
                errResid = R.string.ec_null_members;
                break;
            case EC_NULL_MBPOINT:
                errResid = R.string.ec_null_mbpoint;
                break;
            case EC_NULL_MGRLEVEL:
                errResid = R.string.ec_null_mgrlevel;
                break;

            //300
            case EC_INVALID_JSON:
            case EC_INVALID_FAILE_MODE:
                errResid = R.string.ec_invalid_params;
                break;
            case EC_USERNAME_HAS_REGISTERED:
                errResid = R.string.ec_username_has_registered;
                break;
            case EC_PASSWD_NO_MATCH:
                errResid = R.string.ec_passwd_no_match;
                break;
            case EC_NEWS_HAS_BEEN_PROCESSED:
                errResid = R.string.ec_news_has_been_processed;
                break;
            case EC_DEVICE_HAS_BEEN_BINDED:
            case EC_DEVICE_BIND_APPLY_HAS_BEEN_SUBMIT:
                errResid = R.string.ec_bound_or_requested_to_bind_the_device;
                break;
            case EC_PHONE_HAS_REGISTERED:
                errResid = R.string.ec_phone_has_registered;
                break;
            case EC_EMAIL_HAS_REGISTERED:
                errResid = R.string.ec_email_has_registered;
                break;
            case EC_USER_IS_NOT_BOUND_TO_THIS_DEVICE:
                errResid = R.string.ec_user_is_not_bound_to_this_device;
                break;
            case EC_HAS_JOINED_THE_NETWORK:
            case EC_HAS_APPLYED_TO_JOIN_THE_NETWORK:
                errResid = R.string.ec_joined_or_applied_to_join_the_network;
                break;
            case EC_INVALID_NEWS_TYPE:
                errResid = R.string.ec_invalid_news_type;
                break;
            case EC_THIS_NEWS_DOES_NOT_BELONG_TO_THIS_USER:
                errResid = R.string.ec_this_news_does_not_belong_to_this_user;
                break;
            case EC_INVALID_APPLY_NEWS:
            case EC_INVALID_BIND_NEWS:
                errResid = R.string.ec_invalid_news;
                break;
            case EC_USER_SET_OUT_OF_RANGE:
                errResid = R.string.ec_user_set_out_of_range;
                break;
            case EC_MSG_HAS_ALREADY_AGREE:
                errResid = R.string.ec_msg_has_agreed;
                break;
            case EC_MSG_HAS_ALREADY_DISAGREE:
                errResid = R.string.ec_msg_has_denied;
                break;
            case EC_MSG_HAS_ALREADY_EXPIRED:
                errResid = R.string.ec_msg_has_expired;
                break;
            //400
            case EC_DISABLED_APPID:
                errResid = R.string.ec_disabled_appid;
                break;
            case EC_OVERDUE_APPID:
                errResid = R.string.ec_overdue_appid;
                break;
            case EC_API_NOT_AUTHORIZED:
                errResid = R.string.ec_api_not_authorized;
                break;
            case EC_OVER_LIMIT_RIGSTER_USER:
            case EC_USERS_OVER_LIMIT_BY_SYS:
                errResid = R.string.ec_over_limit_rigster_user;
                break;
            case EC_DEVICES_BINDED_ACCOUNT_OVER_LIMITED:
                errResid = R.string.ec_over_limit_device_accounts;
                break;
            case EC_NETWORK_JOINED_ACCOUNT_LIMITED:
                errResid = R.string.ec_over_limit_users_of_the_network;
                break;
            case EC_USER_JOINED_NETWORK_LIMITED:
                errResid = R.string.ec_over_limit_network_of_the_user;
                break;
            case EC_USER_CREATE_NETWORK_LIMITED:
                errResid = R.string.ec_user_create_network_limited;
                break;
            case EC_APP_HAS_FORBID_MBPOINT_TRANSFER:
                errResid = R.string.ec_app_has_forbid_mbpoint_transfer;
                break;
            case EC_DEVICE_OWNER_CANNOT_UNBIND_DIRECTLY:
                errResid = R.string.ec_device_owner_cannot_unbind_directly;
                break;
            case EC_MUST_DEVICE_OWNER_CAN_GRADE_USER_BIND_LEVEL:
                errResid = R.string.ec_must_device_owner_can_grade_user_bind_level;
                break;
            case EC_EN_SERVERS_LIMITED:
                errResid = R.string.ec_en_servers_limited;
                break;
            case EC_MOVING_EN_SERVER:
                errResid = R.string.ec_moving_en_server;
                break;
            case EC_BIND_EN_SERVER:
                errResid = R.string.ec_bind_en_server;
                break;
            case EC_UNBINDING_EN_SERVER:
                errResid = R.string.ec_unbinding_en_server;
                break;
            case EC_LEAVING_CIRCLE_CANCEL_EN:
                errResid = R.string.ec_leaving_circle_cancel_EN;
                break;
            case EC_AT_LEAST_ONE_FEE_TYPE:
                errResid = R.string.ec_at_least_one_fee_type;
                break;
            case EC_CANNOT_DELETE_EN_SERVER_OWNER:
                errResid = R.string.ec_cannot_delete_en_server_owner;
                break;

            //500
            case EC_INVALID_SIG:
                errResid = R.string.ec_invalid_sig;
                break;
            case EC_NOT_EXISTS_PARTERID:
                errResid = R.string.ec_not_exists_parterid;
                break;
            case EC_APPID_PARTRID_NOT_MATCH:
                errResid = R.string.ec_appid_partrid_not_match;
                break;
            case EC_NOT_FOUND_USER:
                errResid = R.string.ec_not_found_user;
                break;
            case EC_NOT_FOUND_DEVICE_BY_ID:
                errResid = R.string.ec_not_found_device_by_id;
                break;
            case EC_NOT_FOUND_DEVICE_BY_SN:
                errResid = R.string.ec_not_found_device_by_sn;
                break;
            case EC_SN_NOT_EXIST:
                errResid = R.string.ec_sn_not_exist;
                break;
            case EC_USER_ATTR_NOT_MATCH:
                errResid = R.string.ec_user_attr_not_match;
                break;
            case EC_DEV_ATTR_NOT_MATCH_BY_ID:
                errResid = R.string.ec_dev_attr_not_match_by_id;
                break;
            case EC_DEV_ATTR_NOT_MATCH_BY_SN:
                errResid = R.string.ec_dev_attr_not_match_by_sn;
                break;
            case EC_NOT_FOUNT_USER_BY_LOGINNAME:
                errResid = R.string.ec_not_fount_user_by_loginname;
                break;
            case EC_USER_NOT_ADD_DEV:
                errResid = R.string.ec_user_not_add_dev;
                break;
            case EC_USER_NOT_ADMIN:
                errResid = R.string.ec_user_not_admin;
                break;
            case EC_NETWORKID_NOT_FIND:
                errResid = R.string.ec_not_found_this_networkid;
                break;
            case EC_NEWSID_NOT_FIND:
                errResid = R.string.ec_not_found_this_newsid;
                break;
            case EC_NOT_THE_NET_ADMIN:
                errResid = R.string.ec_not_the_net_admin;
                break;
            case EC_CAN_NOT_DELETE_DEAULT_NETWORK:
                errResid = R.string.ec_can_not_delete_deault_network;
                break;
            case EC_THE_USER_CAN_NOT_BE_SEARCHED:
                errResid = R.string.ec_the_user_can_not_be_searched;
                break;
            case EC_THE_USER_HAS_JOINED_THE_NETWORK:
            case EC_HAS_BEEN_INVITED:
                errResid = R.string.ec_joined_or_invited_to_join_the_network;
                break;
            case EC_CAN_NOT_DELETE_ADMIN:
                errResid = R.string.ec_can_not_delete_admin;
                break;
            case EC_DEVICE_NOT_OF_USER:
                errResid = R.string.ec_device_not_of_user;
                break;
            case EC_NO_ENOUGH_SCORE_TO_TRANFER:
                errResid = R.string.ec_no_enough_mbpoion_to_tranfer;
                break;
            case EC_FIND_MORE_THAN_ONE_USER:
                errResid = R.string.ec_find_more_than_one_user;
                break;
            case EC_NO_SCORES_TO_RECEIVE:
            case EC_SCORES_HAVE_BEEN_RECEIVED:
                errResid = R.string.have_received;
                break;
            case EC_AMOUNT_IS_ABNORMAL:
                errResid = R.string.ec_amount_is_abnormal;
                break;
            case EC_INSUFFICIENT_SCORE:
                errResid = R.string.ec_insufficient_score;
                break;
            case EC_MOVE_EN_SERVER_FROM_CIRCLE:
                errResid = R.string.ec_move_en_server_from_circle;
                break;
            case EC_ALREADY_APPLIED:
                errResid = R.string.ec_already_applied;
                break;

            //600
            case EC_BATCH_OVER_LIMIT_REGISTER_USER:
                errResid = R.string.ec_batch_over_limit_register_user;
                break;
            case EC_BATCH_OVER_LIMIT_BINDS_USER:
                errResid = R.string.ec_batch_over_limit_binds_user;
                break;
            case EC_BATCH_OVER_LIMIT_BINDS_DEV:
                errResid = R.string.ec_batch_over_limit_binds_dev;
                break;
            case EC_BATCH_OVER_LIMIT_UNBINDS_USER:
                errResid = R.string.ec_batch_over_limit_unbinds_user;
                break;
            case EC_BATCH_OVER_LIMIT_UNBINDS_DEV:
                errResid = R.string.ec_batch_over_limit_unbinds_dev;
                break;
            //1000
            case EC_ORDER_THIRD_SERVER_ERROR:
            case EC_ORDER_THIRD_DATE_ERROR:
            case EC_ORDER_AMOUNT_ERROR:
            case EC_ORDER_SERVER_ERROR:
            case EC_ORDER_ID_ERROR:
            case EC_ORDER_NOT_EXIST:
            case EC_ORDER_SUBMITTED:
            case EC_ORDER_TYPE_ERROR:
            case EC_ORDER_PAY_FAILD:
            case EC_ORDER_USER_ERROR:
                errResid = R.string.ec_order_submit_error;
                break;
            case EC_ORDER_AMOUNT_OVER_LIMIT:
                errResid = R.string.ec_order_amount_over_limit;
                break;
            default:
                errResid = R.string.ec_request;
        }
        return errResid;
    }
}


