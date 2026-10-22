package za.ac.richfield.smartpantry;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AddEditIngredientActivity extends Activity {
    private DatabaseHelper database;
    private EditText nameInput;
    private EditText quantityInput;
    private EditText expiryInput;
    private Spinner unitInput;
    private long itemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);
        database = new DatabaseHelper(this);
        nameInput = findViewById(R.id.editName);
        quantityInput = findViewById(R.id.editQuantity);
        expiryInput = findViewById(R.id.editExpiry);
        unitInput = findViewById(R.id.spinnerUnit);

        itemId = getIntent().getLongExtra("item_id", -1);
        if (itemId != -1) {
            PantryItem item = database.getItem(itemId);
            if (item == null) {
                finish();
                return;
            }
            ((TextView) findViewById(R.id.textFormTitle)).setText("Edit Ingredient");
            nameInput.setText(item.name);
            quantityInput.setText(RecipeMatcher.niceNumber(item.quantity));
            expiryInput.setText(item.expiry);
            for (int i = 0; i < unitInput.getCount(); i++) {
                if (item.unit.equals(unitInput.getItemAtPosition(i).toString())) {
                    unitInput.setSelection(i);
                    break;
                }
            }
            findViewById(R.id.buttonDelete).setVisibility(View.VISIBLE);
            findViewById(R.id.buttonDelete).setOnClickListener(view -> confirmDelete());
        }
        findViewById(R.id.buttonSave).setOnClickListener(view -> saveItem());
        findViewById(R.id.buttonCancel).setOnClickListener(view -> finish());
    }

    private void saveItem() {
        String name = nameInput.getText().toString().trim();
        String quantityText = quantityInput.getText().toString().trim();
        String expiry = expiryInput.getText().toString().trim();
        String unit = unitInput.getSelectedItem().toString();

        if (name.isEmpty()) {
            nameInput.setError("Enter an ingredient name");
            nameInput.requestFocus();
            return;
        }
        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            quantityInput.setError("Enter a valid quantity");
            quantityInput.requestFocus();
            return;
        }
        if (quantity <= 0 || Double.isNaN(quantity) || Double.isInfinite(quantity)) {
            quantityInput.setError("Quantity must be greater than zero");
            quantityInput.requestFocus();
            return;
        }
        if (!expiry.isEmpty()) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
            format.setLenient(false);
            try {
                if (!expiry.matches("\\d{4}-\\d{2}-\\d{2}")) throw new ParseException("Date format", 0);
                format.parse(expiry);
            } catch (ParseException e) {
                expiryInput.setError("Use YYYY-MM-DD, e.g. 2026-12-31");
                expiryInput.requestFocus();
                return;
            }
        }

        if (itemId == -1) database.addItem(name, quantity, unit, expiry);
        else database.updateItem(itemId, name, quantity, unit, expiry);
        Toast.makeText(this, "Ingredient saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient?")
                .setMessage("This item will be removed from your pantry.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    database.deleteItem(itemId);
                    Toast.makeText(this, "Ingredient deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }
}
