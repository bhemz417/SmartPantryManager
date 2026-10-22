package za.ac.richfield.smartpantry;

import android.app.Activity;
import android.content.Intent;
import android.widget.Button;

/** Three-button navigation shared by the main screens. */
public class Navigation {
    public static void setup(Activity activity) {
        setButton(activity, R.id.navPantry, PantryListActivity.class);
        setButton(activity, R.id.navRecipes, SuggestedRecipesActivity.class);
        setButton(activity, R.id.navSettings, SettingsActivity.class);
    }

    private static void setButton(Activity activity, int id, Class<? extends Activity> destination) {
        Button button = activity.findViewById(id);
        if (activity.getClass() == destination) {
            button.setEnabled(false);
        } else {
            button.setOnClickListener(view -> {
                activity.startActivity(new Intent(activity, destination));
                activity.finish();
            });
        }
    }
}
