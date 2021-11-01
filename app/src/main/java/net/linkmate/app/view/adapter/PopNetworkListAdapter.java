package net.linkmate.app.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.linkmate.app.R;
import net.sdvn.cmapi.Network;

import java.util.ArrayList;
import java.util.List;

public class PopNetworkListAdapter extends RecyclerView.Adapter {
    List<Network> datas;

    public PopNetworkListAdapter(List<Network> datas) {
        this.datas = new ArrayList<>();
        this.datas.addAll(datas);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View layout = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_pop_network, viewGroup, false);
        return new PopHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof PopHolder) {
            ((PopHolder) holder).tvName.setText(datas.get(position).getName());
            ((PopHolder) holder).tvUser.setText(datas.get(position).getOwner());
            ((PopHolder) holder).tvName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.OnItemClick(position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class PopHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvUser;

        public PopHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.item_pop_network_tv_name);
            tvUser = itemView.findViewById(R.id.item_pop_network_tv_user);
        }
    }

    OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void OnItemClick(int position);
    }
}
