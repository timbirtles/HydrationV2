package com.example.hydrationv2r.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hydrationv2r.R;
import com.example.hydrationv2r.models.DrinkModel;

import java.util.List;

public class LogDrinkAdapter extends RecyclerView.Adapter<LogDrinkAdapter.DrinkViewHolder> {

    private List<DrinkModel> drinks;
    private OnDrinkClickListener listener;

    public interface OnDrinkClickListener {
        void onDrinkClick(DrinkModel drink);

        void onDrinkLongClick(DrinkModel drink);
    }

    public LogDrinkAdapter(List<DrinkModel> drinks, OnDrinkClickListener listener) {
        this.drinks = drinks;
        this.listener = listener;
    }

    /**
     * Updates the RecyclerView dataset and refreshes the UI
     * @param newDrinks The updated list of DrinkModel objects
     */
    public void setDrinks(List<DrinkModel> newDrinks) {
        this.drinks = newDrinks;
        // Redraw all drink buttons
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DrinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drink_card, parent, false);
        return new DrinkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DrinkViewHolder holder, int position) {
        DrinkModel drink = drinks.get(position);
        holder.name.setText(drink.name);
        holder.ml.setText(drink.ml + "ml");
        holder.name.setTextColor(drink.colour);

        int resId = holder.itemView.getContext().getResources().getIdentifier(
                drink.iconName, "drawable", holder.itemView.getContext().getPackageName());
        if (resId != 0) {
            holder.icon.setImageResource(resId);
            holder.icon.setImageTintList(ColorStateList.valueOf(drink.colour));
        }

        holder.itemView.setOnClickListener(v -> listener.onDrinkClick(drink));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onDrinkLongClick(drink);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return drinks.size();
    }

    public static class DrinkViewHolder extends RecyclerView.ViewHolder {
        TextView name, ml;
        ImageView icon;

        public DrinkViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_item_name);
            ml = itemView.findViewById(R.id.tv_item_ml);
            icon = itemView.findViewById(R.id.iv_item_icon);
        }
    }
}
