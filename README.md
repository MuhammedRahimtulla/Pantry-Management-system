# Smart Pantry Manager

A Java Android app that helps reduce food waste. You record the ingredients you actually have at home (your *pantry*), and the app suggests recipes you can cook **using only those ingredients**. A recipe is suggested only when every ingredient it needs is in your pantry in at least the required amount, so no shopping trip is needed.

Built for **Mobile App Development 700** (Richfield), by Muhammed Rahimtulla.

## Features

- **Pantry management (full CRUD):** add, view, edit and delete ingredients with a name, quantity, unit and optional expiry date.
  - Swipe a row to delete it, with Undo.
  - Input validation on every field: required name and quantity, letters-only names, quantity > 0, no past expiry date for new items.
  - Ingredient-name autocomplete taken from the recipe collection, plus a warning when the chosen unit can't be compared with the unit the recipes use.
- **Pantry list:** a RecyclerView with a custom `ListAdapter`, bound live to the database through Room + LiveData. Items that are about to expire, or have expired, are highlighted, and a banner reminds you to cook them first.
- **Recipe collection:** 15 recipes seeded into the database on first run.
- **Suggested Recipes:** shows only recipes that pass the strict-matching rule. If nothing matches, it shows the message *"No recipes match your pantry yet"* with a shortcut to add ingredients.
- **Recipe Detail:** the full ingredient list with a ✓ / ✗ against your pantry for each ingredient ("You have 1 kg", "Only 100 g in your pantry", "Not in your pantry"), followed by the method.
- **"I cooked this":** subtracts the recipe's ingredients from the pantry in one database transaction, using the stock that expires soonest first. Items that run out are deleted.
- **Settings:** highlight expiring items on/off, the "expiring soon" window (1/3/5/7 days), ignoring expired items when matching, the default unit for new items, and clearing the pantry.
- **No maps, GPS, location or network permissions.** The app works entirely on the device.

## The strict-matching rule

Implemented in [`RecipeMatcher.java`](app/src/main/java/com/rahimtulla/smartpantry/logic/RecipeMatcher.java):

1. **Normalise names** ([`IngredientNormalizer`](app/src/main/java/com/rahimtulla/smartpantry/logic/IngredientNormalizer.java)) so that trivial differences still match:
   - case, spaces and punctuation are ignored (`"  TOMATOES "` → `tomato`);
   - plurals are reduced to the singular (`tomatoes`/`tomato`, `berries`/`berry`, `peaches`/`peach`);
   - descriptive words are dropped (`"fresh ripe tomatoes"` → `tomato`, `"large free-range eggs"` → `egg`);
   - synonyms are mapped to one name (`ground beef` → `beef mince`, `mealie meal` → `maize meal`, `cake flour` → `flour`, `sunflower oil` → `cooking oil`).
2. **Convert units** ([`Unit`](app/src/main/java/com/rahimtulla/smartpantry/logic/Unit.java)) to a base unit before comparing: mass to grams, volume to millilitres (tsp = 5 ml, tbsp = 15 ml, cup = 250 ml), count to pieces. For example, 1 kg of rice satisfies a recipe needing 500 g. Units of different kinds (e.g. pieces vs grams) are never assumed to match.
3. **Add up duplicates:** "300 g rice" + "0.3 kg rice" counts as 600 g available.
4. **Exclude expired stock** when the setting is on (the default).
5. A recipe is suggested **only if every ingredient is present in at least the required quantity**. If even one ingredient is missing or short, the recipe is excluded (for example, 4 out of 5 ingredients means it is not suggested).

This logic is covered by JUnit tests in [`app/src/test`](app/src/test/java/com/rahimtulla/smartpantry). They include the brief's 4-out-of-5 case, tomato vs tomatoes, unit conversion, insufficient quantity, expired items, incompatible units, and the oldest-first "cooked" deduction.

## Database choice: SQLite with Room

I chose **SQLite through the Room persistence library** because:

- **It suits the problem.** A pantry belongs to one person and one device, so there is no need to sync or share data between users, and therefore no need for a cloud database.
- **It works offline and needs no setup.** Users often check their pantry in the kitchen with poor signal. SQLite needs no network, account, API keys or hosting, so the app works the first time it is installed, and the marker can run it with no configuration.
- **The data persists.** Data is stored in a file on the device (`smart_pantry.db`), so it survives closing and reopening the app.
- **Room adds safety on top of SQLite.** SQL queries are checked at compile time, entities map directly to Java classes, `LiveData` queries refresh the UI automatically, foreign keys cascade-delete a recipe's ingredients, and `runInTransaction` makes the "cooked" update all-or-nothing.
- It matches the persistent-data approach covered in the module.

### Data model

```
recipes (id PK, name, description, steps, prep_minutes)
   1
   │
   ∞
recipe_ingredients (id PK, recipe_id FK → recipes.id ON DELETE CASCADE, name, quantity, unit)

pantry_items (id PK, name, quantity, unit, expiry_epoch_day NULL)
```

`pantry_items` and `recipe_ingredients` are linked *logically* by normalised ingredient name, not by a foreign key. This is deliberate: the user can type any ingredient, including ones no recipe uses.

## Setup and run instructions

**Requirements:** a recent Android Studio that supports Android Gradle Plugin 9.4 (developed with Android Studio 2026.2 and its bundled JDK), Android SDK Platform 37, and an emulator or Android phone running Android 8.0 (API 26) or newer.

1. Clone the repository:
   ```
   git clone <this-repo-url>
   ```
2. In Android Studio choose **File → Open** and select the cloned folder. Wait for the Gradle sync to finish. The wrapper downloads Gradle 9.8.0 automatically.
3. If Android Studio asks for SDK Platform 37, install it from **Tools → SDK Manager**.
4. Choose an emulator (Device Manager), or plug in a phone with USB debugging enabled, then press **Run ▶**.

Command line alternative:

```
gradlew assembleDebug          # builds app/build/outputs/apk/debug/app-debug.apk
gradlew installDebug           # installs on the connected device/emulator
gradlew testDebugUnitTest      # runs the matching-logic unit tests
```

## Trying out the strict matching

| Recipe | Ingredients |
|---|---|
| Cheese and Tomato Toastie | 2 pcs bread, 50 g cheddar cheese, 1 pcs tomato, 10 g butter |
| Scrambled Eggs | 3 pcs eggs, 30 ml milk, 10 g butter, 1 g salt |
| Stywe Pap | 250 g maize meal, 2 g salt, 10 g butter |
| French Toast | 4 pcs bread, 2 pcs eggs, 100 ml milk, 10 g sugar, 20 g butter |
| Tomato and Onion Smoor | 3 pcs tomatoes, 1 pcs onion, 1 tbsp cooking oil, 5 g sugar, 2 g salt |
| Chakalaka | 1 pcs onion, 1 pcs bell pepper, 2 pcs carrots, 410 g baked beans, 2 tbsp cooking oil, 10 g curry powder |
| Spaghetti Bolognese | 250 g spaghetti, 500 g beef mince, 4 pcs tomatoes, 1 pcs onion, 2 pcs garlic, 1 tbsp cooking oil |
| Vegetable Stir-Fry with Rice | 200 g rice, 2 pcs carrots, 1 pcs bell pepper, 200 g cabbage, 30 ml soy sauce, 2 tbsp cooking oil |
| Egg Fried Rice | 200 g rice, 2 pcs eggs, 2 pcs spring onions, 1 pcs carrot, 2 tbsp soy sauce, 1 tbsp cooking oil |
| Pancakes | 125 g flour, 2 pcs eggs, 300 ml milk, 20 g butter, 15 g sugar |
| Garlic Roast Potatoes | 6 pcs potatoes, 3 tbsp cooking oil, 2 pcs garlic, 2 g salt |
| Creamy Tuna Pasta | 250 g pasta, 170 g tuna, 60 g mayonnaise, 1 pcs onion |
| Chicken Curry | 500 g chicken, 1 pcs onion, 2 pcs tomatoes, 15 g curry powder, 2 pcs garlic, 2 tbsp cooking oil |
| Banana Bread | 3 pcs bananas, 250 g flour, 100 g sugar, 2 pcs eggs, 100 g butter, 5 g baking powder |
| Lentil and Carrot Soup | 200 g lentils, 2 pcs carrots, 1 pcs onion, 1 pcs stock cube, 1 tbsp cooking oil |

Quick test: add *Bread 4 pcs*, *Cheddar 200 g*, *Tomatoes 2 pcs* and *Butter 250 g*. The **Cheese and Tomato Toastie** appears under Suggested. Delete the tomatoes and it disappears.

## Project structure

```
app/src/main/java/com/rahimtulla/smartpantry/
├── data/        Room entities, DAOs, AppDatabase (singleton + seed callback), SeedData
├── logic/       Unit, IngredientNormalizer, RecipeMatcher (pure Java, unit-tested)
├── ui/          MainActivity (bottom navigation host)
│   ├── pantry/    PantryFragment, PantryAdapter, AddEditIngredientActivity
│   ├── recipes/   RecipeListFragment (Suggested / All), RecipeAdapter, RecipeDetailActivity
│   └── settings/  SettingsFragment (AndroidX Preference)
└── util/        Prefs, Formatters, InsetsHelper
```
