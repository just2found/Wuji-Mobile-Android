package net.sdvn.common;


import net.sdvn.cmapi.global.Constants;
import net.sdvn.common.internet.R;

/**
 * Created by yun on 18/05/10.
 */

public class ErrorCode {
    public static int dr2String(int error) {
        int errResid = R.string.error_string_socket;
        switch (error) {
            case Constants.DR_MISVERSION:
            case Constants.DR_NETWORK:
            case Constants.DR_TUN_DEVICE:
                errResid = R.string.error_string_socket;
                break;
            case Constants.DR_INVALID_USER:
            case Constants.DR_INVALID_PASS:
                errResid = R.string.error_string_invalid_account;
                break;
            case Constants.DR_MAX_DEVICE:
                errResid = R.string.error_string_max_device;
                break;
            case Constants.DR_NO_NETWORK:
                errResid = R.string.error_string_no_network;
                break;
            case Constants.DR_DEVICE_DISABLED:
                errResid = R.string.error_string_device_disabled;
                break;
            case Constants.DR_DEVICE_ONLINE:
                errResid = R.string.error_string_device_dup;
                break;
            case Constants.DR_DEVICE_DELETED:
                errResid = R.string.error_string_device_locked;
                break;
            case Constants.DR_KO_USER_REMOVED:
                errResid = R.string.KR_Member_Removed;
                break;
            case Constants.DR_KO_DEVICE_REMOVED:
                errResid = R.string.KR_Device_Removed;
                break;
            case Constants.DR_KO_DEVICE_DELETED:
                errResid = R.string.KR_Deivce_Deactived;
                break;
            case Constants.DR_INVALID_SMS:
                errResid = R.string.dr_invalid_sms;
                break;
            case Constants.DR_VPN_PERMISSION_DENIED:
                errResid = R.string.dr_vpn_permission_denied;
                break;
            case Constants.DR_CONNECT_TIMEOUT:
                errResid = R.string.error_string_socket;
                break;
            case Constants.DR_VPN_TUNNEL_IS_OCCUPIED:
                errResid = R.string.dr_vpn_tunnel_is_occupied;
                break;
            case Constants.DR_READ_PHONE_STATE_PERMISSION_DENIED:
                errResid = R.string.dr_read_phone_state_permission_denied;
                break;
        }

        return errResid;
    }

    public static int error2String(int error) {
        int errResid = R.string.error_string_socket;
        switch (error) {
            case Constants.CE_SUCC://0
                errResid = R.string.success;
                break;
            case Constants.CE_FAIL://1
                break;
            case Constants.CE_SOCKET://2
            case Constants.CE_DISCONNECTED://3
            case Constants.CE_VERSION_DISMATCHED://4
            case Constants.CE_PROTOCOL://5
            case Constants.CE_TIMEOUT://6
            case Constants.CE_NETWORK_UNREACHABLE://= 26;
                errResid = R.string.error_string_socket;
                break;
            case Constants.CE_NO_GATEWAY://10
                errResid = R.string.error_string_gw_busy;
                break;
            case Constants.CE_INVALID_PASS://8
            case Constants.CE_INVALID_USER://7
                errResid = R.string.error_string_invalid_account;
                break;
            case Constants.CE_MAX_DEVICE://9
                errResid = R.string.error_string_max_device;
                break;
            case Constants.CE_NO_NETWORK://11
                errResid = R.string.error_string_no_network;
                break;
            case Constants.CE_DEVICE_DISABLED://14
            case Constants.CE_DEVICE_NOTEXISTS://12
                errResid = R.string.error_string_device_disabled;
                break;
            case Constants.CE_DEVICE_ONLINED://13
                errResid = R.string.error_string_device_dup;
                break;
            case Constants.CE_USER_UNACTIVE://15
                errResid = R.string.error_string_user_unactive;
                break;

            case Constants.CE_USER_LOCKED://16
                errResid = R.string.error_string_user_locked;
                break;
            case Constants.CE_INVALID_TUN_DEVICE://17
            case Constants.CE_CANCEL:// = 18;
            case Constants.CE_REDIRECT://= 19;

            case Constants.CE_AUX_AUTH_DISMATCH://= 20;

            case Constants.CE_INVALID_PARTNERID:// = 21;
            case Constants.CE_INVALID_APPID://= 22;

            case Constants.CE_PENDING://= 23;
                errResid = R.string.error_string_pending;
                break;
            case Constants.CE_JSON_EXCEPTION://= 40001;

            case Constants.CE_STATUS://= 24;
            case Constants.CE_UNINITIALIZED://= 25;

            case Constants.CE_INVALID_AUTHORIZATION://= 27;
            case Constants.CE_UNKNOWN_DEVCODE://= 28;
            case Constants.CE_INVALID_SN:// = 29;
            case Constants.CE_NO_SN_SELECTED://= 30;

            case Constants.CE_OPERATION_DENIED://= 31;


            case Constants.CE_INVALID_SMS:// = 33;
            case Constants.CE_INVALID_TICKET://= 34;

            case Constants.CE_TRY_TOO_MANY_TIMES://= 35;

            case Constants.CE_INVALID_DEIVE_CLASS://= 36;

            case Constants.CE_CALL_THIRD_API_FAIL://= 37;

            case Constants.CE_MEMERY_OUT://= 32;
            case Constants.CE_INVALID_CODE://= 38;

            case Constants.CE_CORE_TIMEOUT://= 1000;

            case Constants.CE_INVALID_PARAMETER://= 1001;

                break;
        }
        return errResid;
    }

    public static int ec2String(int devDisableReason) {
        switch (devDisableReason) {
            case 1:
                return R.string.ec_insufficient_points;
            case 2:
                return R.string.flow_is_expire;
            default:
                break;
        }
        return R.string.unknown_exception;
    }
}
