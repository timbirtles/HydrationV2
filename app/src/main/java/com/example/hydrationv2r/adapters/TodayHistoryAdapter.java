package com.example.hydrationv2r.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hydrationv2r.R;
import com.example.hydrationv2r.fragments.TodayHistoryFragment;
import com.example.hydrationv2r.helpers.DatabaseHelper;
import com.example.hydrationv2r.models.LoggedDrinkModel;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;

public class TodayHistoryAdapter extends RecyclerView.Adapter<TodayHistoryAdapter.TodayHistoryViewHolder> {

    Context context;
    List<LoggedDrinkModel> drinks;

    public TodayHistoryAdapter(Context context, View root, TodayHistoryFragment fragment, List<LoggedDrinkModel> drinks) {
        this.context = context;
        DatabaseHelper db = DatabaseHelper.getInstance(context);
        this.drinks = drinks;
        Log.d("TodayHistoryAdapter", "Drink coutn: " + drinks.size());
    }

    @NonNull
    @Override
    public TodayHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.today_history, parent, false);
        return new TodayHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodayHistoryViewHolder holder, int position) {
        LoggedDrinkModel record = drinks.get(position);

        long timestamp = record.timestamp;
        Instant instant = Instant.ofEpochMilli(timestamp);
        ZonedDateTime logTime = instant.atZone(ZoneId.systemDefault());

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime = logTime.format(timeFormatter); // Call .format() here!

        holder.timeExact.setText(formattedTime);
        holder.timeAgo.setText(calculateTimeAgo(timestamp));


        int resId = context.getResources().getIdentifier(record.drink.iconName, "drawable", context.getPackageName());

        if (resId != 0) {
            holder.icon.setImageResource(resId);
            holder.icon.setImageTintList(ColorStateList.valueOf(record.drink.colour));
        }

        holder.amountDrunk.setText(record.amount + "ml");
        holder.drinkName.setText(record.drink.name);

    }

    public void removeLog(int position) {
        drinks.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, drinks.size());
    }

    public LoggedDrinkModel getLogAt(int position) {
        return drinks.get(position);
    }


    @Override
    public int getItemCount() {
        return drinks.size();
    }

    // Gets and stores each View for use later
    public static class TodayHistoryViewHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView drinkName, timeAgo, timeExact, amountDrunk;

        public TodayHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_icon);
            drinkName = itemView.findViewById(R.id.tv_drinkName);
            timeAgo = itemView.findViewById(R.id.tv_timeAgo);
            timeExact = itemView.findViewById(R.id.tv_timeExact);
            amountDrunk = itemView.findViewById(R.id.tv_amountDrunk);
        }
    }

    private String calculateTimeAgo(long savedMillis) {
        long now = System.currentTimeMillis();
        long diff = now - savedMillis;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + "m ago";
        if (hours < 24) return hours + "h ago";

        return "Yesterday";
    }
}
