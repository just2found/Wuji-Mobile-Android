package net.linkmate.app.ui.fragment.main;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import net.linkmate.app.R;
import net.linkmate.app.base.MyConstants;
import net.linkmate.app.bean.AppBean;
import net.linkmate.app.manager.AppManage;
import net.linkmate.app.manager.LoginManager;
import net.linkmate.app.ui.fragment.BaseFragment;
import net.linkmate.app.util.MySPUtils;
import net.linkmate.app.view.CarouselView;
import net.linkmate.app.view.ViewPagerPoint;
import net.linkmate.app.view.adapter.AppRVAdapter;

import java.util.Arrays;

public class AppFragment extends BaseFragment {
    private TextView tvTitle;
    private ImageView ivRight;
    private CarouselView mCvBanner;
    private ViewPagerPoint mVppBannerPoint;
    private RecyclerView rvMoreApp;
    private RelativeLayout itbRl;

    private Integer[] iconIds = new Integer[]{R.drawable.icon_test_1, R.drawable.icon_test_2, R.drawable.icon_test_3, R.drawable.icon_test_4,
            R.drawable.icon_test_5, R.drawable.icon_test_6, R.drawable.icon_test_7, R.drawable.icon_test_8};
    private View mItbIvLeft;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_app;
    }

    @Nullable
    @Override
    protected View getTopView() {
        return itbRl;
    }

    @Override
    protected void initView(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvTitle = view.findViewById(R.id.itb_tv_title);
        ivRight = view.findViewById(R.id.itb_iv_right);
        mCvBanner = view.findViewById(R.id.me_cv_banner);
        mVppBannerPoint = view.findViewById(R.id.me_vpp_banner_point);
        rvMoreApp = view.findViewById(R.id.me_rv_more_app);
        itbRl = view.findViewById(R.id.itb_rl);
        mItbIvLeft = view.findViewById(R.id.itb_iv_left);
        mItbIvLeft.setOnClickListener(v -> {
            onViewClicked();
        });
        tvTitle.setText(R.string.app);
        ivRight.setImageResource(R.drawable.icon_setting_white);
        ivRight.setVisibility(View.GONE);

        //轮播图控件必须自己实现图片加载器加载过程
        mCvBanner.initImageLoader(new CarouselView.ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, int imageResId) {
                imageView.setImageResource(imageResId);
            }
        }).setImageList(Arrays.asList(iconIds));
        //轮播图指示器控件，绑定ViewPager即可，如果是循环的根据循环方式选择参数
        mVppBannerPoint.attachViewPager(mCvBanner.getViewPager(), iconIds.length);
        //设置轮播页点击事件
        mCvBanner.setOnPageClickListener(new CarouselView.OnPageClickListener() {

            @Override
            public void onClick(int index, int imageResId) {
//                Toast.makeText(getContext(), "点击" + index, Toast.LENGTH_SHORT).show();
            }
        });

        rvMoreApp.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        AppRVAdapter adapter = new AppRVAdapter(AppManage.getInstance().getAllApps());
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View v, int position) {
                if (MySPUtils.getBoolean(MyConstants.IS_LOGINED)) {
                    View view = LayoutInflater.from(v.getContext()).inflate(R.layout.layout_dialog_app, null);
                    Dialog dialog = new Dialog(v.getContext(), R.style.DialogTheme);
                    dialog.setContentView(view);
                    TextView title = view.findViewById(R.id.title);
                    title.setText(((AppBean)adapter.getItem(position)).name);
                    dialog.show();
                } else {
                    LoginManager.getInstance().showDialog(v.getContext());
                }
            }
        });
        rvMoreApp.setAdapter(adapter);
    }

    private void onViewClicked() {

    }
}
