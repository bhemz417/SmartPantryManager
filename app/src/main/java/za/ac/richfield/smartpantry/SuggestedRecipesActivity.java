package za.ac.richfield.smartpantry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class SuggestedRecipesActivity extends Activity {
    private DatabaseHelper database;
    private ListView recipeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);
        database = new DatabaseHelper(this);
        recipeList = findViewById(R.id.listRecipes);
        recipeList.setEmptyView(findViewById(R.id.textNoRecipes));
        recipeList.setOnItemClickListener((parent, view, position, id) -> {
            Recipe recipe = (Recipe) parent.getItemAtPosition(position);
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra("recipe_id", recipe.id);
            startActivity(intent);
        });
        Navigation.setup(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<Recipe> matches = database.getSuggestedRecipes();
        recipeList.setAdapter(new RecipeAdapter(this, matches));
        ((TextView) findViewById(R.id.textRecipeCount)).setText(
                matches.size() + " recipe(s) ready to make using only your pantry items.");
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }
}
