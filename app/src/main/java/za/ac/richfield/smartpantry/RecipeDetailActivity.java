package za.ac.richfield.smartpantry;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class RecipeDetailActivity extends Activity {
    private DatabaseHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        database = new DatabaseHelper(this);
        long recipeId = getIntent().getLongExtra("recipe_id", -1);
        Recipe recipe = database.getRecipe(recipeId);
        if (recipe == null) {
            finish();
            return;
        }
        ((TextView) findViewById(R.id.textRecipeTitle)).setText(recipe.name);
        ((TextView) findViewById(R.id.textMethod)).setText(recipe.steps);
        StringBuilder ingredients = new StringBuilder();
        for (RecipeIngredient item : database.getRecipeIngredients(recipeId)) {
            ingredients.append("• ")
                    .append(RecipeMatcher.niceNumber(item.quantity)).append(" ")
                    .append(item.unit).append(" ").append(item.name).append("\n");
        }
        ((TextView) findViewById(R.id.textIngredients)).setText(ingredients.toString().trim());
        findViewById(R.id.buttonBack).setOnClickListener(view -> finish());
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }
}
