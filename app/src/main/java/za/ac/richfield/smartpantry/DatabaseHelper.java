package za.ac.richfield.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/** SQLite storage for pantry items and the built-in recipe collection. */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, expiry TEXT)");
        db.execSQL("CREATE TABLE recipes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, steps TEXT NOT NULL)");
        db.execSQL("CREATE TABLE recipe_ingredients (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, recipe_id INTEGER NOT NULL, " +
                "name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, " +
                "FOREIGN KEY(recipe_id) REFERENCES recipes(id))");
        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // There is only database version 1. Add migrations here for later versions.
    }

    public long addItem(String name, double quantity, String unit, String expiry) {
        ContentValues values = valuesForItem(name, quantity, unit, expiry);
        return getWritableDatabase().insert("pantry_items", null, values);
    }

    public int updateItem(long id, String name, double quantity, String unit, String expiry) {
        return getWritableDatabase().update("pantry_items", valuesForItem(name, quantity, unit, expiry),
                "id=?", new String[]{String.valueOf(id)});
    }

    public int deleteItem(long id) {
        return getWritableDatabase().delete("pantry_items", "id=?",
                new String[]{String.valueOf(id)});
    }

    private ContentValues valuesForItem(String name, double quantity, String unit, String expiry) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("quantity", quantity);
        values.put("unit", unit);
        if (expiry.isEmpty()) values.putNull("expiry");
        else values.put("expiry", expiry);
        return values;
    }

    public PantryItem getItem(long id) {
        try (Cursor cursor = getReadableDatabase().query("pantry_items", null, "id=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (cursor.moveToFirst()) return readPantryItem(cursor);
        }
        return null;
    }

    public List<PantryItem> getPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query("pantry_items", null, null,
                null, null, null, "name COLLATE NOCASE ASC, id ASC")) {
            while (cursor.moveToNext()) items.add(readPantryItem(cursor));
        }
        return items;
    }

    private PantryItem readPantryItem(Cursor cursor) {
        int expiryIndex = cursor.getColumnIndexOrThrow("expiry");
        return new PantryItem(cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("quantity")),
                cursor.getString(cursor.getColumnIndexOrThrow("unit")),
                cursor.isNull(expiryIndex) ? "" : cursor.getString(expiryIndex));
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query("recipes", null, null, null,
                null, null, "name ASC")) {
            while (cursor.moveToNext()) {
                recipes.add(new Recipe(cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("steps"))));
            }
        }
        return recipes;
    }

    public Recipe getRecipe(long id) {
        try (Cursor cursor = getReadableDatabase().query("recipes", null, "id=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return new Recipe(cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("steps")));
            }
        }
        return null;
    }

    public List<RecipeIngredient> getRecipeIngredients(long recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query("recipe_ingredients", null,
                "recipe_id=?", new String[]{String.valueOf(recipeId)}, null, null, "id ASC")) {
            while (cursor.moveToNext()) {
                ingredients.add(new RecipeIngredient(
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("quantity")),
                        cursor.getString(cursor.getColumnIndexOrThrow("unit"))));
            }
        }
        return ingredients;
    }

    public List<Recipe> getSuggestedRecipes() {
        List<PantryItem> pantry = getPantryItems();
        List<Recipe> matches = new ArrayList<>();
        for (Recipe recipe : getAllRecipes()) {
            if (RecipeMatcher.canMake(pantry, getRecipeIngredients(recipe.id))) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    private void seedRecipe(SQLiteDatabase db, String title, String steps, String[][] ingredients) {
        ContentValues recipe = new ContentValues();
        recipe.put("name", title);
        recipe.put("steps", steps);
        long id = db.insertOrThrow("recipes", null, recipe);
        for (String[] ingredient : ingredients) {
            ContentValues requirement = new ContentValues();
            requirement.put("recipe_id", id);
            requirement.put("name", ingredient[0]);
            requirement.put("quantity", Double.parseDouble(ingredient[1]));
            requirement.put("unit", ingredient[2]);
            db.insertOrThrow("recipe_ingredients", null, requirement);
        }
    }

    private void seedRecipes(SQLiteDatabase db) {
        seedRecipe(db, "Tomato Sandwich", "1. Slice the tomato.\n2. Put the tomato between the bread slices.\n3. Serve.",
                new String[][]{{"bread", "2", "piece"}, {"tomato", "1", "piece"}});
        seedRecipe(db, "Cheese Toast", "1. Spread butter on the bread.\n2. Add cheese.\n3. Toast in a pan until the cheese melts.",
                new String[][]{{"bread", "2", "piece"}, {"cheese", "40", "g"}, {"butter", "10", "g"}});
        seedRecipe(db, "Egg Toast", "1. Cook the eggs in a pan with butter.\n2. Toast the bread.\n3. Serve the eggs on the toast.",
                new String[][]{{"bread", "2", "piece"}, {"egg", "2", "piece"}, {"butter", "5", "g"}});
        seedRecipe(db, "Banana Oats", "1. Warm the milk.\n2. Stir in the oats.\n3. Slice the banana on top.",
                new String[][]{{"oats", "50", "g"}, {"banana", "1", "piece"}, {"milk", "200", "ml"}});
        seedRecipe(db, "Simple Porridge", "1. Add oats and milk to a pot.\n2. Stir on low heat until thick.\n3. Serve warm.",
                new String[][]{{"oats", "50", "g"}, {"milk", "250", "ml"}});
        seedRecipe(db, "Scrambled Eggs", "1. Beat eggs with milk.\n2. Melt butter in a pan.\n3. Stir eggs gently until cooked.",
                new String[][]{{"egg", "2", "piece"}, {"milk", "30", "ml"}, {"butter", "5", "g"}});
        seedRecipe(db, "Tomato Eggs", "1. Cut the tomato.\n2. Cook it in butter.\n3. Add beaten eggs and stir until cooked.",
                new String[][]{{"egg", "2", "piece"}, {"tomato", "1", "piece"}, {"butter", "5", "g"}});
        seedRecipe(db, "Cheese Omelette", "1. Beat eggs and milk.\n2. Cook in a pan.\n3. Add cheese and fold over.",
                new String[][]{{"egg", "2", "piece"}, {"cheese", "30", "g"}, {"milk", "20", "ml"}});
        seedRecipe(db, "Plain Rice", "1. Put the rice and water in a pot.\n2. Simmer until the rice is soft.",
                new String[][]{{"rice", "100", "g"}, {"water", "200", "ml"}});
        seedRecipe(db, "Tomato Rice", "1. Cook rice in the water.\n2. Chop the tomatoes.\n3. Mix tomatoes into the hot rice.",
                new String[][]{{"rice", "100", "g"}, {"tomato", "2", "piece"}, {"water", "200", "ml"}});
        seedRecipe(db, "Mashed Potatoes", "1. Boil potatoes until soft.\n2. Mash with butter and milk.",
                new String[][]{{"potato", "2", "piece"}, {"milk", "100", "ml"}, {"butter", "15", "g"}});
        seedRecipe(db, "Fried Potatoes", "1. Slice potatoes thinly.\n2. Heat oil in a pan.\n3. Fry potato slices until cooked.",
                new String[][]{{"potato", "2", "piece"}, {"oil", "15", "ml"}});
        seedRecipe(db, "Carrot Salad", "1. Grate the carrots.\n2. Squeeze the lemon over them.\n3. Mix and serve.",
                new String[][]{{"carrot", "2", "piece"}, {"lemon", "1", "piece"}});
        seedRecipe(db, "Cucumber Tomato Salad", "1. Slice the cucumber and tomatoes.\n2. Mix and serve.",
                new String[][]{{"cucumber", "1", "piece"}, {"tomato", "2", "piece"}});
        seedRecipe(db, "Simple Pasta", "1. Boil water in a pot.\n2. Add pasta and cook until soft.\n3. Drain.",
                new String[][]{{"pasta", "100", "g"}, {"water", "500", "ml"}});
        seedRecipe(db, "Buttered Pasta", "1. Boil the pasta in water.\n2. Drain.\n3. Mix in butter while warm.",
                new String[][]{{"pasta", "100", "g"}, {"water", "500", "ml"}, {"butter", "15", "g"}});
        seedRecipe(db, "Tomato Pasta", "1. Boil pasta in water and drain.\n2. Chop tomatoes.\n3. Warm tomatoes in oil, then mix with pasta.",
                new String[][]{{"pasta", "100", "g"}, {"tomato", "2", "piece"}, {"water", "500", "ml"}, {"oil", "10", "ml"}});
        seedRecipe(db, "Peanut Butter Sandwich", "1. Spread peanut butter on one bread slice.\n2. Cover with the other slice.",
                new String[][]{{"bread", "2", "piece"}, {"peanut butter", "30", "g"}});
        seedRecipe(db, "Banana Sandwich", "1. Slice the banana.\n2. Place it between the bread slices.",
                new String[][]{{"bread", "2", "piece"}, {"banana", "1", "piece"}});
        seedRecipe(db, "Fruit Bowl", "1. Peel the banana and orange.\n2. Chop all fruit.\n3. Mix in a bowl.",
                new String[][]{{"apple", "1", "piece"}, {"banana", "1", "piece"}, {"orange", "1", "piece"}});
    }
}
