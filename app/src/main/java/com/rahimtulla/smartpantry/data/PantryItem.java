package com.rahimtulla.smartpantry.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * One ingredient the user currently has at home (a row in the pantry_items table).
 */
@Entity(tableName = "pantry_items")
public class PantryItem {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "name")
    public String name = "";

    @ColumnInfo(name = "quantity")
    public double quantity;

    /** Unit code as stored by {@link com.rahimtulla.smartpantry.logic.Unit#code}, e.g. "g", "ml", "pcs". */
    @NonNull
    @ColumnInfo(name = "unit")
    public String unit = "pcs";

    /** Optional expiry date stored as a LocalDate epoch day (days since 1970-01-01); null = no expiry. */
    @Nullable
    @ColumnInfo(name = "expiry_epoch_day")
    public Long expiryEpochDay;

    public PantryItem() {
    }

    @Ignore
    public PantryItem(@NonNull String name, double quantity, @NonNull String unit, @Nullable Long expiryEpochDay) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryEpochDay = expiryEpochDay;
    }

    /** Copy constructor, used when we need to modify an item without touching the original. */
    @Ignore
    public PantryItem(@NonNull PantryItem other) {
        this.id = other.id;
        this.name = other.name;
        this.quantity = other.quantity;
        this.unit = other.unit;
        this.expiryEpochDay = other.expiryEpochDay;
    }
}
