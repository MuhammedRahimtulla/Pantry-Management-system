package com.rahimtulla.smartpantry.ui.recipes;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.rahimtulla.smartpantry.R;
import com.rahimtulla.smartpantry.data.AppDatabase;
import com.rahimtulla.smartpantry.data.PantryItem;
import com.rahimtulla.smartpantry.data.RecipeIngredient;
import com.rahimtulla.smartpantry.data.RecipeWithIngredients;
import com.rahimtulla.smartpantry.logic.RecipeMatcher;
import com.rahimtulla.smartpantry.util.Formatters;
import com.rahimtulla.smartpantry.util.InsetsHelper;
import com.rahimtulla.smartpantry.util.Prefs;

import java.util.List;

/**
 * Recipe Detail screen: full ingredient list (each checked against the pantry) and the method.
 * Opened with an Intent carrying {@link #EXTRA_RECIPE_ID}.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "com.rahimtulla.smartpantry.EXTRA_RECIPE_ID";

    public static Intent newIntent(Context context, long recipeId) {
        Intent intent = new Intent(context, RecipeDetailActivity.class);
        intent.putExtra(EXTRA_RECIPE_ID, recipeId);
        return intent;
    }

    private long recipeId;
    private TextView nameView, descriptionView, timeView, statusText;
    private MaterialCardView statusCard;
    private LinearLayout ingredientsContainer, stepsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");
        InsetsHelper.padTop(toolbar);
        InsetsHelper.padBottom(findViewById(R.id.scroll));

        nameView = findViewById(R.id.recipe_name);
        descriptionView = findViewById(R.id.recipe_description);
        timeView = findViewById(R.id.recipe_time);
        statusCard = findViewById(R.id.status_card);
        statusText = findViewById(R.id.status_text);
        ingredientsContainer = findViewById(R.id.ingredients_container);
        stepsContainer = findViewById(R.id.steps_container);

        recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
    }

    /** Loaded in onResume (not onCreate) so the ticks are up to date after editing the pantry and coming back. */
    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void load() {
        boolean excludeExpired = Prefs.excludeExpired(this);
        long today = Formatters.todayEpochDay();
        AppDatabase db = AppDatabase.get(this);
        AppDatabase.EXECUTOR.execute(() -> {
            RecipeWithIngredients recipe = db.recipeDao().getWithIngredients(recipeId);
            List<PantryItem> pantry = db.pantryDao().getAllOldestFirst();
            RecipeMatcher.RecipeMatch match = recipe == null ? null
                    : RecipeMatcher.evaluate(recipe, pantry, today, excludeExpired);
            runOnUiThread(() -> {
                if (match == null) {
                    Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                } else if (!isFinishing()) {
                    show(match);
                }
            });
        });
    }

    private void show(RecipeMatcher.RecipeMatch match) {
        RecipeWithIngredients r = match.recipe;
        setTitle(r.recipe.name);
        nameView.setText(r.recipe.name);
        descriptionView.setText(r.recipe.description);
        timeView.setText(getString(R.string.prep_minutes, r.recipe.prepMinutes));

        // Overall status banner
        int okColor = ContextCompat.getColor(this, R.color.status_ok);
        int badColor = ContextCompat.getColor(this, R.color.status_expired_text);
        if (match.canMake()) {
            statusText.setText(R.string.status_can_make);
            statusText.setTextColor(okColor);
            statusCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_ok_bg));
        } else {
            int problems = 0;
            for (RecipeMatcher.IngredientCheck check : match.checks) {
                if (check.status != RecipeMatcher.Status.HAVE_ENOUGH) {
                    problems++;
                }
            }
            statusText.setText(getResources().getQuantityString(R.plurals.status_cannot_make, problems, problems));
            statusText.setTextColor(badColor);
            statusCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_expired_bg));
        }

        // Ingredients, each with a tick or cross
        ingredientsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (RecipeMatcher.IngredientCheck check : match.checks) {
            View row = inflater.inflate(R.layout.item_ingredient_check, ingredientsContainer, false);
            ImageView icon = row.findViewById(R.id.check_icon);
            TextView name = row.findViewById(R.id.check_name);
            TextView detail = row.findViewById(R.id.check_detail);

            RecipeIngredient need = check.required;
            name.setText(Formatters.amount(need.quantity, need.unit) + "  " + need.name);
            String available = Formatters.amount(check.availableInRequiredUnit, need.unit);
            switch (check.status) {
                case HAVE_ENOUGH:
                    detail.setText(getString(R.string.check_have, available));
                    break;
                case NOT_ENOUGH:
                    detail.setText(getString(R.string.check_not_enough, available));
                    break;
                case UNIT_MISMATCH:
                    detail.setText(getString(R.string.check_unit_mismatch, need.unit));
                    break;
                default:
                    detail.setText(R.string.check_missing);
            }
            boolean ok = check.status == RecipeMatcher.Status.HAVE_ENOUGH;
            icon.setImageResource(ok ? R.drawable.ic_check_circle : R.drawable.ic_cancel);
            icon.setImageTintList(ColorStateList.valueOf(ok ? okColor : badColor));
            detail.setTextColor(ok ? okColor : badColor);
            ingredientsContainer.addView(row);
        }

        // Numbered method steps (stored one per line)
        stepsContainer.removeAllViews();
        String[] steps = r.recipe.steps.split("\n");
        for (int i = 0; i < steps.length; i++) {
            View row = inflater.inflate(R.layout.item_step, stepsContainer, false);
            ((TextView) row.findViewById(R.id.step_number)).setText(getString(R.string.step_number, i + 1));
            ((TextView) row.findViewById(R.id.step_text)).setText(steps[i]);
            stepsContainer.addView(row);
        }
    }
}
