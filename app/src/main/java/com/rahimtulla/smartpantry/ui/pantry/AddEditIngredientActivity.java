package com.rahimtulla.smartpantry.ui.pantry;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.rahimtulla.smartpantry.R;
import com.rahimtulla.smartpantry.data.AppDatabase;
import com.rahimtulla.smartpantry.data.IngredientUnit;
import com.rahimtulla.smartpantry.data.PantryDao;
import com.rahimtulla.smartpantry.data.PantryItem;
import com.rahimtulla.smartpantry.logic.IngredientNormalizer;
import com.rahimtulla.smartpantry.logic.Unit;
import com.rahimtulla.smartpantry.util.Formatters;
import com.rahimtulla.smartpantry.util.InsetsHelper;
import com.rahimtulla.smartpantry.util.Prefs;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Add Ingredient / Edit Ingredient screen. Started with an Intent:
 * no extra = add a new item; {@link #EXTRA_ITEM_ID} = edit (or delete) that existing item.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "com.rahimtulla.smartpantry.EXTRA_ITEM_ID";
    private static final long NO_ID = -1;
    private static final String STATE_EXPIRY = "state_expiry";
    private static final double MAX_QUANTITY = 100_000;

    /** Builds the Intent that opens this screen; pass NO_ID / use {@link #newIntent(Context)} to add. */
    public static Intent newIntent(Context context, long itemId) {
        Intent intent = new Intent(context, AddEditIngredientActivity.class);
        intent.putExtra(EXTRA_ITEM_ID, itemId);
        return intent;
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, AddEditIngredientActivity.class);
    }

    private PantryDao dao;
    private long itemId = NO_ID;
    /** Expiry date as an epoch day, or null when the user has not chosen one. */
    @Nullable
    private Long expiryEpochDay;
    /** Unit each recipe ingredient is measured in, keyed by normalised name. */
    private final Map<String, Unit> recipeUnits = new HashMap<>();

    private TextInputLayout nameLayout, quantityLayout, unitLayout, expiryLayout;
    private MaterialAutoCompleteTextView nameInput, unitInput;
    private TextInputEditText quantityInput, expiryInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_ingredient);

        dao = AppDatabase.get(this).pantryDao();
        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, NO_ID);
        boolean editing = itemId != NO_ID;

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(editing ? R.string.title_edit_ingredient : R.string.title_add_ingredient);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        InsetsHelper.padTop(toolbar);
        InsetsHelper.padBottom(findViewById(R.id.scroll));

        nameLayout = findViewById(R.id.name_layout);
        quantityLayout = findViewById(R.id.quantity_layout);
        unitLayout = findViewById(R.id.unit_layout);
        expiryLayout = findViewById(R.id.expiry_layout);
        nameInput = findViewById(R.id.name_input);
        quantityInput = findViewById(R.id.quantity_input);
        unitInput = findViewById(R.id.unit_input);
        expiryInput = findViewById(R.id.expiry_input);

        unitInput.setSimpleItems(unitLabels());
        unitInput.setOnItemClickListener((parent, view, position, id) -> {
            unitLayout.setError(null);
            updateUnitHint();
        });
        nameInput.setOnItemClickListener((parent, view, position, id) -> suggestUnitForName());
        nameInput.addTextChangedListener(new SimpleWatcher(() -> {
            nameLayout.setError(null);
            updateUnitHint();
        }));
        quantityInput.addTextChangedListener(new SimpleWatcher(() -> quantityLayout.setError(null)));

        expiryInput.setOnClickListener(v -> pickExpiryDate());
        expiryLayout.setStartIconOnClickListener(v -> pickExpiryDate());
        findViewById(R.id.clear_expiry_button).setOnClickListener(v -> setExpiry(null));
        findViewById(R.id.save_button).setOnClickListener(v -> save());

        Button deleteButton = findViewById(R.id.delete_button);
        deleteButton.setVisibility(editing ? View.VISIBLE : View.GONE);
        deleteButton.setOnClickListener(v -> confirmDelete());

        loadIngredientSuggestions();

        if (savedInstanceState != null) {
            // Text fields restore themselves after rotation; the expiry is held in a field, so restore it here.
            if (savedInstanceState.containsKey(STATE_EXPIRY)) {
                setExpiry(savedInstanceState.getLong(STATE_EXPIRY));
            }
        } else if (editing) {
            loadItem();
        } else {
            setUnit(Unit.parse(Prefs.defaultUnit(this)));
        }
    }

    /** Toolbar back arrow behaves like the system back button. */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (expiryEpochDay != null) {
            outState.putLong(STATE_EXPIRY, expiryEpochDay);
        }
    }

    /** Reads the item being edited on a background thread, then fills in the form on the UI thread. */
    private void loadItem() {
        AppDatabase.EXECUTOR.execute(() -> {
            PantryItem item = dao.getById(itemId);
            runOnUiThread(() -> {
                if (item == null) {
                    Toast.makeText(this, R.string.item_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                nameInput.setText(item.name, false);
                quantityInput.setText(Formatters.quantity(item.quantity));
                setUnit(Unit.parse(item.unit));
                setExpiry(item.expiryEpochDay);
            });
        });
    }

    /** Loads the ingredient names used by recipes for autocomplete (one entry per normalised name). */
    private void loadIngredientSuggestions() {
        AppDatabase.EXECUTOR.execute(() -> {
            List<IngredientUnit> rows = AppDatabase.get(this).recipeDao().getIngredientUnits();
            List<String> names = new ArrayList<>();
            Map<String, Unit> units = new HashMap<>();
            for (IngredientUnit row : rows) {
                String key = IngredientNormalizer.normalize(row.name);
                if (!units.containsKey(key)) {
                    units.put(key, Unit.parse(row.unit));
                    names.add(capitalise(row.name));
                }
            }
            runOnUiThread(() -> {
                recipeUnits.putAll(units);
                nameInput.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names));
                updateUnitHint();
            });
        });
    }

    // ---------------------------------------------------------------------------------------------
    // Validation and saving
    // ---------------------------------------------------------------------------------------------

    private void save() {
        String name = nameInput.getText() == null ? "" : nameInput.getText().toString().trim().replaceAll("\\s+", " ");
        String quantityText = quantityInput.getText() == null ? "" : quantityInput.getText().toString().trim();
        Unit unit = selectedUnit();

        boolean valid = true;

        // Name: required, 2+ characters, letters with spaces/hyphens/apostrophes only.
        if (name.isEmpty()) {
            nameLayout.setError(getString(R.string.error_name_required));
            valid = false;
        } else if (name.length() < 2) {
            nameLayout.setError(getString(R.string.error_name_short));
            valid = false;
        } else if (!name.matches("[\\p{L}][\\p{L} '\\-]*")) {
            nameLayout.setError(getString(R.string.error_name_chars));
            valid = false;
        }

        // Quantity: required, numeric, greater than zero and not absurdly large.
        double quantity = 0;
        if (quantityText.isEmpty()) {
            quantityLayout.setError(getString(R.string.error_quantity_required));
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(quantityText.replace(',', '.'));
                if (quantity <= 0) {
                    quantityLayout.setError(getString(R.string.error_quantity_positive));
                    valid = false;
                } else if (quantity > MAX_QUANTITY) {
                    quantityLayout.setError(getString(R.string.error_quantity_too_big));
                    valid = false;
                }
            } catch (NumberFormatException e) {
                quantityLayout.setError(getString(R.string.error_quantity_number));
                valid = false;
            }
        }

        // Unit: must be one of the listed units.
        if (unit == null) {
            unitLayout.setError(getString(R.string.error_unit_required));
            valid = false;
        }

        // Expiry: optional, but a brand-new item cannot already be expired.
        if (itemId == NO_ID && expiryEpochDay != null && expiryEpochDay < Formatters.todayEpochDay()) {
            expiryLayout.setError(getString(R.string.error_expiry_past));
            valid = false;
        }

        if (!valid) {
            return;
        }

        PantryItem item = new PantryItem(capitalise(name), quantity, unit.code, expiryEpochDay);
        AppDatabase.EXECUTOR.execute(() -> {
            if (itemId == NO_ID) {
                dao.insert(item);           // Create
            } else {
                item.id = itemId;
                dao.update(item);           // Update
            }
            runOnUiThread(() -> {
                Toast.makeText(this, getString(R.string.saved, item.name), Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void confirmDelete() {
        String name = nameInput.getText() == null ? "" : nameInput.getText().toString();
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(getString(R.string.delete_confirm_message, name))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    PantryItem item = new PantryItem();
                    item.id = itemId;     // Room deletes by primary key
                    AppDatabase.EXECUTOR.execute(() -> {
                        dao.delete(item);   // Delete
                        runOnUiThread(() -> {
                            Toast.makeText(this, getString(R.string.deleted, name), Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                })
                .show();
    }

    // ---------------------------------------------------------------------------------------------
    // Units
    // ---------------------------------------------------------------------------------------------

    private String[] unitLabels() {
        int[] ids = {R.string.unit_g, R.string.unit_kg, R.string.unit_ml, R.string.unit_l,
                R.string.unit_tsp, R.string.unit_tbsp, R.string.unit_cup, R.string.unit_pcs};
        String[] labels = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            labels[i] = getString(ids[i]);
        }
        return labels;
    }

    /** The unit chosen in the dropdown; labels look like "g (grams)", so the code is the first word. */
    @Nullable
    private Unit selectedUnit() {
        String text = unitInput.getText() == null ? "" : unitInput.getText().toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        return Unit.parse(text.split(" ")[0]);
    }

    private void setUnit(@Nullable Unit unit) {
        if (unit == null) {
            return;
        }
        for (String label : unitLabels()) {
            if (label.split(" ")[0].equals(unit.code)) {
                unitInput.setText(label, false);   // false = don't filter the dropdown
                break;
            }
        }
        updateUnitHint();
    }

    /** When an ingredient is picked from the suggestions, pre-select the unit recipes use for it. */
    private void suggestUnitForName() {
        Unit recipeUnit = recipeUnits.get(IngredientNormalizer.normalize(nameInput.getText().toString()));
        if (recipeUnit != null) {
            Unit current = selectedUnit();
            if (current == null || !current.isCompatibleWith(recipeUnit)) {
                setUnit(recipeUnit);
            }
        }
    }

    /** Warns when the chosen unit cannot be compared with the unit recipes use for this ingredient. */
    private void updateUnitHint() {
        if (nameInput.getText() == null) {
            return;
        }
        Unit recipeUnit = recipeUnits.get(IngredientNormalizer.normalize(nameInput.getText().toString()));
        Unit current = selectedUnit();
        if (recipeUnit == null) {
            unitLayout.setHelperText(null);
        } else if (current != null && !current.isCompatibleWith(recipeUnit)) {
            unitLayout.setHelperText(getString(R.string.unit_mismatch_warning, recipeUnit.code));
        } else {
            unitLayout.setHelperText(getString(R.string.unit_suggestion, recipeUnit.code));
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Expiry date
    // ---------------------------------------------------------------------------------------------

    private void pickExpiryDate() {
        LocalDate initial = expiryEpochDay != null ? LocalDate.ofEpochDay(expiryEpochDay) : LocalDate.now().plusDays(7);
        DatePickerDialog dialog = new DatePickerDialog(this,
                (picker, year, month, day) -> setExpiry(LocalDate.of(year, month + 1, day).toEpochDay()),
                initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth());
        if (itemId == NO_ID) {
            // New items cannot be added already expired.
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        }
        dialog.show();
    }

    private void setExpiry(@Nullable Long epochDay) {
        expiryEpochDay = epochDay;
        expiryLayout.setError(null);
        expiryInput.setText(epochDay == null ? "" : Formatters.date(epochDay));
    }

    private static String capitalise(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /** TextWatcher that only cares that the text changed. */
    private static class SimpleWatcher implements TextWatcher {
        private final Runnable onChange;

        SimpleWatcher(Runnable onChange) {
            this.onChange = onChange;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onChange.run();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
