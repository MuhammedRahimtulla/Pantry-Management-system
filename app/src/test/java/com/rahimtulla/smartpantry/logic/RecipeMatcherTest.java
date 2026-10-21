package com.rahimtulla.smartpantry.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.rahimtulla.smartpantry.data.PantryItem;
import com.rahimtulla.smartpantry.data.Recipe;
import com.rahimtulla.smartpantry.data.RecipeIngredient;
import com.rahimtulla.smartpantry.data.RecipeWithIngredients;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecipeMatcherTest {

    private static final long TODAY = 20_000; // an arbitrary epoch day

    private static long nextId = 1;

    private static PantryItem pantry(String name, double qty, String unit) {
        return pantry(name, qty, unit, null);
    }

    private static PantryItem pantry(String name, double qty, String unit, Long expiry) {
        PantryItem item = new PantryItem(name, qty, unit, expiry);
        item.id = nextId++;
        return item;
    }

    private static RecipeWithIngredients recipe(String name, RecipeIngredient... ingredients) {
        RecipeWithIngredients r = new RecipeWithIngredients();
        r.recipe = new Recipe();
        r.recipe.name = name;
        r.ingredients = Arrays.asList(ingredients);
        return r;
    }

    private static RecipeIngredient need(String name, double qty, String unit) {
        return new RecipeIngredient(name, qty, unit);
    }

    private static boolean canMake(RecipeWithIngredients r, PantryItem... items) {
        return RecipeMatcher.evaluate(r, Arrays.asList(items), TODAY, true).canMake();
    }

    private final RecipeWithIngredients omelette = recipe("Omelette",
            need("eggs", 3, "pcs"),
            need("milk", 50, "ml"),
            need("butter", 10, "g"),
            need("cheddar cheese", 40, "g"),
            need("salt", 1, "g"));

    @Test
    public void allIngredientsPresent_isSuggested() {
        assertTrue(canMake(omelette,
                pantry("Eggs", 6, "pcs"),
                pantry("Milk", 1, "l"),
                pantry("Butter", 250, "g"),
                pantry("Cheddar cheese", 200, "g"),
                pantry("Salt", 500, "g")));
    }

    @Test
    public void fourOfFiveIngredients_isNotSuggested() {
        // The brief's example: 5 required, 4 present -> must NOT be suggested.
        assertFalse(canMake(omelette,
                pantry("Eggs", 6, "pcs"),
                pantry("Milk", 1, "l"),
                pantry("Butter", 250, "g"),
                pantry("Salt", 500, "g")));
    }

    @Test
    public void notEnoughQuantity_isNotSuggested() {
        assertFalse(canMake(omelette,
                pantry("Eggs", 2, "pcs"), // need 3
                pantry("Milk", 1, "l"),
                pantry("Butter", 250, "g"),
                pantry("Cheddar cheese", 200, "g"),
                pantry("Salt", 500, "g")));
    }

    @Test
    public void exactQuantity_isEnough() {
        assertTrue(canMake(omelette,
                pantry("eggs", 3, "pcs"),
                pantry("milk", 50, "ml"),
                pantry("butter", 10, "g"),
                pantry("cheddar", 40, "g"),
                pantry("salt", 1, "g")));
    }

    @Test
    public void pluralAndCaseDifferences_stillMatch() {
        RecipeWithIngredients salad = recipe("Salad", need("tomato", 2, "pcs"), need("onion", 1, "pcs"));
        assertTrue(canMake(salad, pantry("TOMATOES", 4, "pcs"), pantry("Onions", 1, "pcs")));
    }

    @Test
    public void differentButCompatibleUnits_areConverted() {
        RecipeWithIngredients r = recipe("Rice", need("rice", 500, "g"), need("water", 1, "cup"));
        assertTrue(canMake(r, pantry("rice", 1, "kg"), pantry("water", 2, "l")));
        assertFalse(canMake(r, pantry("rice", 0.4, "kg"), pantry("water", 2, "l")));
    }

    @Test
    public void incompatibleUnits_areNotAssumedToMatch() {
        RecipeWithIngredients r = recipe("Eggs", need("egg", 2, "pcs"));
        RecipeMatcher.RecipeMatch match = RecipeMatcher.evaluate(r,
                Collections.singletonList(pantry("eggs", 500, "g")), TODAY, true);
        assertFalse(match.canMake());
        assertEquals(RecipeMatcher.Status.UNIT_MISMATCH, match.checks.get(0).status);
    }

    @Test
    public void multiplePantryEntriesAreAddedTogether() {
        RecipeWithIngredients r = recipe("Rice", need("rice", 600, "g"));
        assertTrue(canMake(r, pantry("rice", 300, "g"), pantry("Rice", 0.3, "kg")));
    }

    @Test
    public void expiredItemsAreIgnoredWhenSettingIsOn() {
        RecipeWithIngredients r = recipe("Toast", need("bread", 2, "pcs"));
        List<PantryItem> pantry = Collections.singletonList(pantry("bread", 4, "pcs", TODAY - 1));
        assertFalse(RecipeMatcher.evaluate(r, pantry, TODAY, true).canMake());
        assertTrue(RecipeMatcher.evaluate(r, pantry, TODAY, false).canMake());
        // Expiring today is still usable.
        assertTrue(canMake(r, pantry("bread", 4, "pcs", TODAY)));
    }

    @Test
    public void suggestReturnsOnlyFullyMakeableRecipes() {
        RecipeWithIngredients toast = recipe("Toast", need("bread", 2, "pcs"), need("butter", 10, "g"));
        RecipeWithIngredients eggs = recipe("Boiled eggs", need("egg", 2, "pcs"));
        List<PantryItem> pantry = Arrays.asList(pantry("bread", 2, "pcs"), pantry("eggs", 6, "pcs"));

        List<RecipeMatcher.RecipeMatch> suggestions =
                RecipeMatcher.suggest(Arrays.asList(toast, eggs), pantry, TODAY, true);

        assertEquals(1, suggestions.size());
        assertEquals("Boiled eggs", suggestions.get(0).recipe.recipe.name);
    }

    @Test
    public void emptyPantry_suggestsNothing() {
        assertTrue(RecipeMatcher.suggest(Collections.singletonList(omelette),
                new ArrayList<>(), TODAY, true).isEmpty());
    }

    @Test
    public void recipeWithNoIngredients_isNeverSuggested() {
        assertFalse(canMake(recipe("Nothing"), pantry("salt", 1, "g")));
    }

    @Test
    public void consumptionUsesOldestStockFirstAndConvertsUnits() {
        PantryItem oldRice = pantry("rice", 200, "g", TODAY + 1);
        PantryItem newRice = pantry("rice", 1, "kg", TODAY + 30);
        RecipeWithIngredients r = recipe("Rice", need("rice", 500, "g"));

        List<PantryItem> changed = RecipeMatcher.planConsumption(r, Arrays.asList(oldRice, newRice), TODAY, true);

        assertEquals(2, changed.size());
        assertEquals(oldRice.id, changed.get(0).id);
        assertEquals(0, changed.get(0).quantity, 1e-9);     // old bag used up -> delete
        assertEquals(0.7, changed.get(1).quantity, 1e-9);   // 1 kg - 300 g = 0.7 kg
        assertEquals(200, oldRice.quantity, 1e-9);          // originals untouched
    }

    @Test(expected = IllegalStateException.class)
    public void consumptionRefusesRecipesThatCannotBeMade() {
        RecipeMatcher.planConsumption(recipe("Rice", need("rice", 500, "g")),
                Collections.singletonList(pantry("rice", 100, "g")), TODAY, true);
    }
}
