package com.example.how_about_camping;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.how_about_camping.HourlyItem;

import java.util.List;

public class HourlyItemAdapter extends RecyclerView.Adapter<com.example.how_about_camping.HourlyItemAdapter.ViewHolder> {
    Context context;
    List<HourlyItem> hourlyItems;

    public HourlyItemAdapter(Context context, List<HourlyItem> hourlyItems) {
        this.context=context;
        this.hourlyItems = hourlyItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hourly, parent,false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        com.example.how_about_camping.HourlyItem item = hourlyItems.get(position);
        holder.time_h.setText(item.getDays());
        holder.temp_h.setText(item.getTemp_hourly());
        holder.image.setImageResource(item.getWeather_photo());

    }

    @Override
    public int getItemCount() {
        return hourlyItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView time_h;
        TextView temp_h;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            time_h=(TextView) itemView.findViewById(R.id.time_hourly);
            temp_h=(TextView)itemView.findViewById(R.id.temp_hourly);
            image=(ImageView)itemView.findViewById(R.id.hourly_image);
        }
    }
}
