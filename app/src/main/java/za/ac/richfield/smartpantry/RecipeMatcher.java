package za.ac.richfield.smartpantry;

import java.util.List;
import java.util.Locale;

/** Checks ALL required ingredients, with simple plural and unit handling. */
public class RecipeMatcher {
    public static boolean canMake(List<PantryItem> pantry, List<RecipeIngredient> requirements) {
        if (requirements.isEmpty()) return false;

        for (RecipeIngredient needed : requirements) {
            double available = 0;
            for (PantryItem item : pantry) {
                if (normalizeName(item.name).equals(normalizeName(needed.name))
                        && unitGroup(item.unit).equals(unitGroup(needed.unit))) {
                    available += toBase(item.quantity, item.unit);
                }
            }
            if (available + 0.000001 < toBase(needed.quantity, needed.unit)) {
                return false;
            }
        }
        return true;
    }

    public static String normalizeName(String name) {
        String value = name.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
        if (value.equals("tomatoes")) return "tomato";
        if (value.equals("potatoes")) return "potato";
        if (value.equals("loaves")) return "loaf";
        if (value.endsWith("ies") && value.length() > 3) {
            return value.substring(0, value.length() - 3) + "y";
        }
        if (value.endsWith("s") && !value.endsWith("ss") && !value.endsWith("us")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    public static String unitGroup(String unit) {
        switch (unit.toLowerCase(Locale.ROOT)) {
            case "piece": return "count";
            case "g":
            case "kg": return "weight";
            case "ml":
            case "l":
            case "tsp":
            case "tbsp": return "volume";
            default: return "unknown:" + unit;
        }
    }

    public static double toBase(double amount, String unit) {
        switch (unit.toLowerCase(Locale.ROOT)) {
            case "kg":
            case "l": return amount * 1000;
            case "tsp": return amount * 5;
            case "tbsp": return amount * 15;
            default: return amount;
        }
    }

    public static String niceNumber(double number) {
        if (number == Math.rint(number)) return String.format(Locale.ROOT, "%.0f", number);
        return String.format(Locale.ROOT, "%.2f", number).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
