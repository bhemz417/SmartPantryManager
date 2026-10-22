package za.ac.richfield.smartpantry;

public class PantryItem {
    public final long id;
    public final String name;
    public final double quantity;
    public final String unit;
    public final String expiry;

    public PantryItem(long id, String name, double quantity, String unit, String expiry) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiry = expiry;
    }
}
