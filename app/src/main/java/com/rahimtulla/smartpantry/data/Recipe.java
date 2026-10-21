package com.rahimtulla.smartpantry.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A recipe in the built-in collection (a row in the recipes table).
 * Its ingredients live in the separate recipe_ingredients table (one-to-many).
 */
@Entity(tableName = "recipes")
public class Recipe {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "name")
    public String name = "";

    @NonNull
    @ColumnInfo(name = "description")
    public String description = "";

    /** Preparation steps, one step per line. */
    @NonNull
    @ColumnInfo(name = "steps")
    public String steps = "";

    @ColumnInfo(name = "prep_minutes")
    public int prepMinutes;
}
