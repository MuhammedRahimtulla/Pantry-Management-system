package com.rahimtulla.smartpantry.util;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Apps targeting Android 15+ are drawn edge-to-edge, behind the status and navigation bars.
 * These helpers add padding so content is not hidden behind those bars (or the keyboard).
 */
public final class InsetsHelper {

    private InsetsHelper() {
    }

    /** Pads the top of a view (usually the toolbar) by the status-bar height. */
    public static void padTop(View view) {
        final int start = view.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, start + bars.top, bars.right, v.getPaddingBottom());
            return windowInsets;
        });
    }

    /** Pads the bottom of a view by the navigation-bar height, or the keyboard height when it is open. */
    public static void padBottom(View view) {
        final int start = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(),
                    start + Math.max(bars.bottom, ime.bottom));
            return windowInsets;
        });
    }
}
