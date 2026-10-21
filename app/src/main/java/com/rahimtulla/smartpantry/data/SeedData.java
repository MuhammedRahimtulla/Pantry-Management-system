package com.rahimtulla.smartpantry.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The built-in recipe collection, written into the database the first time it is created.
 *
 * Units follow one convention so recipes and pantry entries compare cleanly:
 * solids in grams (g), liquids in ml/tbsp/tsp, and countable things in pieces (pcs).
 * Water is assumed to be available and is mentioned only in the steps.
 */
public final class SeedData {

    private SeedData() {
    }

    public static List<RecipeWithIngredients> recipes() {
        List<RecipeWithIngredients> list = new ArrayList<>();

        list.add(recipe("Scrambled Eggs", "Soft, creamy eggs ready in minutes.", 10,
                steps("Whisk the eggs, milk and salt together in a bowl.",
                        "Melt the butter in a pan over low heat.",
                        "Add the egg mixture and stir gently until just set.",
                        "Serve immediately."),
                ing("eggs", 3, "pcs"), ing("milk", 30, "ml"), ing("butter", 10, "g"), ing("salt", 1, "g")));

        list.add(recipe("French Toast", "A great way to use up bread that is going stale.", 15,
                steps("Whisk the eggs, milk and sugar in a shallow dish.",
                        "Soak each slice of bread in the mixture for a few seconds per side.",
                        "Fry the slices in butter until golden on both sides.",
                        "Serve warm."),
                ing("bread", 4, "pcs"), ing("eggs", 2, "pcs"), ing("milk", 100, "ml"),
                ing("sugar", 10, "g"), ing("butter", 20, "g")));

        list.add(recipe("Cheese and Tomato Toastie", "A quick lunch from bread, cheese and a leftover tomato.", 10,
                steps("Butter one side of each slice of bread.",
                        "Place cheese and sliced tomato between the unbuttered sides.",
                        "Toast in a pan or sandwich press until golden and the cheese melts."),
                ing("bread", 2, "pcs"), ing("cheddar cheese", 50, "g"), ing("tomato", 1, "pcs"),
                ing("butter", 10, "g")));

        list.add(recipe("Tomato and Onion Smoor", "Classic South African tomato-and-onion relish for pap or braai.", 25,
                steps("Heat the oil and fry the chopped onion until soft.",
                        "Add the chopped tomatoes, sugar and salt.",
                        "Simmer for 15 minutes, stirring now and then, until thick."),
                ing("tomatoes", 3, "pcs"), ing("onion", 1, "pcs"), ing("cooking oil", 1, "tbsp"),
                ing("sugar", 5, "g"), ing("salt", 2, "g")));

        list.add(recipe("Stywe Pap", "Firm maize porridge, a staple side dish.", 30,
                steps("Bring 500 ml of salted water to the boil.",
                        "Pour in the maize meal in one go without stirring; cover and simmer for 5 minutes.",
                        "Stir well with a fork, cover and steam on low heat for 20 minutes.",
                        "Stir in the butter and serve."),
                ing("maize meal", 250, "g"), ing("salt", 2, "g"), ing("butter", 10, "g")));

        list.add(recipe("Chakalaka", "Spicy vegetable relish that uses up carrots and peppers.", 30,
                steps("Fry the chopped onion in oil until soft, then add the curry powder.",
                        "Add the grated carrots and diced pepper; cook for 5 minutes.",
                        "Stir in the baked beans and simmer for 10 minutes."),
                ing("onion", 1, "pcs"), ing("bell pepper", 1, "pcs"), ing("carrots", 2, "pcs"),
                ing("baked beans", 410, "g"), ing("cooking oil", 2, "tbsp"), ing("curry powder", 10, "g")));

        list.add(recipe("Spaghetti Bolognese", "Hearty mince and tomato sauce over spaghetti.", 40,
                steps("Fry the onion and garlic in oil until soft.",
                        "Add the mince and brown it, breaking it up as it cooks.",
                        "Add the chopped tomatoes and simmer for 20 minutes.",
                        "Meanwhile cook the spaghetti in salted boiling water, drain and serve with the sauce."),
                ing("spaghetti", 250, "g"), ing("beef mince", 500, "g"), ing("tomatoes", 4, "pcs"),
                ing("onion", 1, "pcs"), ing("garlic", 2, "pcs"), ing("cooking oil", 1, "tbsp")));

        list.add(recipe("Vegetable Stir-Fry with Rice", "Clears out the vegetable drawer in one pan.", 30,
                steps("Cook the rice in boiling water until tender.",
                        "Heat the oil and stir-fry the sliced carrots, pepper and cabbage for 5 minutes.",
                        "Add the soy sauce, toss well and serve over the rice."),
                ing("rice", 200, "g"), ing("carrots", 2, "pcs"), ing("bell pepper", 1, "pcs"),
                ing("cabbage", 200, "g"), ing("soy sauce", 30, "ml"), ing("cooking oil", 2, "tbsp")));

        list.add(recipe("Egg Fried Rice", "Best made with leftover cooked rice.", 20,
                steps("Cook the rice and let it cool (or use leftover rice).",
                        "Scramble the eggs in a little oil and set aside.",
                        "Fry the diced carrot and spring onions, add the rice and soy sauce.",
                        "Stir the egg back in and serve."),
                ing("rice", 200, "g"), ing("eggs", 2, "pcs"), ing("spring onions", 2, "pcs"),
                ing("carrot", 1, "pcs"), ing("soy sauce", 2, "tbsp"), ing("cooking oil", 1, "tbsp")));

        list.add(recipe("Pancakes", "Thin South African-style pancakes.", 30,
                steps("Whisk the flour, eggs and milk into a smooth, thin batter.",
                        "Melt a little butter in a pan and pour in a thin layer of batter.",
                        "Cook until the edges lift, flip and cook the other side.",
                        "Sprinkle with sugar and roll up."),
                ing("flour", 125, "g"), ing("eggs", 2, "pcs"), ing("milk", 300, "ml"),
                ing("butter", 20, "g"), ing("sugar", 15, "g")));

        list.add(recipe("Garlic Roast Potatoes", "Crispy potatoes that use up a bag before it sprouts.", 50,
                steps("Preheat the oven to 200 °C.",
                        "Cut the potatoes into chunks and toss with oil, crushed garlic and salt.",
                        "Roast for 40 minutes, turning halfway, until golden."),
                ing("potatoes", 6, "pcs"), ing("cooking oil", 3, "tbsp"), ing("garlic", 2, "pcs"),
                ing("salt", 2, "g")));

        list.add(recipe("Creamy Tuna Pasta", "Store-cupboard pasta with tinned tuna.", 20,
                steps("Cook the pasta in salted boiling water and drain.",
                        "Mix the drained tuna, mayonnaise and finely chopped onion.",
                        "Stir the tuna mixture through the warm pasta."),
                ing("pasta", 250, "g"), ing("tuna", 170, "g"), ing("mayonnaise", 60, "g"),
                ing("onion", 1, "pcs")));

        list.add(recipe("Chicken Curry", "Mild home-style curry.", 45,
                steps("Fry the onion and garlic in oil until golden.",
                        "Add the curry powder and fry for 1 minute.",
                        "Add the chicken pieces and brown them.",
                        "Add the chopped tomatoes and simmer for 25 minutes until the chicken is cooked."),
                ing("chicken", 500, "g"), ing("onion", 1, "pcs"), ing("tomatoes", 2, "pcs"),
                ing("curry powder", 15, "g"), ing("garlic", 2, "pcs"), ing("cooking oil", 2, "tbsp")));

        list.add(recipe("Banana Bread", "The classic answer to over-ripe bananas.", 70,
                steps("Preheat the oven to 180 °C and grease a loaf tin.",
                        "Mash the bananas and mix in the melted butter, sugar and eggs.",
                        "Fold in the flour and baking powder.",
                        "Bake for 55-60 minutes until a skewer comes out clean."),
                ing("bananas", 3, "pcs"), ing("flour", 250, "g"), ing("sugar", 100, "g"),
                ing("eggs", 2, "pcs"), ing("butter", 100, "g"), ing("baking powder", 5, "g")));

        list.add(recipe("Lentil and Carrot Soup", "Cheap, filling soup from pantry staples.", 40,
                steps("Fry the onion in oil until soft.",
                        "Add the diced carrots, rinsed lentils, stock cube and 1 litre of water.",
                        "Simmer for 30 minutes until the lentils are soft.",
                        "Blend partly for a thicker soup."),
                ing("lentils", 200, "g"), ing("carrots", 2, "pcs"), ing("onion", 1, "pcs"),
                ing("stock cube", 1, "pcs"), ing("cooking oil", 1, "tbsp")));

        return list;
    }

    /**
     * Inserts every seed recipe using raw SQL inserts. Called from Room's onCreate callback, which runs
     * synchronously inside the database-creation transaction, so the recipes exist before the first query.
     */
    static void insertAll(SupportSQLiteDatabase db) {
        for (RecipeWithIngredients r : recipes()) {
            ContentValues recipeValues = new ContentValues();
            recipeValues.put("name", r.recipe.name);
            recipeValues.put("description", r.recipe.description);
            recipeValues.put("steps", r.recipe.steps);
            recipeValues.put("prep_minutes", r.recipe.prepMinutes);
            long recipeId = db.insert("recipes", SQLiteDatabase.CONFLICT_ABORT, recipeValues);

            for (RecipeIngredient i : r.ingredients) {
                ContentValues ingredientValues = new ContentValues();
                ingredientValues.put("recipe_id", recipeId);
                ingredientValues.put("name", i.name);
                ingredientValues.put("quantity", i.quantity);
                ingredientValues.put("unit", i.unit);
                db.insert("recipe_ingredients", SQLiteDatabase.CONFLICT_ABORT, ingredientValues);
            }
        }
    }

    private static RecipeWithIngredients recipe(String name, String description, int prepMinutes,
                                                String steps, RecipeIngredient... ingredients) {
        RecipeWithIngredients r = new RecipeWithIngredients();
        r.recipe = new Recipe();
        r.recipe.name = name;
        r.recipe.description = description;
        r.recipe.prepMinutes = prepMinutes;
        r.recipe.steps = steps;
        r.ingredients = Arrays.asList(ingredients);
        return r;
    }

    private static String steps(String... steps) {
        return String.join("\n", steps);
    }

    private static RecipeIngredient ing(String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }
}
