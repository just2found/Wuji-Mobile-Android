package net.sdvn.nascommon.model;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import net.sdvn.common.Local;
import net.sdvn.nascommon.constant.AppConstants;
import net.sdvn.nascommon.utils.DialogUtils;
import net.sdvn.nascommon.utils.EmptyUtils;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.nascommonlib.R;

import org.jetbrains.annotations.Nullable;

import io.weline.devhelper.DevTypeHelper;
import timber.log.Timber;

public class UiUtils {
    public static String genNameBySN(@NonNull String devicesn) {
        return AppConstants.DEFAULT_DEV_NAME_PREFIX + (devicesn.length() > 5 ? devicesn.substring(devicesn.length() - 5) : devicesn);
    }

    public static String formatWithError(@StringRes int resId, Object args) {
        return String.format("%s(%s)", Utils.getApp().getString(resId), args);
    }

    public static void showStorageSettings(final Context context) {
        Timber.d("showStorageSettings");
        DialogUtils.showConfirmDialog(context,
                R.string.permission_denied,
                R.string.perm_denied_storage,
                R.string.settings, R.string.cancel,
                (dialog, isPositiveBtn) -> {
                    if (isPositiveBtn) {
                        Utils.gotoAppDetailsSettings(context);
                    }
                }
        );

    }

    public static boolean isEn() {
        return !isCN();//Locale.getDefault() != null && Objects.equals(Locale.getDefault().getDisplayLanguage(),
               // Locale.ENGLISH.getDisplayLanguage());
    }

    public static boolean isCN() {
        return isHans() || isHant();
    }

    public static boolean isHans() {
        return Local.isHans();
//        String lang = Locale.getDefault().toString();
//        switch (lang) {
//            case "zh_CN":
//            case "zh_CN_#Hans":
//            case "zh_SG_#Hans":
//                return true;
//            default:
//                return false;
//        }
    }

    public static boolean isHant() {
        return Local.isHant();
//        String lang = Locale.getDefault().toString();
//        switch (lang) {
//            case "zh_TW":
//            case "zh_HK":
//            case "zh_MO":
//            case "zh_TW_#Hant":
//            case "zh_HK_#Hant":
//            case "zh_MO_#Hant":
//                return true;
//            default:
//                return false;
//        }
    }

    public static void showSettings(Activity context, boolean isContacts) {
        int tip;
        if (isContacts) {
            tip = R.string.permission_denied_backup_contact;
        } else {
            tip = R.string.permission_denied_backup_sms;
        }
        DialogUtils.showConfirmDialog(context,
                R.string.permission_denied,
                tip,
                R.string.settings, R.string.cancel,
                new DialogUtils.OnDialogClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, @NonNull boolean isPositiveBtn) {
                        if (isPositiveBtn) {
                            Utils.gotoAppDetailsSettings(context);
                        }
                    }
                }
        );
    }

    public static boolean isNas(int devClass) {
        return DevTypeHelper.isNas(devClass);
    }

    public static boolean isNasByFeature(int devFeature) {
        return DevTypeHelper.isNasByFeature(devFeature);
    }

    public static boolean isAndroidTV(int devClass) {
        return DevTypeHelper.isAndroidTV(devClass);
    }

    public static boolean isM8(int devClass) {
        return DevTypeHelper.isM8(devClass);
    }

    public static boolean isNewVersion(String version, String newVersion) {
        try {
            if (EmptyUtils.isEmpty(version) || EmptyUtils.isEmpty(newVersion)) {
                return false;
            }
            String[] strs = version.split("\\.");
            String[] strs2 = newVersion.split("\\.");
            for (int i = 0; i < strs.length; i++) {
                int verNo = Integer.parseInt(strs[i].trim());
                int verNo2 = Integer.parseInt(strs2[i].trim());
                if (verNo2 > verNo) {
                    return true;
                } else if (verNo2 == verNo) {
                    continue;
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return false;
    }

    public static int getStatusBarOffsetPx(@Nullable Context context) {
        Context applicationContext = context.getApplicationContext();
        int identifier = applicationContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (identifier > 0) {
            return (int) applicationContext.getResources().getDimension(identifier);
        }
        return 0;
    }
}