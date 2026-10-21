package com.rahimtulla.smartpantry.logic;

import com.rahimtulla.smartpantry.data.PantryItem;
import com.rahimtulla.smartpantry.data.RecipeIngredient;
import com.rahimtulla.smartpantry.data.RecipeWithIngredients;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The strict-matching rule: a recipe is suggested only if EVERY ingredient it needs is in the pantry
 * in at least the required quantity. One missing or short ingredient disqualifies the recipe.
 *
 * Matching is made robust by comparing normalised names ({@link IngredientNormalizer}) and by
 * converting quantities to a common base unit ({@link Unit}) before comparing them.
 */
public final class RecipeMatcher {

    /** Tolerance for floating-point comparisons (e.g. 0.1 + 0.2 vs 0.3). */
    private static final double EPSILON = 1e-6;

    public enum Status {
        /** Pantry has this ingredient in at least the required amount. */
        HAVE_ENOUGH,
        /** Pantry has this ingredient, but not enough of it. */
        NOT_ENOUGH,
        /** Pantry has this ingredient but only in an incompatible unit (e.g. pieces vs grams). */
        UNIT_MISMATCH,
        /** Ingredient is not in the pantry at all (or only expired stock). */
        MISSING
    }

    /** Result of checking one recipe ingredient against the pantry. */
    public static final class IngredientCheck {
        public final RecipeIngredient required;
        public final Status status;
        /** Total usable pantry amount, expressed in the recipe's unit (0 if missing/incompatible). */
        public final double availableInRequiredUnit;

        IngredientCheck(RecipeIngredient required, Status status, double availableInRequiredUnit) {
            this.required = required;
            this.status = status;
            this.availableInRequiredUnit = availableInRequiredUnit;
        }
    }

    /** Result of checking a whole recipe. */
    public static final class RecipeMatch {
        public final RecipeWithIngredients recipe;
        public final List<IngredientCheck> checks;

        RecipeMatch(RecipeWithIngredients recipe, List<IngredientCheck> checks) {
            this.recipe = recipe;
            this.checks = Collections.unmodifiableList(checks);
        }

        /** True only when the recipe has ingredients and every single one is satisfied. */
        public boolean canMake() {
            if (checks.isEmpty()) {
                return false;
            }
            for (IngredientCheck check : checks) {
                if (check.status != Status.HAVE_ENOUGH) {
                    return false;
                }
            }
            return true;
        }
    }

    private RecipeMatcher() {
    }

    /** Whether a pantry item may be used for cooking today. */
    public static boolean isUsable(PantryItem item, long todayEpochDay, boolean excludeExpired) {
        if (item.quantity <= EPSILON) {
            return false;
        }
        boolean expired = item.expiryEpochDay != null && item.expiryEpochDay < todayEpochDay;
        return !(excludeExpired && expired);
    }

    /**
     * Returns only the recipes that can be made right now (the "Suggested Recipes" list).
     */
    public static List<RecipeMatch> suggest(List<RecipeWithIngredients> recipes, List<PantryItem> pantry,
                                            long todayEpochDay, boolean excludeExpired) {
        Map<String, List<PantryItem>> index = indexPantry(pantry, todayEpochDay, excludeExpired);
        List<RecipeMatch> result = new ArrayList<>();
        for (RecipeWithIngredients recipe : recipes) {
            RecipeMatch match = evaluate(recipe, index);
            if (match.canMake()) {
                result.add(match);
            }
        }
        return result;
    }

    /** Checks one recipe against the pantry, ingredient by ingredient (used by the detail screen). */
    public static RecipeMatch evaluate(RecipeWithIngredients recipe, List<PantryItem> pantry,
                                       long todayEpochDay, boolean excludeExpired) {
        return evaluate(recipe, indexPantry(pantry, todayEpochDay, excludeExpired));
    }

    private static RecipeMatch evaluate(RecipeWithIngredients recipe, Map<String, List<PantryItem>> index) {
        List<IngredientCheck> checks = new ArrayList<>();
        for (RecipeIngredient required : recipe.ingredients) {
            checks.add(check(required, index));
        }
        return new RecipeMatch(recipe, checks);
    }

    private static IngredientCheck check(RecipeIngredient required, Map<String, List<PantryItem>> index) {
        List<PantryItem> candidates = index.get(IngredientNormalizer.normalize(required.name));
        if (candidates == null || candidates.isEmpty()) {
            return new IngredientCheck(required, Status.MISSING, 0);
        }

        Unit requiredUnit = Unit.parse(required.unit);
        if (requiredUnit == null) {
            return new IngredientCheck(required, Status.UNIT_MISMATCH, 0);
        }

        // Add up every pantry entry for this ingredient whose unit can be converted to the recipe's unit,
        // e.g. "500 g rice" + "1 kg rice" = 1500 g available.
        double availableBase = 0;
        boolean anyCompatible = false;
        for (PantryItem item : candidates) {
            Unit itemUnit = Unit.parse(item.unit);
            if (requiredUnit.isCompatibleWith(itemUnit)) {
                availableBase += itemUnit.toBaseAmount(item.quantity);
                anyCompatible = true;
            }
        }
        if (!anyCompatible) {
            return new IngredientCheck(required, Status.UNIT_MISMATCH, 0);
        }

        double neededBase = requiredUnit.toBaseAmount(required.quantity);
        double availableInRequiredUnit = requiredUnit.fromBaseAmount(availableBase);
        Status status = availableBase + EPSILON >= neededBase ? Status.HAVE_ENOUGH : Status.NOT_ENOUGH;
        return new IngredientCheck(required, status, availableInRequiredUnit);
    }

    /** Groups usable pantry items by normalised ingredient name, preserving the given order. */
    private static Map<String, List<PantryItem>> indexPantry(List<PantryItem> pantry, long todayEpochDay,
                                                             boolean excludeExpired) {
        Map<String, List<PantryItem>> index = new HashMap<>();
        for (PantryItem item : pantry) {
            if (!isUsable(item, todayEpochDay, excludeExpired)) {
                continue;
            }
            String key = IngredientNormalizer.normalize(item.name);
            List<PantryItem> list = index.get(key);
            if (list == null) {
                list = new ArrayList<>();
                index.put(key, list);
            }
            list.add(item);
        }
        return index;
    }

    /**
     * Works out how the pantry changes after cooking a recipe. Stock is used oldest-first, so pass the
     * pantry sorted by expiry date. Returns modified copies of the affected items; a copy whose
     * quantity is 0 should be deleted. The original items are not changed.
     *
     * @throws IllegalStateException if the recipe cannot be made from the pantry
     */
    public static List<PantryItem> planConsumption(RecipeWithIngredients recipe, List<PantryItem> pantryOldestFirst,
                                                   long todayEpochDay, boolean excludeExpired) {
        // Work on copies so a recipe that lists the same ingredient twice deducts from updated amounts.
        List<PantryItem> working = new ArrayList<>();
        for (PantryItem item : pantryOldestFirst) {
            working.add(new PantryItem(item));
        }
        Map<String, List<PantryItem>> index = indexPantry(working, todayEpochDay, excludeExpired);
        if (!evaluate(recipe, index).canMake()) {
            throw new IllegalStateException("Recipe cannot be made from the current pantry");
        }

        Map<Long, PantryItem> changed = new LinkedHashMap<>();
        for (RecipeIngredient required : recipe.ingredients) {
            Unit requiredUnit = Unit.parse(required.unit);
            double stillNeededBase = requiredUnit.toBaseAmount(required.quantity);

            for (PantryItem item : index.get(IngredientNormalizer.normalize(required.name))) {
                if (stillNeededBase <= EPSILON) {
                    break;
                }
                Unit itemUnit = Unit.parse(item.unit);
                if (!requiredUnit.isCompatibleWith(itemUnit) || item.quantity <= EPSILON) {
                    continue;
                }
                double itemBase = itemUnit.toBaseAmount(item.quantity);
                double takeBase = Math.min(itemBase, stillNeededBase);
                item.quantity = round(itemUnit.fromBaseAmount(itemBase - takeBase));
                stillNeededBase -= takeBase;
                changed.put(item.id, item);
            }
        }
        return new ArrayList<>(changed.values());
    }

    /** Rounds to 3 decimal places so leftovers like 0.30000000004 are stored as 0.3. */
    private static double round(double value) {
        double rounded = Math.round(value * 1000.0) / 1000.0;
        return rounded < 0.001 ? 0 : rounded;
    }
}
