package com.rahimtulla.smartpantry.data;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

/**
 * A recipe together with all of its ingredient rows, loaded by Room in one @Transaction query.
 */
public class RecipeWithIngredients {

    @Embedded
    public Recipe recipe;

    @Relation(parentColumn = "id", entityColumn = "recipe_id")
    public List<RecipeIngredient> ingredients;
}
