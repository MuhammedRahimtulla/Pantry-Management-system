package com.rahimtulla.smartpantry.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

/**
 * Keys and typed getters for the user's settings (stored in the default SharedPreferences file,
 * which is what the Settings screen's PreferenceFragment writes to).
 */
public final class Prefs {

    public static final String KEY_EXPIRY_HIGHLIGHT = "expiry_highlight";
    public static final String KEY_EXPIRY_WINDOW_DAYS = "expiry_window_days";
    public static final String KEY_EXCLUDE_EXPIRED = "exclude_expired";
    public static final String KEY_DEFAULT_UNIT = "default_unit";
    public static final String KEY_CLEAR_PANTRY = "clear_pantry";

    private Prefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /** Whether items close to expiry are highlighted on the pantry screen. */
    public static boolean isExpiryHighlightOn(Context context) {
        return prefs(context).getBoolean(KEY_EXPIRY_HIGHLIGHT, true);
    }

    /** How many days ahead counts as "expiring soon". */
    public static int expiryWindowDays(Context context) {
        try {
            return Integer.parseInt(prefs(context).getString(KEY_EXPIRY_WINDOW_DAYS, "3"));
        } catch (NumberFormatException e) {
            return 3;
        }
    }

    /** Whether expired items are ignored when matching recipes (on by default, for food safety). */
    public static boolean excludeExpired(Context context) {
        return prefs(context).getBoolean(KEY_EXCLUDE_EXPIRED, true);
    }

    /** Unit pre-selected when adding a new pantry item. */
    public static String defaultUnit(Context context) {
        return prefs(context).getString(KEY_DEFAULT_UNIT, "pcs");
    }
}
