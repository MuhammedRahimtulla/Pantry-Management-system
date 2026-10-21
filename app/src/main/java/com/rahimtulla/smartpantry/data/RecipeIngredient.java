package com.rahimtulla.smartpantry.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * One ingredient line required by a recipe, e.g. "200 g rice".
 * Deleting a recipe cascades to its ingredient rows.
 */
@Entity(
        tableName = "recipe_ingredients",
        foreignKeys = @ForeignKey(
                entity = Recipe.class,
                parentColumns = "id",
                childColumns = "recipe_id",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("recipe_id"))
public class RecipeIngredient {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "recipe_id")
    public long recipeId;

    @NonNull
    @ColumnInfo(name = "name")
    public String name = "";

    @ColumnInfo(name = "quantity")
    public double quantity;

    @NonNull
    @ColumnInfo(name = "unit")
    public String unit = "pcs";

    public RecipeIngredient() {
    }

    @Ignore
    public RecipeIngredient(@NonNull String name, double quantity, @NonNull String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }
}
