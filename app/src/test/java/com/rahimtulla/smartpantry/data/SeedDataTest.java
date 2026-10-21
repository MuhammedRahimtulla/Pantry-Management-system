package com.rahimtulla.smartpantry.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.rahimtulla.smartpantry.logic.Unit;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeedDataTest {

    @Test
    public void seedsFifteenUniqueRecipes() {
        List<RecipeWithIngredients> recipes = SeedData.recipes();
        assertEquals(15, recipes.size());
        Set<String> names = new HashSet<>();
        for (RecipeWithIngredients r : recipes) {
            assertTrue("Duplicate recipe " + r.recipe.name, names.add(r.recipe.name));
        }
    }

    @Test
    public void everyRecipeHasIngredientsStepsAndKnownUnits() {
        for (RecipeWithIngredients r : SeedData.recipes()) {
            assertFalse(r.recipe.name + " has no ingredients", r.ingredients.isEmpty());
            assertFalse(r.recipe.name + " has no steps", r.recipe.steps.isEmpty());
            for (RecipeIngredient i : r.ingredients) {
                assertNotNull(r.recipe.name + ": unknown unit " + i.unit, Unit.parse(i.unit));
                assertTrue(i.quantity > 0);
            }
        }
    }
}
