package com.rahimtulla.smartpantry.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The app's single on-device SQLite database, accessed through Room.
 * Room does not allow queries on the main thread, so all reads/writes go through {@link #EXECUTOR}.
 */
@Database(
        entities = {PantryItem.class, Recipe.class, RecipeIngredient.class},
        version = 1,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "smart_pantry.db";

    /** Background threads for database work. */
    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);

    private static volatile AppDatabase instance;

    /** Runs once, when the database file is first created on the device: pre-loads the recipe collection. */
    private static final RoomDatabase.Callback SEED_CALLBACK = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            SeedData.insertAll(db);
        }
    };

    public abstract PantryDao pantryDao();

    public abstract RecipeDao recipeDao();

    /** Thread-safe singleton: one database connection shared by the whole app. */
    public static AppDatabase get(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DB_NAME)
                            .addCallback(SEED_CALLBACK)
                            .build();
                }
            }
        }
        return instance;
    }
}
