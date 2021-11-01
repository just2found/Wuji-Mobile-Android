package net.linkmate.app.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.linkmate.app.R;
import net.sdvn.nascommon.model.oneos.OneOSPluginInfo;
import net.sdvn.nascommon.widget.kyleduo.SwitchButton;

import java.util.ArrayList;
import java.util.List;

public class PluginAdapter extends BaseAdapter {
    private Context context;
    private int rightWidth;
    private LayoutInflater mInflater;
    private List<OneOSPluginInfo> mPluginList = new ArrayList<>();
    private OnPluginClickListener listener;

    public PluginAdapter(Context context, int rightWidth, List<OneOSPluginInfo> mPluginList) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.rightWidth = rightWidth;
        this.mPluginList = mPluginList;
    }

    @Override
    public int getCount() {
        return mPluginList.size();
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
        LinearLayout mLeftLayout;
        TextView title;
        LinearLayout mRightLayout;
        TextView mIconImage;
        TextView mNameTxt;
        TextView mStatTxt;
        TextView mVersionTxt;
        TextView mUninstallTxt;
        // ImageButton mOpenBtn;
        SwitchButton mStateBtn;
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_listview_plugin, null);
            holder = new ViewHolder();
            holder.mLeftLayout = convertView.findViewById(R.id.layout_left);
            holder.title = convertView.findViewById(R.id.title_left);
            holder.mRightLayout = convertView.findViewById(R.id.layout_right);
            holder.mIconImage = convertView.findViewById(R.id.app_icon);
            holder.mNameTxt = convertView.findViewById(R.id.app_name);
            holder.mStatTxt = convertView.findViewById(R.id.app_stat);
            holder.mUninstallTxt = convertView.findViewById(R.id.app_uninstall);
            holder.mVersionTxt = convertView.findViewById(R.id.app_version);
            // holder.mOpenBtn = (ImageButton) convertView.findViewById(R.id.app_open);
            holder.mStateBtn = convertView.findViewById(R.id.btn_state);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final OneOSPluginInfo info = mPluginList.get(position);
        if (info.getIsTitle()) {
            holder.mLeftLayout.setVisibility(View.GONE);
            holder.mRightLayout.setVisibility(View.GONE);
            holder.title.setVisibility(View.VISIBLE);
            holder.title.setText(info.getName());
            return convertView;
        }
        holder.mLeftLayout.setVisibility(View.VISIBLE);
        holder.mRightLayout.setVisibility(View.VISIBLE);
        holder.title.setVisibility(View.GONE);

        LayoutParams leftLayout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        holder.mLeftLayout.setLayoutParams(leftLayout);
        LayoutParams rightLayout = new LayoutParams(rightWidth, LayoutParams.MATCH_PARENT);
        holder.mRightLayout.setLayoutParams(rightLayout);

        holder.mStateBtn.setCheckedNoEvent(info.isOn());
//        holder.mIconImage.setImageResource(getResByName(info.getPack()));
        if (info.getPack() != null) {
            setImageResource(holder.mIconImage, info.getPack());
        }
        holder.mNameTxt.setText(info.getName());
        String version = null;
        if (info.getVersion() != null) {
            version = info.getVersion().toLowerCase();
            if (version.startsWith("v")) {
                version = version.substring(1).trim();
            }
            holder.mVersionTxt.setText(String.format(" ( V %s )", version));
        }
        OneOSPluginInfo.State state = info.getStat();
        int status;
        if (state == OneOSPluginInfo.State.ON) {
            status = R.string.app_state_on;
        } else if (state == OneOSPluginInfo.State.OFF) {
            status = R.string.app_state_off;
        } else if (state == OneOSPluginInfo.State.UNKNOWN) {
            status = R.string.app_state_unknown;
        } else {
            status = R.string.app_state_getting;
        }
        holder.mStatTxt.setText(String.format("%s%s", context.getResources().getString(R.string.app_status), context.getResources().getString(status)));

        if (info.isCanDel()) {
            holder.mUninstallTxt.setText(R.string.uninstall);
            holder.mUninstallTxt.setBackgroundColor(context.getResources().getColor(R.color.red));
            holder.mUninstallTxt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(@NonNull View v) {
                    switch (v.getId()) {
                        case R.id.app_uninstall:
                            if (null != listener) {
                                listener.onClick(v, info);
                            }
                            break;
                    }
                }
            });
        } else {
            holder.mUninstallTxt.setText(R.string.can_not_uninstall);
            holder.mUninstallTxt.setBackgroundColor(context.getResources().getColor(R.color.light_gray));
        }

        if (info.isCanOff()) {
            holder.mStateBtn.setVisibility(View.VISIBLE);
            holder.mStateBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (null != listener) {
                        listener.onClick(holder.mStateBtn, info);
                    }
                }
            });
        } else {
            holder.mStateBtn.setVisibility(View.GONE);
        }

        return convertView;
    }

    private void setImageResource(@NonNull TextView iconImage, @NonNull String pack) {
        int resByName = getResByName(pack);
        if (resByName == R.drawable.bg_square_full_radius_primary) {
            pack = pack.toUpperCase();
            iconImage.setTextColor(Color.WHITE);
            iconImage.setGravity(Gravity.CENTER);
            iconImage.setText(pack);
            iconImage.setBackgroundResource(resByName);
        } else {
            iconImage.setText("");
            iconImage.setBackgroundResource(resByName);
        }
    }

    public void setOnClickListener(OnPluginClickListener listener) {
        this.listener = listener;
    }

    private int getResByName(String name) {
        int resId = R.drawable.bg_square_full_radius_primary;
        if (name.equalsIgnoreCase("shell")) {
            resId = R.drawable.icon_plug_ssh;
        } else if (name.equalsIgnoreCase("aria2")) {
            resId = R.drawable.icon_plug_aria;
        } else if (name.equalsIgnoreCase("bdsync")) {
            resId = R.drawable.icon_plug_bdsync;
        } else if (name.equalsIgnoreCase("clock")) {
            resId = R.drawable.icon_plug_clock;
        } else if (name.equalsIgnoreCase("cups")) {
            resId = R.drawable.icon_plug_cups;
        } else if (name.equalsIgnoreCase("dlna")) {
            resId = R.drawable.icon_plug_dlna;
        } else if (name.equalsIgnoreCase("transmission")) {
            resId = R.drawable.icon_plug_pt;
        } else if (name.equalsIgnoreCase("samba")) {
            resId = R.drawable.icon_plug_samba;
        } else if (name.equalsIgnoreCase("thunder")) {
            resId = R.drawable.icon_plug_thunder;
        } else if (name.equalsIgnoreCase("autoupgrade")) {
            resId = R.drawable.icon_plug_auto_upgrade;
        } else if (name.equalsIgnoreCase("syncthing")) {
            resId = R.drawable.icon_plug_rsync;
        } else if (name.equalsIgnoreCase("BTSync")) {
            resId = R.drawable.icon_plug_btsync;
        } else if (name.equalsIgnoreCase("ftpd")) {
            resId = R.drawable.icon_plug_ftp;
        } else if (name.equalsIgnoreCase("QQIOT")) {
            resId = R.drawable.icon_plug_qq_iot;
        } else if (name.equalsIgnoreCase("NFS")) {
            resId = R.drawable.icon_plug_nfs;
        } else if (name.equalsIgnoreCase("Phddns")) {
            resId = R.drawable.icon_plug_phddns;
        } else if (name.equalsIgnoreCase("afp")) {
            resId = R.drawable.icon_plug_afp;
        } else if (name.equalsIgnoreCase("rsync")) {
            resId = R.drawable.icon_plug_rsync;
        } else if (name.equalsIgnoreCase("vsftp")) {
            resId = R.drawable.icon_plug_ftp;
        } else if (name.equalsIgnoreCase("note")) {
            resId = R.drawable.icon_plug_note;
        }

        return resId;
    }

    public interface OnPluginClickListener {
        void onClick(View view, OneOSPluginInfo info);
    }
}
