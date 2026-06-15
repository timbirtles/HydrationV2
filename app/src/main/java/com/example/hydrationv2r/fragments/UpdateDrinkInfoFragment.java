package com.example.hydrationv2r.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.hydrationv2r.MainActivity;
import com.example.hydrationv2r.R;
import com.example.hydrationv2r.helpers.DatabaseHelper;
import com.example.hydrationv2r.models.DrinkModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class UpdateDrinkInfoFragment extends Fragment {

    private static class DynamicDrinkCardHolder {
        int drinkId;
        View cardRootView;
        TextInputEditText nameInput;
        TextInputEditText sizeInput;
        RecyclerView rvIcons;
        RecyclerView rvColors;
        String selectedIcon;
        int selectedColor;
    }

    private LinearLayout drinksContainer;
    private final List<DynamicDrinkCardHolder> dynamicCards = new ArrayList<>();

    // Row selection items
    private final Integer[] colours = {
            0xFF4CAF50, 0xFF5FB0B0, 0xFFF44336, 0xFFFFC107,
            0xFF9C27B0, 0xFFE91E63, 0xFF2196F3, 0xFFFF9800,
            0xFF795548, 0xFF607D8B, 0xFFCDDC39, 0xFF00BCD4
    };

    private final Integer[] icons = {
            R.drawable.icon_one, R.drawable.icon_two, R.drawable.icon_three,
            R.drawable.icon_four, R.drawable.icon_five, R.drawable.icon_six,
            R.drawable.icon_seven, R.drawable.icon_eight, R.drawable.icon_nine,
            R.drawable.icon_ten, R.drawable.icon_eleven, R.drawable.icon_twelve,
            R.drawable.icon_thirteen, R.drawable.icon_fourteen
    };

    public UpdateDrinkInfoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_update_drink_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        drinksContainer = view.findViewById(R.id.drinks_container);

        // Initial loading of active items from SQLite
        loadCurrentData();

        // Add New Drink button listener
        MaterialButton btnNew = view.findViewById(R.id.btn_new);
        btnNew.setOnClickListener(v -> addNewDrinkUiStub());

        // Save All Changes button listener
        MaterialButton saveBtn = view.findViewById(R.id.btn_save);
        saveBtn.setOnClickListener(v -> {
            if (saveDrinkSettings()) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).GoHome();
                }
            }
        });

        // Reset Button listener
        MaterialButton resetBtn = view.findViewById(R.id.btn_reset);
        resetBtn.setOnClickListener(v -> {




            android.database.sqlite.SQLiteDatabase db = DatabaseHelper.getInstance(getContext()).getWritableDatabase();
            db.execSQL("UPDATE drinks SET is_hidden = 1"); // clear screen layout space
            db.execSQL("INSERT INTO drinks (name, ml, icon, colour) VALUES ('Tea', 300, 'icon_twelve', 0xFF4CAF50)");
            db.execSQL("INSERT INTO drinks (name, ml, icon, colour) VALUES ('Water (Pint)', 550, 'icon_thirteen', 0xFF5FB0B0)");
            db.execSQL("INSERT INTO drinks (name, ml, icon, colour) VALUES ('Coffee', 300, 'icon_fourteen', 0xFFF44336)");
            db.execSQL("INSERT INTO drinks (name, ml, icon, colour) VALUES ('Beer', 330, 'icon_eleven', 0xFFFFC107)");

            Toast.makeText(getContext(), "Reverted to defaults", Toast.LENGTH_SHORT).show();
            loadCurrentData();
        });
    }

    private void loadCurrentData() {
        drinksContainer.removeAllViews();
        dynamicCards.clear();

        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        List<DrinkModel> activeDrinks = db.getActiveDrinks();

        for (DrinkModel drink : activeDrinks) {
            inflateDrinkCard(drink);
        }
    }

    private void inflateDrinkCard(DrinkModel drink) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View cardView = inflater.inflate(R.layout.drink_config_card, drinksContainer, false);

        DynamicDrinkCardHolder holder = new DynamicDrinkCardHolder();
        holder.drinkId = drink.getId();
        holder.cardRootView = cardView;
        holder.nameInput = cardView.findViewById(R.id.et_drink_name);
        holder.sizeInput = cardView.findViewById(R.id.et_drink_size);
        holder.rvIcons = cardView.findViewById(R.id.rv_icons);
        holder.rvColors = cardView.findViewById(R.id.rv_colors);
        holder.selectedIcon = drink.getIconName();
        holder.selectedColor = drink.getColour();

        // Configure Layout Managers for the horizontal scrolling selectors
        holder.rvIcons.setLayoutManager(new GridLayoutManager(getContext(), 5));
        holder.rvColors.setLayoutManager(new GridLayoutManager(getContext(), 5));



        // Assign active input labels
        holder.nameInput.setText(drink.getName());
        holder.sizeInput.setText(String.valueOf(drink.getMl()));

        // Connect lists
        setupSelectionRow(holder.rvIcons, icons, false, holder, drink.getIconName());
        setupSelectionRow(holder.rvColors, colours, true, holder, drink.getColour());

        // Delete button logic (Changes status flag in DB)
        MaterialButton deleteBtn = cardView.findViewById(R.id.btn_delete_drink);
        deleteBtn.setOnClickListener(v -> {
            if (holder.drinkId > 0) {
                android.database.sqlite.SQLiteDatabase writeDb = DatabaseHelper.getInstance(getContext()).getWritableDatabase();
                android.content.ContentValues cv = new android.content.ContentValues();
                cv.put("is_hidden", 1);
                writeDb.update("drinks", cv, "id = ?", new String[]{String.valueOf(holder.drinkId)});
            }
            drinksContainer.removeView(cardView);
            dynamicCards.remove(holder);
            Toast.makeText(getContext(), "Drink removed", Toast.LENGTH_SHORT).show();
        });

        drinksContainer.addView(cardView);
        dynamicCards.add(holder);
    }

    private void addNewDrinkUiStub() {
        // Create generic object blueprint
        DrinkModel placeholder = new DrinkModel(-1, "", 250, "icon_one", 0xFF2196F3);
        inflateDrinkCard(placeholder);

        // Auto scroll down to added element
        drinksContainer.post(() -> holderScrollFocus(dynamicCards.get(dynamicCards.size() - 1).cardRootView));
    }

    private void holderScrollFocus(View view) {
        view.requestFocus();
    }

    private boolean saveDrinkSettings() {
        android.database.sqlite.SQLiteDatabase writeDb = DatabaseHelper.getInstance(requireContext()).getWritableDatabase();

        try {
            writeDb.beginTransaction();
            for (DynamicDrinkCardHolder holder : dynamicCards) {
                String name = holder.nameInput.getText().toString().trim();
                String sizeStr = holder.sizeInput.getText().toString().trim();

                if (name.isEmpty() || sizeStr.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (name.length() > 18) {
                    Toast.makeText(getContext(), "Drink names must be under 19 characters", Toast.LENGTH_SHORT).show();
                    return false;
                }

                int ml = Integer.parseInt(sizeStr);
                String iconName = (holder.selectedIcon != null) ? holder.selectedIcon : "icon_one";
                int color = (holder.selectedColor != 0) ? holder.selectedColor : 0xFF2196F3;

                android.content.ContentValues values = new android.content.ContentValues();
                values.put("name", name);
                values.put("ml", ml);
                values.put("icon", iconName);
                values.put("colour", color);
                values.put("is_hidden", 0);

                if (holder.drinkId > 0) {
                    // Update current item card
                    writeDb.update("drinks", values, "id = ?", new String[]{String.valueOf(holder.drinkId)});
                } else {
                    // Save new added item card to active scope loop allocation
                    writeDb.insert("drinks", null, values);
                }
            }
            writeDb.setTransactionSuccessful();
            Toast.makeText(getContext(), "Changes saved successfully!", Toast.LENGTH_SHORT).show();
            hideKeyboard();
            return true;
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid value present inside Size field", Toast.LENGTH_SHORT).show();
            return false;
        } finally {
            writeDb.endTransaction();
        }
    }

    private void hideKeyboard() {
        View view = this.getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setupSelectionRow(RecyclerView rv, Object[] items, boolean isColor, DynamicDrinkCardHolder holder, Object currentValue) {
        int initialPos = -1;
        for (int i = 0; i < items.length; i++) {
            if (isColor) {
                if (items[i].equals(currentValue)) { initialPos = i; break; }
            } else {
                String resName = getResources().getResourceEntryName((int) items[i]);
                if (resName.equals(currentValue)) { initialPos = i; break; }
            }
        }

        final int finalInitialPos = initialPos;

        RecyclerView.Adapter adapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            private int selectedPos = finalInitialPos;

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
                View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_selection_square, p, false);
                return new RecyclerView.ViewHolder(v) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
                View container = h.itemView.findViewById(R.id.square_container);
                View border = h.itemView.findViewById(R.id.selection_border);
                ImageView iconView = h.itemView.findViewById(R.id.square_icon);

                if (isColor) {
                    container.setBackgroundTintList(ColorStateList.valueOf((int) items[pos]));
                    iconView.setVisibility(View.GONE);
                } else {
                    container.setBackgroundTintList(ColorStateList.valueOf(0xFFF0F2F5));
                    iconView.setImageResource((int) items[pos]);
                    iconView.setVisibility(View.VISIBLE);
                    iconView.setImageTintList(ColorStateList.valueOf(holder.selectedColor));
                }

                border.setVisibility(pos == selectedPos ? View.VISIBLE : View.GONE);

                h.itemView.setOnClickListener(v -> {
                    if (selectedPos == h.getAdapterPosition()) return;

                    int previousPos = selectedPos;
                    selectedPos = h.getAdapterPosition();

                    notifyItemChanged(previousPos);
                    notifyItemChanged(selectedPos);

                    if (isColor) {
                        holder.selectedColor = (int) items[selectedPos];
                        if (holder.rvIcons != null && holder.rvIcons.getAdapter() != null) {
                            holder.rvIcons.getAdapter().notifyDataSetChanged();
                        }
                    } else {
                        holder.selectedIcon = getResources().getResourceEntryName((int) items[selectedPos]);
                    }
                });
            }

            @Override
            public int getItemCount() { return items.length; }
        };

        rv.setAdapter(adapter);
        if (initialPos != -1) rv.scrollToPosition(initialPos);
    }
}