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

    /**
     * Every ingredient name used by the recipes with the unit they use - powers autocomplete and the
     * unit suggestion on the add/edit screen.
     */
    @Query("SELECT name, unit FROM recipe_ingredients GROUP BY name ORDER BY name COLLATE NOCASE")
    List<IngredientUnit> getIngredientUnits();
}
