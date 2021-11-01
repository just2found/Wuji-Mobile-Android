package net.linkmate.app.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import net.linkmate.app.R;
import net.linkmate.app.ui.activity.LoginActivity;

public class PlsLoginFragment extends BaseFragment {
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_pls_login;
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        Button btnLogin = view.findViewById(R.id.pls_login_btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), LoginActivity.class));
            }
        });
    }
}
