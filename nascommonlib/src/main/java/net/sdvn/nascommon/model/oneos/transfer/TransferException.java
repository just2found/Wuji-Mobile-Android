package net.sdvn.nascommon.model.oneos.transfer;

/**
 * Created by gaoyun@eli-tech.com on 2016/2/18.
 */
public enum TransferException {
    NONE,
    UNKNOWN_EXCEPTION,
    LOCAL_SPACE_INSUFFICIENT,//本地空間不足
    SERVER_SPACE_INSUFFICIENT,//雲空间不足
    FAILED_REQUEST_SERVER,//请求服务器错误
    ENCODING_EXCEPTION,//解析异常
    IO_EXCEPTION,//
    FILE_NOT_FOUND,//文件找不到
    SERVER_FILE_NOT_FOUND,//源文件未发现
    SOCKET_TIMEOUT,//连接超时
    WIFI_UNAVAILABLE,
    SSUDP_DISCONNECTED,
    SOURCE_NOT_FOUND,//源服务未发现
    SOURCE_EXPIRED,//过期
    DES_NOT_FOUND,
    TEMPORARY_FILE_NOT_FOUND,//临时文件丢失
    AUTH_EXP,
    NO_PERM
}
