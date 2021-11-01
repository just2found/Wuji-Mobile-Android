package net.linkmate.app.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import net.linkmate.app.R;
import net.linkmate.app.ui.activity.LoginActivity;
import net.linkmate.app.util.UIUtils;
import net.sdvn.cmapi.CMAPI;
import net.sdvn.nascommon.utils.DialogUtils;

public class TipsBar extends LinearLayout {
    private View content;
    private TextView tvContent;
    private TextView tvLink;
    private ImageView ivClose;
    private Dialog mDialog;
    private View root;

    public TipsBar(Context context) {
        this(context, null);
    }

    public TipsBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TipsBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TipsBar);

        int color = typedArray.getInt(R.styleable.TipsBar_bg_type, 0x425FFF);

        int icon_visibility = typedArray.getInt(R.styleable.TipsBar_icon_visibility, VISIBLE);
        int icon_src = typedArray.getResourceId(R.styleable.TipsBar_icon_src, -1);

        String content_text = typedArray.getString(R.styleable.TipsBar_content_text);

        int content_text_color = typedArray.getColor(R.styleable.TipsBar_content_text_color, -1);

        String tips_text = typedArray.getString(R.styleable.TipsBar_link_text);
        int iconPadding = typedArray.getDimensionPixelSize(R.styleable.TipsBar_icon_padding, -1);
        typedArray.recycle();

        content = View.inflate(context, R.layout.include_tips_bar, this);
        tvContent = content.findViewById(R.id.tips_tv_content);
        tvLink = content.findViewById(R.id.tips_tv_link);
        ivClose = content.findViewById(R.id.tips_iv_close);
        root = content.findViewById(R.id.root);
        content.setBackgroundColor(color);
        ivClose.setVisibility(icon_visibility);
        if (iconPadding != -1)
            ivClose.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
        if (icon_src != -1)
            ivClose.setImageResource(icon_src);
        ivClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
        if (!TextUtils.isEmpty(content_text)) {
            tvContent.setText(content_text);
        }
        if (content_text_color != -1) {
            tvContent.setTextColor(content_text_color);
        }
        if (TextUtils.isEmpty(tips_text)) {
            tvLink.setVisibility(GONE);
        } else {
            tvLink.setVisibility(VISIBLE);
            tvLink.setText(tips_text);
        }
    }

    public void setTips(@StringRes int resid) {
        setTips(getContext().getResources().getText(resid));
    }

    public void setTips(CharSequence text) {
        tvContent.setText(text);
        tvContent.setTextColor(getContext().getResources().getColor(R.color.text_dark_gray));
        root.setBackgroundColor(getContext().getResources().getColor(R.color.bg_tips));
        content.setVisibility(VISIBLE);
    }

    public void setWarning(@StringRes int resid) {
        setWarning(getContext().getResources().getText(resid));
    }

    public void setWarning(CharSequence text) {
        tvContent.setText(text);
        tvContent.setTextColor(getContext().getResources().getColor(R.color.text_warning));
        root.setBackgroundColor(getContext().getResources().getColor(R.color.bg_warning));
        content.setVisibility(VISIBLE);
    }

    public void setBackLink(@StringRes int resid) {
        setLink(getContext().getResources().getText(resid), new OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startActivity(new Intent(getContext(), LoginActivity.class));
            }
        });
    }

    public void setLink(@StringRes int resid, @Nullable OnClickListener l) {
        setLink(getContext().getResources().getText(resid), l);
    }

    public void setLink(CharSequence text, @Nullable OnClickListener l) {
        tvLink.setVisibility(VISIBLE);
        tvLink.setText(text);
        tvLink.setOnClickListener(l);
    }

    public void setBackToLogin() {
        setBackLink(R.string.back_to_login);
        setLinkClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog = DialogUtils.showConfirmDialog(getContext(), 0, R.string.stop_connect, R.string.confirm,
                        R.string.cancel, new DialogUtils.OnDialogClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, @NonNull boolean isPositiveBtn) {
                                if (isPositiveBtn) {
                                    if (CMAPI.getInstance().isDisconnected()) {
                                        Activity activity = UIUtils.getActivity(TipsBar.this);
                                        //退出登录
                                        getContext().startActivity(new Intent(getContext(), LoginActivity.class));
                                        if (activity != null) {
                                            activity.finish();
                                        }
                                    } else {
                                        //未连接前可取消
                                        if (!CMAPI.getInstance().isConnected()) {
                                            CMAPI.getInstance().cancelLogin();
                                            Activity activity = UIUtils.getActivity(TipsBar.this);
                                            //退出登录
                                            getContext().startActivity(new Intent(getContext(), LoginActivity.class));
                                            if (activity != null) {
                                                activity.finish();
                                            }

                                        }
                                    }
                                }
                            }
                        });
            }
        });
    }

    public void showTipWithoutNet() {
        setWarning(R.string.network_not_available);
        setBackToLogin();
    }

    public void showTipWithoutService() {
        setWarning(R.string.tip_wait_for_service_connect);
        setBackToLogin();
    }

    public void close() {
        post(new Runnable() {
            @Override
            public void run() {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                setVisibility(GONE);
                content.setVisibility(GONE);
                tvLink.setVisibility(GONE);
                tvLink.setOnClickListener(null);
            }
        });
    }

    public boolean isShowing() {
        return content.getVisibility() == VISIBLE;
    }

    public void setCloseClickListener(OnClickListener onClickListener) {
        ivClose.setOnClickListener(onClickListener);
    }

    public void setLinkClickListener(OnClickListener onClickListener) {
        tvLink.setOnClickListener(onClickListener);
    }

}
