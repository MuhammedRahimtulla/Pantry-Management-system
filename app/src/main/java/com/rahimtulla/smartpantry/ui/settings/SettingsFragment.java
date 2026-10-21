package com.rahimtulla.smartpantry.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rahimtulla.smartpantry.R;
import com.rahimtulla.smartpantry.data.AppDatabase;
import com.rahimtulla.smartpantry.util.Prefs;

/**
 * Settings screen. PreferenceFragmentCompat builds the UI from res/xml/preferences.xml and saves each
 * change to SharedPreferences automatically; the rest of the app reads the values through {@link Prefs}.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference clear = findPreference(Prefs.KEY_CLEAR_PANTRY);
        if (clear != null) {
            clear.setOnPreferenceClickListener(p -> {
                confirmClearPantry();
                return true;
            });
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle(R.string.title_settings);
    }

    private void confirmClearPantry() {
        Context appContext = requireContext().getApplicationContext();
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.clear_confirm_title)
                .setMessage(R.string.clear_confirm_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.clear_confirm_ok, (dialog, which) ->
                        AppDatabase.EXECUTOR.execute(() -> {
                            AppDatabase.get(appContext).pantryDao().deleteAll();
                            View root = getView();
                            if (root != null) {
                                root.post(() -> Toast.makeText(appContext, R.string.pantry_cleared,
                                        Toast.LENGTH_SHORT).show());
                            }
                        }))
                .show();
    }
}
