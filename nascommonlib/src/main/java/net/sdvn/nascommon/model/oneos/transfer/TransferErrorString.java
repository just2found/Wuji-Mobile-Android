package net.sdvn.nascommon.model.oneos.transfer;

import android.content.Context;

import androidx.annotation.NonNull;

import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommonlib.R;

public class TransferErrorString {
    public static String getFailedInfo(@NonNull Context context, @NonNull TransferElement mElement, boolean isDownload) {
        String failedInfo = null;

        if (!Utils.isWifiAvailable(context)) {
            mElement.setException(TransferException.WIFI_UNAVAILABLE);
        }

        TransferException failedId = mElement.getException();
        if (failedId == TransferException.NONE) {
            return null;
        } else if (failedId == TransferException.LOCAL_SPACE_INSUFFICIENT) {
            failedInfo = context.getResources().getString(R.string.local_space_insufficient);
        } else if (failedId == TransferException.SERVER_SPACE_INSUFFICIENT) {
            failedInfo = context.getResources().getString(R.string.server_space_insufficient);
        } else if (failedId == TransferException.FAILED_REQUEST_SERVER) {
            failedInfo = context.getResources().getString(R.string.request_server_exception);
        } else if (failedId == TransferException.ENCODING_EXCEPTION) {
            failedInfo = context.getResources().getString(R.string.decoding_exception);
        } else if (failedId == TransferException.IO_EXCEPTION) {
            failedInfo = context.getResources().getString(R.string.io_exception);
        } else if (failedId == TransferException.FILE_NOT_FOUND) {
            if (isDownload) {
                failedInfo = context.getResources().getString(R.string.touch_file_failed);
            } else {
                failedInfo = context.getResources().getString(R.string.file_not_found);
            }
        } else if (failedId == TransferException.SERVER_FILE_NOT_FOUND) {
            failedInfo = context.getResources().getString(R.string.source_not_found);
        } else if (failedId == TransferException.UNKNOWN_EXCEPTION) {
            failedInfo = context.getResources().getString(R.string.unknown_exception);
        } else if (failedId == TransferException.SOCKET_TIMEOUT) {
            failedInfo = context.getResources().getString(R.string.socket_timeout);
        } else if (failedId == TransferException.WIFI_UNAVAILABLE) {
            failedInfo = context.getResources().getString(R.string.wifi_connect_break);
        } else if (failedId == TransferException.AUTH_EXP) {
            failedInfo= context.getResources().getString(R.string.msg_error_session);
        }

        return failedInfo;
    }

}
