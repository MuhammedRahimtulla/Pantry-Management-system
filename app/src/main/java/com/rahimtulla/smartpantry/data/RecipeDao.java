package com.rahimtulla.smartpantry.data;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

/**
 * Read access to the seeded recipe collection.
 */
@Dao
public interface RecipeDao {

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY name COLLATE NOCASE")
    List<RecipeWithIngredients> getAllWithIngredients();

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    RecipeWithIngredients getWithIngredients(long id);

    /** Every distinct ingredient name used by the recipes - used for autocomplete on the add screen. */
    @Query("SELECT DISTINCT name FROM recipe_ingredients ORDER BY name COLLATE NOCASE")
    List<String> getAllIngredientNames();
}
