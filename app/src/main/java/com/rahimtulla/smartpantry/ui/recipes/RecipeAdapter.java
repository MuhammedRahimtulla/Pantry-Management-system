package com.rahimtulla.smartpantry.ui.recipes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rahimtulla.smartpantry.R;
import com.rahimtulla.smartpantry.data.RecipeWithIngredients;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Custom RecyclerView adapter that shows recipe cards. Used by both the Suggested and All Recipes lists.
 */
public class RecipeAdapter extends ListAdapter<RecipeWithIngredients, RecipeAdapter.ViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(RecipeWithIngredients recipe);
    }

    private final OnRecipeClickListener listener;
    /** Ids of recipes that can be cooked right now; these get a "Ready" label. */
    private Set<Long> readyIds = Collections.emptySet();

    public RecipeAdapter(OnRecipeClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    public void setReadyIds(Set<Long> readyIds) {
        this.readyIds = new HashSet<>(readyIds);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeWithIngredients r = getItem(position);
        holder.name.setText(r.recipe.name);
        holder.description.setText(r.recipe.description);
        holder.meta.setText(holder.itemView.getContext().getString(
                R.string.recipe_meta, r.recipe.prepMinutes, r.ingredients.size()));
        holder.ready.setVisibility(readyIds.contains(r.recipe.id) ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(r));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView description;
        final TextView meta;
        final TextView ready;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.recipe_name);
            description = itemView.findViewById(R.id.recipe_description);
            meta = itemView.findViewById(R.id.recipe_meta);
            ready = itemView.findViewById(R.id.recipe_ready);
        }
    }

    private static final DiffUtil.ItemCallback<RecipeWithIngredients> DIFF =
            new DiffUtil.ItemCallback<RecipeWithIngredients>() {
                @Override
                public boolean areItemsTheSame(@NonNull RecipeWithIngredients a, @NonNull RecipeWithIngredients b) {
                    return a.recipe.id == b.recipe.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull RecipeWithIngredients a, @NonNull RecipeWithIngredients b) {
                    // Recipes are read-only seed data, so the same id means the same content.
                    return a.recipe.id == b.recipe.id;
                }
            };
}
