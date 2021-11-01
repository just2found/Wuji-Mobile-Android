package net.linkmate.app.ui.activity.feature;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.linkmate.app.R;

public class DevClientActivity extends AppCompatActivity {
    private RelativeLayout rlTitle;
    private ImageView ivLeft;
    private TextView tvTitle;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_client);
        rlTitle = findViewById(R.id.itb_rl);
        ivLeft = findViewById(R.id.itb_iv_left);
        tvTitle = findViewById(R.id.itb_tv_title);
        tvTitle.setText("Test_phone");
        ivLeft.setVisibility(View.VISIBLE);
        ivLeft.setImageResource(R.drawable.icon_return);
        ivLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
