package net.sdvn.nascommon.model;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.model.oneos.OneOSFile;
import net.sdvn.nascommonlib.R;

import java.util.List;

public class MsgGenerator {
    public static final int NAME_MAX_LENGTH = 20;

    public static String genFiles(@NonNull Context context, boolean isInvite, boolean isNewUser, @Nullable List<OneOSFile> fileList) {
        StringBuilder message = new StringBuilder(isInvite ?
                String.format(context.getString(R.string.invite_sms_content_start), context.getString(R.string.app_name)) : context.getString(R.string.share_sms_content_start));
        if (fileList != null && fileList.size() > 0) {
            if (isInvite)
                message.append(context.getString(R.string.invite_sms_content_mid));
            message.append("(");
            for (int i = 0; i < fileList.size(); i++) {
                String str = "***";
                if (i >= 3) {
                    message.append("...");
                    break;
                }
                if (i > 0) {
                    message.append("、");
                }
                String name = fileList.get(i).getName();
                if (name.length() <= NAME_MAX_LENGTH)
                    message.append(name);
                else {
                    int lastIndex = name.lastIndexOf(".");
                    if (lastIndex > 0) {
                        String suffix = name.substring(lastIndex);
                        int beginLength = NAME_MAX_LENGTH - suffix.length();
                        String prefix = name.substring(0, beginLength > 0 ? beginLength : 0);
                        message.append(prefix).append(str).append(suffix);
                    } else {
                        message.append(name, 0, NAME_MAX_LENGTH / 2).append(str).
                                append(name, name.length() - 6, name.length());
                    }
                }

            }
            message.append(").");
        }
        if (isNewUser)
            message.append(String.format(context.getString(R.string.invite_sms_content_end),
                    context.getString(R.string.downloadURL),
                    context.getString(R.string.app_name)));
        return message.toString();
    }
}
