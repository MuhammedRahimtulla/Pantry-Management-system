package com.rahimtulla.smartpantry.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.rahimtulla.smartpantry.R;
import com.rahimtulla.smartpantry.ui.pantry.PantryFragment;
import com.rahimtulla.smartpantry.util.InsetsHelper;

/**
 * Host activity for the four top-level screens. The bottom navigation bar swaps the fragment shown
 * in the middle of the screen; the add/edit and detail screens are separate activities opened with Intents.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        InsetsHelper.padTop(toolbar);

        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            showTab(item.getItemId());
            return true;
        });
        bottomNav.setOnItemReselectedListener(item -> {
            // Already on this tab - nothing to do.
        });

        // On first launch show the pantry. After a rotation the FragmentManager restores the previous tab itself.
        if (savedInstanceState == null) {
            showTab(R.id.nav_pantry);
        }

        // Back from any other tab returns to the pantry tab before leaving the app.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (bottomNav.getSelectedItemId() != R.id.nav_pantry) {
                    bottomNav.setSelectedItemId(R.id.nav_pantry);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    /** Lets fragments switch tabs, e.g. the pantry's "See recipes" banner. */
    public void selectTab(int menuItemId) {
        bottomNav.setSelectedItemId(menuItemId);
    }

    private void showTab(int menuItemId) {
        Fragment fragment;
        if (menuItemId == R.id.nav_pantry) {
            fragment = new PantryFragment();
        } else {
            fragment = new Fragment(R.layout.fragment_placeholder);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
