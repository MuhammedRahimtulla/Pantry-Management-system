package com.rahimtulla.smartpantry.ui.recipes;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.rahimtulla.smartpantry.R;
import com.rahimtulla.smartpantry.data.AppDatabase;
import com.rahimtulla.smartpantry.data.PantryItem;
import com.rahimtulla.smartpantry.data.RecipeWithIngredients;
import com.rahimtulla.smartpantry.logic.RecipeMatcher;
import com.rahimtulla.smartpantry.ui.pantry.AddEditIngredientActivity;
import com.rahimtulla.smartpantry.util.Formatters;
import com.rahimtulla.smartpantry.util.Prefs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Recipe list screen, used in two modes:
 * <ul>
 *   <li>{@link #suggested()} - the "Suggested Recipes" screen: ONLY recipes that pass the strict-matching rule.</li>
 *   <li>{@link #all()} - the full recipe collection, with a "Ready" label on recipes that can be cooked now.</li>
 * </ul>
 * The list is recalculated every time the pantry table changes.
 */
public class RecipeListFragment extends Fragment {

    private static final String ARG_SUGGESTED_ONLY = "suggested_only";

    public static RecipeListFragment suggested() {
        return create(true);
    }

    public static RecipeListFragment all() {
        return create(false);
    }

    private static RecipeListFragment create(boolean suggestedOnly) {
        Bundle args = new Bundle();
        args.putBoolean(ARG_SUGGESTED_ONLY, suggestedOnly);
        RecipeListFragment fragment = new RecipeListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private boolean suggestedOnly;
    private RecipeAdapter adapter;
    private TextView summary;
    private View content, emptyState, progress;
    /** Recipes never change at runtime, so they are loaded once and reused for every recalculation. */
    @Nullable
    private List<RecipeWithIngredients> recipesCache;

    public RecipeListFragment() {
        super(R.layout.fragment_recipe_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        suggestedOnly = requireArguments().getBoolean(ARG_SUGGESTED_ONLY);
        requireActivity().setTitle(suggestedOnly ? R.string.title_suggested : R.string.title_recipes);

        summary = view.findViewById(R.id.summary);
        content = view.findViewById(R.id.content);
        emptyState = view.findViewById(R.id.empty_state);
        progress = view.findViewById(R.id.progress);
        view.findViewById(R.id.empty_add_button).setOnClickListener(v ->
                startActivity(AddEditIngredientActivity.newIntent(requireContext())));

        RecyclerView list = view.findViewById(R.id.recipe_list);
        adapter = new RecipeAdapter(this::openRecipe);
        list.setAdapter(adapter);

        // Whenever the pantry changes (add, edit, delete, cook), re-run the matching.
        AppDatabase.get(requireContext()).pantryDao().observeAll()
                .observe(getViewLifecycleOwner(), this::recalculate);
    }

    /** Runs the strict matcher on a background thread, then shows the result on the UI thread. */
    private void recalculate(List<PantryItem> pantry) {
        Context appContext = requireContext().getApplicationContext();
        boolean excludeExpired = Prefs.excludeExpired(appContext);
        long today = Formatters.todayEpochDay();
        AppDatabase.EXECUTOR.execute(() -> {
            if (recipesCache == null) {
                recipesCache = AppDatabase.get(appContext).recipeDao().getAllWithIngredients();
            }
            List<RecipeWithIngredients> recipes = recipesCache;
            List<RecipeMatcher.RecipeMatch> matches = RecipeMatcher.suggest(recipes, pantry, today, excludeExpired);

            List<RecipeWithIngredients> makeable = new ArrayList<>();
            Set<Long> readyIds = new HashSet<>();
            for (RecipeMatcher.RecipeMatch match : matches) {
                makeable.add(match.recipe);
                readyIds.add(match.recipe.recipe.id);
            }

            View root = getView();
            if (root != null) {
                root.post(() -> show(suggestedOnly ? makeable : recipes, readyIds, recipes.size()));
            }
        });
    }

    private void show(List<RecipeWithIngredients> recipes, Set<Long> readyIds, int total) {
        if (!isAdded()) {
            return;
        }
        progress.setVisibility(View.GONE);
        adapter.setReadyIds(readyIds);
        adapter.submitList(recipes);

        boolean empty = suggestedOnly && recipes.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        content.setVisibility(empty ? View.GONE : View.VISIBLE);
        summary.setText(suggestedOnly
                ? getString(R.string.suggested_summary, readyIds.size(), total)
                : getString(R.string.all_summary, total));
    }

    private void openRecipe(RecipeWithIngredients recipe) {
        // Opening the recipe detail screen is added together with that screen.
    }
}
