package net.linkmate.app.ui.scan;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.StringRes;

import net.linkmate.app.R;
import net.linkmate.app.ui.fragment.BaseFragment;
import net.sdvn.nascommon.utils.Utils;


/**
 * Created by yun on 2018/2/7.
 */

public class ScanResultOperatorFragment extends BaseFragment implements View.OnClickListener {
    private ImageView mImageView;
    private TextView mTvOperation;
    private TextView mTvOperationTips;
    private Button mBtnOk;
    private Button mBtnExtra;
    private int resid;
    private View.OnClickListener listener;


    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        mImageView = view.findViewById(R.id.fra_scan_result_iv);
        mTvOperation = view.findViewById(R.id.fra_scan_result_tv_operator);
        mTvOperationTips = view.findViewById(R.id.fra_scan_result_tv_operator_tips);
        mBtnOk = view.findViewById(R.id.fra_scan_result_btn_operator);
        mBtnExtra = view.findViewById(R.id.fra_scan_result_btn_extra);
        Bundle arguments = getArguments();
        mImageView.setImageResource(arguments.getInt("resIdIv"));
        int resIdOperation = arguments.getInt("resIdOperation");
        mTvOperation.setVisibility(resIdOperation != -1 ? View.VISIBLE : View.INVISIBLE);
        if (resIdOperation != -1) {
            mTvOperation.setText(getActivity().getResources().getString(resIdOperation));
        }
        int resIdOperationTip = arguments.getInt("resIdOperationTip");
        mTvOperationTips.setVisibility(resIdOperationTip != -1 ? View.VISIBLE : View.INVISIBLE);
        if (resIdOperationTip != -1) {
            mTvOperationTips.setText(getActivity().getResources().getString(resIdOperationTip));
        }
        mBtnOk.setOnClickListener(this);

        if (listener != null) {
            mBtnExtra.setVisibility(View.VISIBLE);
            mBtnExtra.setText(this.resid);
            mBtnExtra.setOnClickListener(this);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_scan_result_operator;
    }

    @Override
    public void onClick(View v) {
        if (Utils.isFastClick(v)) return;
        if (v == mBtnOk) {
            requireActivity().finish();
        } else if (v == mBtnExtra) {
            listener.onClick(v);
        }
    }

    public static ScanResultOperatorFragment newInstance(int resIdIv, int resIdOperation, int resIdOperationTip) {
        Bundle bundle = new Bundle();
        bundle.putInt("resIdIv", resIdIv);
        bundle.putInt("resIdOperationTip", resIdOperationTip);
        bundle.putInt("resIdOperation", resIdOperation);
        ScanResultOperatorFragment srof = new ScanResultOperatorFragment();
        srof.setArguments(bundle);
        return srof;
    }

    public void setOnBtnClickListener(@StringRes int resid, View.OnClickListener listener) {
        if (listener != null) {
            this.resid = resid;
            this.listener = listener;
        }
    }
}
