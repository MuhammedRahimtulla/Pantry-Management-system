package com.rahimtulla.smartpantry.ui.pantry;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.rahimtulla.smartpantry.R;
import com.rahimtulla.smartpantry.data.AppDatabase;
import com.rahimtulla.smartpantry.data.PantryDao;
import com.rahimtulla.smartpantry.data.PantryItem;
import com.rahimtulla.smartpantry.ui.MainActivity;
import com.rahimtulla.smartpantry.util.Formatters;
import com.rahimtulla.smartpantry.util.Prefs;

import java.util.List;

/**
 * Pantry List screen: shows every ingredient the user has, read live from the database.
 */
public class PantryFragment extends Fragment {

    /** How long the Undo option stays on screen after a swipe-delete (LENGTH_LONG is under 3 seconds). */
    private static final int UNDO_DURATION_MS = 5000;

    private PantryAdapter adapter;
    private View emptyState;
    private View expiringBanner;
    private TextView expiringBannerText;

    public PantryFragment() {
        super(R.layout.fragment_pantry);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle(R.string.title_pantry);

        emptyState = view.findViewById(R.id.empty_state);
        expiringBanner = view.findViewById(R.id.expiring_banner);
        expiringBannerText = view.findViewById(R.id.expiring_banner_text);
        view.findViewById(R.id.expiring_banner_button).setOnClickListener(v ->
                ((MainActivity) requireActivity()).selectTab(R.id.nav_suggested));

        RecyclerView list = view.findViewById(R.id.pantry_list);
        adapter = new PantryAdapter(this::onItemClicked);
        list.setAdapter(adapter);

        attachSwipeToDelete(list, view.findViewById(R.id.pantry_root));

        view.findViewById(R.id.fab_add).setOnClickListener(v ->
                startActivity(AddEditIngredientActivity.newIntent(requireContext())));

        // LiveData: Room re-runs the query and calls this observer every time the pantry table changes,
        // and stops automatically when this fragment's view is destroyed.
        AppDatabase.get(requireContext()).pantryDao().observeAll()
                .observe(getViewLifecycleOwner(), this::showItems);
    }

    /** Settings may have changed while we were away, so re-apply them every time the screen is shown. */
    @Override
    public void onResume() {
        super.onResume();
        adapter.setExpirySettings(Formatters.todayEpochDay(),
                Prefs.isExpiryHighlightOn(requireContext()),
                Prefs.expiryWindowDays(requireContext()));
        updateExpiringBanner(adapter.getCurrentList());
    }

    private void showItems(List<PantryItem> items) {
        adapter.submitList(items);
        emptyState.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        updateExpiringBanner(items);
    }

    /** Shows a banner when items will expire within the user's chosen window. */
    private void updateExpiringBanner(List<PantryItem> items) {
        if (!Prefs.isExpiryHighlightOn(requireContext())) {
            expiringBanner.setVisibility(View.GONE);
            return;
        }
        long today = Formatters.todayEpochDay();
        int window = Prefs.expiryWindowDays(requireContext());
        int count = 0;
        for (PantryItem item : items) {
            if (item.expiryEpochDay != null) {
                long daysLeft = item.expiryEpochDay - today;
                if (daysLeft >= 0 && daysLeft <= window) {
                    count++;
                }
            }
        }
        expiringBanner.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        expiringBannerText.setText(getResources().getQuantityString(R.plurals.expiring_banner, count, count, window));
    }

    /**
     * Swipe a row left or right to delete it. The deleted item is kept in memory so the Snackbar's
     * Undo button can insert it back (with the same id).
     */
    private void attachSwipeToDelete(RecyclerView list, View snackbarParent) {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return;
                }
                PantryItem deleted = adapter.getItemAt(position);
                PantryDao dao = AppDatabase.get(requireContext()).pantryDao();
                AppDatabase.EXECUTOR.execute(() -> dao.delete(deleted));

                Snackbar.make(snackbarParent, getString(R.string.deleted, deleted.name), UNDO_DURATION_MS)
                        .setAnchorView(R.id.fab_add)
                        .setAction(R.string.undo, v -> AppDatabase.EXECUTOR.execute(() -> dao.insert(deleted)))
                        .show();
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(list);
    }

    /** Explicit Intent carrying the item's id, so the editor knows which row to load. */
    private void onItemClicked(PantryItem item) {
        startActivity(AddEditIngredientActivity.newIntent(requireContext(), item.id));
    }
}
