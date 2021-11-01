package net.linkmate.app.data;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import net.linkmate.app.R;
import net.linkmate.app.ui.activity.mine.score.RechargeActivity;
import net.linkmate.app.util.DialogUtil;

public class ScoreHelper {
    public static void showNeedMBPointDialog(Context context) {
        DialogUtil.showSelectDialog(context, context.getString(R.string.low_score_tips),
                context.getString(R.string.ok), new DialogUtil.OnDialogButtonClickListener() {
                    @Override
                    public void onClick(View v, String strEdit, Dialog dialog, boolean isCheck) {
                        context.startActivity(new Intent(context, RechargeActivity.class));
                        dialog.dismiss();
                    }
                },
                context.getString(R.string.cancel), null);
    }
}
