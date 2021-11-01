package net.sdvn.nascommon.model.phone.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sdvn.nascommon.model.phone.AppInfo;
import net.sdvn.nascommon.utils.log.Logger;
import net.sdvn.nascommonlib.R;

import java.util.ArrayList;
import java.util.List;

public class AppAdapter extends BaseAdapter {
    private Context context;
    private int rightWidth;
    private LayoutInflater mInflater;
    @NonNull
    private List<AppInfo> mAppList = new ArrayList<AppInfo>();

    public AppAdapter(Context context, int rightWidth) {
        this.context = context;
        this.rightWidth = rightWidth;
        this.mInflater = LayoutInflater.from(context);
    }

    public void setAppList(@NonNull List<AppInfo> appList) {
        mAppList.clear();
        mAppList.addAll(appList);
    }

    @Override
    public int getCount() {
        return mAppList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder {
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        ImageView appIcon;
        TextView appName;
        TextView appVersion;
        TextView appSize;
        TextView appUninstall;
        TextView appOpen;
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_listview_app, null);
            holder = new ViewHolder();
            holder.leftLayout = convertView.findViewById(R.id.layout_power_off);
            holder.rightLayout = convertView.findViewById(R.id.layout_right);
            holder.appIcon = convertView.findViewById(R.id.app_icon);
            holder.appName = convertView.findViewById(R.id.app_name);
            holder.appVersion = convertView.findViewById(R.id.app_version);
            holder.appSize = convertView.findViewById(R.id.app_size);
            holder.appUninstall = convertView.findViewById(R.id.app_uninstall);
            holder.appOpen = convertView.findViewById(R.id.app_open);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LayoutParams leftLayout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        holder.leftLayout.setLayoutParams(leftLayout);
        LayoutParams rightLayout = new LayoutParams(rightWidth, LayoutParams.MATCH_PARENT);
        holder.rightLayout.setLayoutParams(rightLayout);

        final AppInfo appInfo = mAppList.get(position);
        holder.appIcon.setImageDrawable(appInfo.getAppIcon());
        holder.appName.setText(appInfo.getAppName());
        holder.appVersion.setText(appInfo.getAppVersion());
        // holder.appSize.setTitleText(Formatter.formatFileSize(context, appInfo.appSize));

        holder.appUninstall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                if (v.getId() == R.id.app_uninstall) {
                    unInstaller(appInfo.getPkName());
                }
            }
        });

        holder.appOpen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                if (v.getId() == R.id.app_open){
                        appOpen(appInfo.getIntent());
                }
            }
        });

        return convertView;
    }

    private void unInstaller(String packagekName) {
        Logger.LOGI("-----", packagekName);
        Uri packageURI = Uri.parse("package:" + packagekName);
        Intent intent = new Intent(Intent.ACTION_DELETE, packageURI);
        context.startActivity(intent);
    }

    private void appOpen(Intent intent) {
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.error_open_app, Toast.LENGTH_SHORT).show();
        }
    }

}
