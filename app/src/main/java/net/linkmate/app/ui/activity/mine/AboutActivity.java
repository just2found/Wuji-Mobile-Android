package net.linkmate.app.ui.activity.mine;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import net.linkmate.app.BuildConfig;
import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.base.MyConstants;
import net.linkmate.app.ui.activity.WebViewActivity;
import net.linkmate.app.ui.viewmodel.UpdateViewModel;
import net.linkmate.app.util.ToastUtils;
import net.linkmate.app.view.TipsBar;
import net.sdvn.common.internet.protocol.UpdateInfo;
import net.sdvn.nascommon.utils.Utils;

import org.view.libwidget.MagicTextViewUtil;

import kotlin.Unit;
import libs.source.common.livedata.Resource;
import libs.source.common.livedata.Status;

public class AboutActivity extends BaseActivity {
    private ImageView itbIvLeft;
    private TextView itbTvTitle;
    private RelativeLayout itbRl;
    private TextView tvVersionName;
    private TextView tvNews;
    private TextView tvTermsPrivate;
    //    private UpdateResult updateResult;
    private UpdateViewModel updateViewModel;
    private Observer<Resource<UpdateInfo>> mResourceObserver;
    private View mAboutTvCheckUpdate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        bindView();
        itbTvTitle.setText(R.string.about);
        itbTvTitle.setTextColor(getResources().getColor(R.color.title_text_color));
        itbIvLeft.setVisibility(View.VISIBLE);
        itbIvLeft.setImageResource(R.drawable.icon_return);

        itbIvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        initView();
        updateViewModel = ViewModelProviders.of(this).get(UpdateViewModel.class);
    }

    @Override
    protected View getTopView() {
        return itbRl;
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return findViewById(R.id.tipsBar);
    }

    private void initView() {
        tvVersionName.setText(BuildConfig.VERSION_NAME);
        String terms_and_conditions = getString(R.string.sub_agreement);
//        String terms_and_conditions_url = getString(R.string.terms_and_conditions_url);
        String privacy_policy = getString(R.string.privacy_policy);
//        String privacy_policy_url = getString(R.string.privacy_policy_url);
//        //通过TextView里面的类html标签来实现显示效果
//        String text = "<font color='blue'><a href='%s'>%s</a></font><font color='#C9C9C9'> | </font>" +
//                "<font color='blue'><a href='%s'>%s</a>";
//
//        tvTermsPrivate.setText(Html.fromHtml(String.format(text, terms_and_conditions_url,
//                terms_and_conditions, privacy_policy_url, privacy_policy)));
//        //设置鼠标移动事件，产生链接显示,没有这句话，进不去
//        tvTermsPrivate.setMovementMethod(LinkMovementMethod.getInstance());
        int color = getResources().getColor(R.color.link_blue);
        /*MagicTextViewUtil.Companion.getInstance(tvTermsPrivate)
                .append(terms_and_conditions, color, true, s ->
                        showTermsAndConditions()
                )
                .append("|", getResources().getColor(R.color.gray))
                .append(privacy_policy, color, true, s ->
                        showPrivacyPolicy())
                .show();*/
        // TODO: 2020/3/3   check update
        findViewById(R.id.about_tv_check_update).setVisibility(View.VISIBLE);
    }

    private Unit showPrivacyPolicy() {
        String privacy_policy = getString(R.string.privacy_policy);
        String privacy_policy_url = MyConstants.getPrivacyUrlByLanguage(this);//getString(R.string.privacy_policy_url);
        WebViewActivity.open(this, privacy_policy, privacy_policy_url);
        return null;
    }

    private Unit showTermsAndConditions() {
        String terms_and_conditions = getString(R.string.sub_agreement);
        String terms_and_conditions_url = MyConstants.getAgreementUrlByLanguage(this);//getString(R.string.terms_and_conditions_url);
        WebViewActivity.open(this, terms_and_conditions, terms_and_conditions_url);
        return null;
    }

    private void onViewClicked(View view) {
        if (Utils.isFastClick(view)) return;
        switch (view.getId()) {
            case R.id.about_tv_check_update:
                checkAppVersion();
                break;
        }
    }

    private void checkAppVersion() {
        showLoading(R.string.checking_for_updates);
        LiveData<Resource<UpdateInfo>> resourceLiveData = updateViewModel.checkAppVersion(true);
        if (resourceLiveData != null) {
            if (mResourceObserver == null) {
                mResourceObserver = updateResultResource -> {
                    Status status = updateResultResource.getStatus();
                    if (status == Status.ERROR) {
                        dismissLoading();
                        ToastUtils.showToast(getString(R.string.update_fail));
                    } else if (status == Status.SUCCESS) {
                        UpdateInfo updateResult = updateResultResource.getData();
                        if (updateResult != null) {
                            if (updateResult.result == 109) {
                                dismissLoading();
                                ToastUtils.showToast(getString(R.string.no_update));
                            } else if (updateResult.result == 0) {
                                dismissLoading();
                                if (!TextUtils.isEmpty(updateResult.getVersion())) {
                                    updateViewModel.showUpdateDialog(updateResult, AboutActivity.this);
                                }
                            }
                        }
                    }else {
                        dismissLoading();
                        ToastUtils.showToast(getString(R.string.update_fail));
                    }
                };
            }
            resourceLiveData.observe(this, mResourceObserver);
        }
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String fastDomain = NetConfig.host;
//                try {
//                    String updateJson = MySPUtils.getString(AboutActivity.this, MySPUtils.UPDATE_INFO, "");
//                    List<String> domains = new ArrayList<>();
//                    if (!TextUtils.isEmpty(updateJson)) {
//                        UpdateResult updateResult = new Gson().fromJson(updateJson, UpdateResult.class);
//                        if (updateResult.domains != null) {
//                            domains.addAll(updateResult.domains);
//                        }
//                    }
//                    if (domains.size() == 0) {
//                        domains.add(AppConfig.host_cn);
//                        domains.add(AppConfig.host_us);
//                    }
//                    int minAvg = 100000;
//                    for (String domain : domains) {
//                        if (!TextUtils.isEmpty(domain)) {
//                            String res = PingUtil.ping("ping -c 3 -W 3 " + domain);
//                            int avg = PingUtil.getAvgRTT(res);
//                            if (avg != -1 && avg < minAvg) {
//                                minAvg = avg;
//                                fastDomain = domain;
//                            }
//                        }
//                    }
//
//                    String postBody = "{" +
//                            "\"partnerid\":\"" + MyConstants.CONFIG_PARTID + "\"," +
//                            "\"appid\":\"" + MyConstants.CONFIG_APPID + "\"," +
//                            "\"version\":\"" + BuildConfig.VERSION_NAME + "\"," +
//                            "\"deviceclass\":" + MyConstants.CONFIG_DEV_CLASS +
//                            "}";
//                    LogUtils.e(AboutActivity.class.getSimpleName(), "update request:" + postBody);
//                    Request request = new Request.Builder()
//                            .url(MyConstants.UPDATE_URL.replace("$BASEURL$", fastDomain))
//                            .post(RequestBody.create(MediaType.parse("text/x-markdown; charset=utf-8"), postBody))
//                            .build();
//                    OkHttpClient client = new OkHttpClient().newBuilder()
//                            .connectTimeout(15, TimeUnit.SECONDS)
//                            .readTimeout(15, TimeUnit.SECONDS)
//                            .writeTimeout(15, TimeUnit.SECONDS)
//                            .sslSocketFactory(SSLSocketClient.getSSLSocketFactory())//配置忽略证书
//                            .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
//                            .build();
//
//                    Response response = client.newCall(request).execute();
//                    if (response.isSuccessful()) {
//                        if (response.body() != null) {
//                            String json = response.body().string();
//                            updateResult = new Gson().fromJson(json, UpdateResult.class);
//                            LogUtils.e(AboutActivity.class.getSimpleName(), "update result:" + updateResult.toString());
//                            if (updateResult.result == 109) {
//                                //无新版本
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        dismissLoading();
//                                        ToastUtils.showToast(getString(R.string.no_update));
//                                    }
//                                });
//                                return;
//                            }
//                            if (updateResult.result == 0) {
//                                MySPUtils.saveString(AboutActivity.this, MySPUtils.UPDATE_INFO, json);
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        dismissLoading();
//                                        if (!TextUtils.isEmpty(updateResult.version))
//                                            showUpdateAvailable(updateResult.version);
//                                    }
//                                });
//                                return;
//                            }
//                        }
//                    }
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            dismissLoading();
//                            ToastUtils.showToast(getString(R.string.update_fail));
//                        }
//                    });
//                } catch (Exception ignore) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            ToastUtils.showToast(getString(R.string.update_fail));
//                            dismissLoading();
//                        }
//                    });
//                }
//            }
//        }).start();
    }

    private void bindView() {
        itbIvLeft =  findViewById(R.id.itb_iv_left);
        itbTvTitle =  findViewById(R.id.itb_tv_title);
        itbRl =  findViewById(R.id.itb_rl);
        tvVersionName =  findViewById(R.id.about_tv_version_name);
        tvNews =  findViewById(R.id.about_tv_news);
        tvTermsPrivate =  findViewById(R.id.terms_private);
        mAboutTvCheckUpdate =  findViewById(R.id.about_tv_check_update);
        mAboutTvCheckUpdate.setOnClickListener(v -> {
            onViewClicked(v);
        });
    }

//    private void showUpdateAvailable(@NonNull final String version) {
//        DialogUtil.showSelectDialog(this, getString(R.string.update_title)
//                        + "\r\n" + getString(R.string.update_content).replace("$VERSION$", version),
//                getString(R.string.download_now), new DialogUtil.OnDialogButtonClickListener() {
//                    @Override
//                    public void onClick(View v, String strEdit, Dialog dialog, boolean isCheck) {
//                        beginUpdate();
//                        dialog.dismiss();
//                    }
//                },
//                getString(R.string.download_later), null);
//    }
//
//    private void beginUpdate() {
//        PermissionChecker.checkPermission(this, strings -> {
//                    if (updateResult.enabled && !TextUtils.isEmpty(updateResult.files.get(0).downloadurl)) {
//                        Intent service = new Intent(AboutActivity.this, UpdateService.class);
//                        service.putExtra("update_url", updateResult.files.get(0).downloadurl);
//                        startService(service);
//                    } else {
//                        ToastUtils.showToast(getString(R.string.update_fail));
//                    }
//                },
//                permissions -> {
//                    if (permissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE) ||
//                            permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                        ToastUtils.showToast(getString(R.string.pls_grant_storage_permission));
//                    }
//                },
//                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//    }
}
