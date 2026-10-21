package com.rahimtulla.smartpantry.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Create / Read / Update / Delete operations for pantry items.
 */
@Dao
public interface PantryDao {

    @Insert
    long insert(PantryItem item);

    @Update
    void update(PantryItem item);

    @Delete
    void delete(PantryItem item);

    @Query("DELETE FROM pantry_items")
    void deleteAll();

    /** Observable list for the pantry screen: the RecyclerView refreshes automatically on any change. */
    @Query("SELECT * FROM pantry_items ORDER BY name COLLATE NOCASE")
    LiveData<List<PantryItem>> observeAll();

    /** Items that expire first come first, so cooking uses up the oldest stock before newer stock. */
    @Query("SELECT * FROM pantry_items ORDER BY expiry_epoch_day IS NULL, expiry_epoch_day, id")
    List<PantryItem> getAllOldestFirst();

    @Query("SELECT * FROM pantry_items WHERE id = :id")
    PantryItem getById(long id);
}
