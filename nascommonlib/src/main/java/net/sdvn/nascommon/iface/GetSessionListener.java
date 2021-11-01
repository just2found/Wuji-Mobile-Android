package net.sdvn.nascommon.iface;


import android.widget.Toast;

import net.sdvn.common.internet.utils.LoginTokenUtil;
import net.sdvn.nascommon.SessionManager;
import net.sdvn.nascommon.constant.HttpErrorNo;
import net.sdvn.nascommon.model.oneos.user.LoginSession;
import net.sdvn.nascommon.receiver.NetworkStateManager;
import net.sdvn.nascommon.utils.ToastHelper;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;

import io.weline.repo.api.V5HttpErrorNoKt;

public abstract class GetSessionListener implements EventListener<LoginSession> {
    private boolean mShowFailure;

    public GetSessionListener() {
        this(true);
    }

    public GetSessionListener(boolean showFailure) {
        mShowFailure = showFailure;
    }

    public void onStart(String url) {

    }

    public abstract void onSuccess(String url, LoginSession loginSession);

    public void onFailure(String url, int errorNo, String errorMsg) {
        if (errorNo == HttpErrorNo.ERR_ONE_REQUEST && "无效的Token".equals(errorMsg)) {
            LoginTokenUtil.clearToken();
        }else  if (errorNo == HttpErrorNo.ERR_ONE_REQUEST && "无效的Ticket".equals(errorMsg)) {
            SessionManager.getInstance().rebootDevice(url);
        }
        if (mShowFailure) {
            if (errorNo == HttpErrorNo.ERR_ONEOS_VERSION) {
                ToastHelper.showLongToastSafe(R.string.tips_title_version_mismatch, Toast.LENGTH_LONG);
            } else if (errorNo == HttpErrorNo.ERR_CONNECT_REFUSED || (errorNo == HttpErrorNo.ERR_UNABLE_HOST)) {
                ToastHelper.showLongToastSafe(R.string.connection_refused, Toast.LENGTH_LONG);
            } else if (errorNo == HttpErrorNo.ERR_ONE_NO_USERNAME) {
                ToastHelper.showLongToastSafe(R.string.get_help_from_admin, Toast.LENGTH_LONG);
            } else {
                errorMsg = HttpErrorNo.getResultMsg(true, errorNo, errorMsg);
                Logger.LOGD(this, errorMsg);
                if (NetworkStateManager.Companion.getInstance().isEstablished())
                    ToastHelper.showLongToastSafe(errorMsg);
            }
        }
    }
}