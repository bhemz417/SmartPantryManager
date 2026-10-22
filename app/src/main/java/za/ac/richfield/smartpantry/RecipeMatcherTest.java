package za.ac.richfield.smartpantry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecipeMatcherTest {
    private static int passed = 0;

    private static void check(String name, boolean expected, List<PantryItem> pantry,
                              List<RecipeIngredient> needed) {
        if (RecipeMatcher.canMake(pantry, needed) != expected) {
            throw new AssertionError("FAILED: " + name);
        }
        passed++;
        System.out.println("PASS " + name);
    }

    private static PantryItem item(String name, double amount, String unit) {
        return new PantryItem(0, name, amount, unit, "");
    }

    private static RecipeIngredient need(String name, double amount, String unit) {
        return new RecipeIngredient(name, amount, unit);
    }

    public static void main(String[] args) {
        List<RecipeIngredient> sandwich = Arrays.asList(
                need("bread", 2, "piece"), need("tomato", 1, "piece"));
        check("Empty pantry", false, Collections.emptyList(), sandwich);
        check("Missing one ingredient", false,
                Collections.singletonList(item("bread", 2, "piece")), sandwich);
        check("Plural tomatoes matches tomato", true,
                Arrays.asList(item("bread", 2, "piece"), item("tomatoes", 1, "piece")), sandwich);
        check("Insufficient quantity", false,
                Arrays.asList(item("bread", 1, "piece"), item("tomatoes", 1, "piece")), sandwich);
        check("Enough quantity", true,
                Arrays.asList(item("bread", 3, "piece"), item("tomato", 2, "piece")), sandwich);
        check("Empty ingredient requirements never suggest", false,
                Collections.singletonList(item("bread", 3, "piece")), Collections.emptyList());
        check("Kilograms to grams", true,
                Collections.singletonList(item("butter", 0.1, "kg")),
                Collections.singletonList(need("butter", 80, "g")));
        check("Litres to millilitres", true,
                Collections.singletonList(item("milk", 0.3, "l")),
                Collections.singletonList(need("milk", 250, "ml")));
        check("Teaspoons to tablespoons", true,
                Collections.singletonList(item("oil", 3, "tsp")),
                Collections.singletonList(need("oil", 1, "tbsp")));
        check("Cannot compare pieces and grams", false,
                Collections.singletonList(item("tomato", 500, "g")),
                Collections.singletonList(need("tomato", 1, "piece")));
        check("Summing duplicate items", true,
                Arrays.asList(item("cheese", 0.02, "kg"), item("cheese", 25, "g")),
                Collections.singletonList(need("cheese", 40, "g")));
        check("Potatoes / potato", true,
                Collections.singletonList(item("potatoes", 2, "piece")),
                Collections.singletonList(need("potato", 2, "piece")));
        check("Tomato plural unit insufficient", false,
                Collections.singletonList(item("tomatoes", 0.5, "piece")),
                Collections.singletonList(need("tomato", 1, "piece")));
        check("No recipe with only one of three ingredients", false,
                Collections.singletonList(item("egg", 2, "piece")),
                Arrays.asList(need("egg", 2, "piece"), need("milk", 30, "ml"),
                        need("butter", 5, "g")));
        System.out.println("ALL " + passed + " MATCHER TESTS PASSED");
    }
}
