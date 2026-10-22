package za.ac.richfield.smartpantry;

public class Recipe {
    public final long id;
    public final String name;
    public final String steps;

    public Recipe(long id, String name, String steps) {
        this.id = id;
        this.name = name;
        this.steps = steps;
    }
}
