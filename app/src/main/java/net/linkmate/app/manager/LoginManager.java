package net.linkmate.app.manager;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import net.linkmate.app.R;
import net.linkmate.app.base.MyConstants;
import net.linkmate.app.ui.activity.LoginActivity;
import net.linkmate.app.util.DialogUtil;
import net.linkmate.app.util.MySPUtils;
import net.sdvn.nascommon.db.SPHelper;

public class LoginManager {
    private static class SingletonHolder {
        private static LoginManager instance = new LoginManager();
    }

    private LoginManager() {
        _loginedData.setValue(false);
    }

    private MutableLiveData<Boolean> _loginedData = new MutableLiveData<>(false);
    public LiveData<Boolean> loginedData = _loginedData;

    public static LoginManager getInstance() {
        return SingletonHolder.instance;
    }

    public boolean isLogined() {
        Boolean value = _loginedData.getValue();
        return value != null && value;
    }

//    private WeakHashMap<OnStatusChange, Integer> map = new WeakHashMap<>();
//
//    public void addLoginListener(OnStatusChange change) {
//        map.put(change, 0);
//    }
//
//    public void removeLoginListener(OnStatusChange change) {
//        map.remove(change);
//    }

    public void notifyLogin(boolean isLogin) {
        MySPUtils.saveBoolean(MyConstants.IS_LOGINED, isLogin);
        _loginedData.setValue(isLogin);
//        Iterator<OnStatusChange> iterator = map.keySet().iterator();
//        while (iterator.hasNext()) {
//            OnStatusChange change = iterator.next();
//            change.onLoginStatusChange(isLogin);
//        }
        if (isLogin){
            SPHelper.clearPwd();
        }
    }

    public void showDialog(final Context context) {
        DialogUtil.showSelectDialog(context, context.getString(R.string.login_tips),
                context.getString(R.string.ok), new DialogUtil.OnDialogButtonClickListener() {
                    @Override
                    public void onClick(View v, String strEdit, Dialog dialog, boolean isCheck) {
                        context.startActivity(new Intent(context, LoginActivity.class));
                        dialog.dismiss();
                    }
                },
                context.getString(R.string.cancel), null);
    }
}
