package com.example.hydrationv2r.fragments;

import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hydrationv2r.R;
import com.example.hydrationv2r.adapters.TodayHistoryAdapter;
import com.example.hydrationv2r.helpers.DatabaseHelper;
import com.example.hydrationv2r.models.LoggedDrinkModel;

import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class TodayHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tv_empty;

    public TodayHistoryFragment() {
        // Required empty public constructor
    }

    public static TodayHistoryFragment newInstance(String param1, String param2) {
        TodayHistoryFragment fragment = new TodayHistoryFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.today_history_fragment, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.rv_today_history);
        DatabaseHelper db = DatabaseHelper.getInstance(getContext());

        tv_empty = view.findViewById(R.id.tv_blank);
        tv_empty.setVisibility(View.GONE);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (recyclerView.getAdapter() instanceof TodayHistoryAdapter) {
                    TodayHistoryAdapter adapter = (TodayHistoryAdapter) recyclerView.getAdapter();

                    int position = viewHolder.getAdapterPosition();
                    LoggedDrinkModel deletedLog = adapter.getLogAt(position);

                    if (DatabaseHelper.getInstance(getContext()).deleteSpecificLog(deletedLog.logId)) {
                        adapter.removeLog(position);

                        // Handle showing the empty state text if no items remain after swipe
                        if (adapter.getItemCount() == 0) {
                            tv_empty.setVisibility(View.VISIBLE);
                        }

                        // Refresh parent layout view metrics
                        Fragment parent = getParentFragment();
                        if (parent instanceof HydrateFragment) {
                            ((HydrateFragment) parent).viewModel.refreshTodayTotal();
                        }
                    }
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {

                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(getContext(), R.color.red))
                        .addSwipeLeftActionIcon(R.drawable.bin)
                        .addSwipeLeftLabel("Delete")
                        .setSwipeLeftLabelColor(Color.WHITE)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }

    @Override
    public void onResume() {
        super.onResume();
        // Force database data requery whenever fragment becomes visible
        DatabaseHelper db = DatabaseHelper.getInstance(getContext());
        List<LoggedDrinkModel> drinks = db.getLogsForToday();

        if (drinks == null || drinks.isEmpty()) {
            tv_empty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tv_empty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            // Assign fresh adapter data list cleanly
            TodayHistoryAdapter adapter = new TodayHistoryAdapter(getContext(), getView(), this, drinks);
            recyclerView.setAdapter(adapter);
        }
    }
}
