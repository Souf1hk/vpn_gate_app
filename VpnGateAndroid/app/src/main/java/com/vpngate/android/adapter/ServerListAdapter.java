package com.vpngate.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blongho.country_data.Country;
import com.blongho.country_data.World;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.vpngate.android.R;
import com.vpngate.android.model.VpnServer;

import java.util.List;

/**
 * Adapter for VPN server list
 */
public class ServerListAdapter extends RecyclerView.Adapter<ServerListAdapter.ServerViewHolder> {
    
    private List<VpnServer> servers;
    private OnServerClickListener listener;
    
    public interface OnServerClickListener {
        void onServerClick(VpnServer server);
        void onServerFavoriteClick(VpnServer server);
    }
    
    public ServerListAdapter(List<VpnServer> servers, OnServerClickListener listener) {
        this.servers = servers;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ServerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_server, parent, false);
        return new ServerViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ServerViewHolder holder, int position) {
        VpnServer server = servers.get(position);
        holder.bind(server);
    }
    
    @Override
    public int getItemCount() {
        return servers.size();
    }
    
    public void updateServers(List<VpnServer> newServers) {
        this.servers = newServers;
        notifyDataSetChanged();
    }
    
    class ServerViewHolder extends RecyclerView.ViewHolder {
        
        private MaterialCardView cardView;
        private ImageView ivFlag;
        private ImageView ivFavorite;
        private TextView tvCountry;
        private TextView tvIpAddress;
        private TextView tvPing;
        private TextView tvSpeed;
        private TextView tvSessions;
        private TextView tvUptime;
        
        public ServerViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = (MaterialCardView) itemView;
            ivFlag = itemView.findViewById(R.id.iv_flag);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
            tvCountry = itemView.findViewById(R.id.tv_country);
            tvIpAddress = itemView.findViewById(R.id.tv_ip_address);
            tvPing = itemView.findViewById(R.id.tv_ping);
            tvSpeed = itemView.findViewById(R.id.tv_speed);
            tvSessions = itemView.findViewById(R.id.tv_sessions);
            tvUptime = itemView.findViewById(R.id.tv_uptime);
        }
        
        public void bind(VpnServer server) {
            // Set country flag
            Country country = World.getCountryFrom(server.getCountryShort().toLowerCase());
            if (country != null) {
                Glide.with(itemView.getContext())
                    .load(country.getFlagResource())
                    .into(ivFlag);
            }
            
            // Set favorite icon
            ivFavorite.setImageResource(server.isFavorite() ? 
                R.drawable.ic_favorite : R.drawable.ic_favorite_border);
            ivFavorite.setColorFilter(itemView.getContext().getColor(
                server.isFavorite() ? R.color.error : R.color.text_secondary));
            
            // Set server information
            tvCountry.setText(server.getCountryLong());
            tvIpAddress.setText(server.getIpAddress());
            
            // Set ping with color coding
            tvPing.setText(server.getPing() + " ms");
            tvPing.setTextColor(getPingColor(server.getPing()));
            
            // Set speed
            tvSpeed.setText(server.getFormattedSpeed());
            
            // Set sessions
            tvSessions.setText(String.valueOf(server.getNumVpnSessions()));
            
            // Set uptime
            tvUptime.setText(server.getFormattedUptime());
            
            // Set click listeners
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onServerClick(server);
                }
            });
            
            ivFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onServerFavoriteClick(server);
                }
            });
        }
        
        private int getPingColor(int ping) {
            if (ping <= 50) {
                return itemView.getContext().getColor(R.color.ping_excellent);
            } else if (ping <= 100) {
                return itemView.getContext().getColor(R.color.ping_good);
            } else if (ping <= 200) {
                return itemView.getContext().getColor(R.color.ping_fair);
            } else {
                return itemView.getContext().getColor(R.color.ping_poor);
            }
        }
    }
}