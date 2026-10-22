package za.ac.richfield.smartpantry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class RecipeAdapter extends BaseAdapter {
    private final List<Recipe> recipes;
    private final LayoutInflater inflater;

    public RecipeAdapter(Context context, List<Recipe> recipes) {
        this.recipes = recipes;
        inflater = LayoutInflater.from(context);
    }

    @Override public int getCount() { return recipes.size(); }
    @Override public Recipe getItem(int position) { return recipes.get(position); }
    @Override public long getItemId(int position) { return getItem(position).id; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView == null ? inflater.inflate(R.layout.row_recipe, parent, false) : convertView;
        ((TextView) row.findViewById(R.id.rowRecipeTitle)).setText(getItem(position).name);
        return row;
    }
}
