package com.rahimtulla.smartpantry.logic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Turns a free-text ingredient name into a canonical key so that trivially different spellings
 * match each other: "Tomatoes", " tomato " and "Fresh tomatoes" all become "tomato".
 *
 * Both the pantry and the recipes are normalised with the same function, so the key only has to be
 * consistent - it does not have to be perfect English.
 */
public final class IngredientNormalizer {

    /** Descriptive words that do not change what the ingredient is. */
    private static final Set<String> DESCRIPTORS = new HashSet<>(Arrays.asList(
            "fresh", "large", "small", "medium", "ripe", "chopped", "diced", "sliced",
            "grated", "peeled", "raw", "organic", "free", "range", "of"));

    /** Irregular plurals that the suffix rules below would get wrong. */
    private static final Map<String, String> IRREGULAR = new HashMap<>();

    /** Different names for the same thing, mapped to one canonical name (applied after singularising). */
    private static final Map<String, String> SYNONYMS = new HashMap<>();

    static {
        IRREGULAR.put("leaves", "leaf");
        IRREGULAR.put("loaves", "loaf");
        IRREGULAR.put("halves", "half");
        IRREGULAR.put("cookies", "cookie");
        IRREGULAR.put("brownies", "brownie");

        SYNONYMS.put("scallion", "spring onion");
        SYNONYMS.put("green onion", "spring onion");
        SYNONYMS.put("ground beef", "beef mince");
        SYNONYMS.put("minced beef", "beef mince");
        SYNONYMS.put("mince", "beef mince");
        SYNONYMS.put("chili", "chilli");
        SYNONYMS.put("chile", "chilli");
        SYNONYMS.put("capsicum", "bell pepper");
        SYNONYMS.put("sweet pepper", "bell pepper");
        SYNONYMS.put("cilantro", "coriander");
        SYNONYMS.put("mealie meal", "maize meal");
        SYNONYMS.put("mielie meal", "maize meal");
        SYNONYMS.put("garbanzo", "chickpea");
        SYNONYMS.put("garbanzo bean", "chickpea");
        SYNONYMS.put("courgette", "zucchini");
        SYNONYMS.put("baby marrow", "zucchini");
        SYNONYMS.put("plain flour", "flour");
        SYNONYMS.put("cake flour", "flour");
        SYNONYMS.put("all purpose flour", "flour");
        SYNONYMS.put("white sugar", "sugar");
        SYNONYMS.put("castor sugar", "sugar");
        SYNONYMS.put("vegetable oil", "cooking oil");
        SYNONYMS.put("sunflower oil", "cooking oil");
        SYNONYMS.put("oil", "cooking oil");
        SYNONYMS.put("hen egg", "egg");
        SYNONYMS.put("chicken egg", "egg");
        SYNONYMS.put("full cream milk", "milk");
        SYNONYMS.put("whole milk", "milk");
        SYNONYMS.put("cheddar", "cheddar cheese");
        SYNONYMS.put("spaghetti pasta", "spaghetti");
    }

    private IngredientNormalizer() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        // 1. Lower-case and replace anything that is not a letter with a space ("free-range" -> "free range").
        String text = raw.toLowerCase(Locale.ROOT).replaceAll("[^a-z]+", " ").trim();
        if (text.isEmpty()) {
            return "";
        }

        // 2. Drop descriptive words and singularise each remaining word.
        StringBuilder sb = new StringBuilder();
        for (String word : text.split(" ")) {
            if (DESCRIPTORS.contains(word)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(singularize(word));
        }
        String key = sb.length() > 0 ? sb.toString() : text;

        // 3. Map synonyms onto one canonical name.
        String canonical = SYNONYMS.get(key);
        return canonical != null ? canonical : key;
    }

    /** Simple English plural-to-singular rules, good enough for ingredient names. */
    static String singularize(String word) {
        String irregular = IRREGULAR.get(word);
        if (irregular != null) {
            return irregular;
        }
        int n = word.length();
        if (n <= 3) {
            return word; // "egg", "pea", "oat" etc. are already short; avoid mangling "gas", "bus".
        }
        if (word.endsWith("ies")) {
            return word.substring(0, n - 3) + "y";      // berries -> berry
        }
        if (word.endsWith("oes")) {
            return word.substring(0, n - 2);            // tomatoes -> tomato, potatoes -> potato
        }
        if (word.endsWith("ches") || word.endsWith("shes") || word.endsWith("xes") || word.endsWith("sses")) {
            return word.substring(0, n - 2);            // peaches -> peach, dishes -> dish
        }
        if (word.endsWith("ss") || word.endsWith("us") || word.endsWith("is")) {
            return word;                                // grass, couscous, hummus, (no change)
        }
        if (word.endsWith("s")) {
            return word.substring(0, n - 1);            // onions -> onion, cheeses -> cheese
        }
        return word;
    }
}
