package za.ac.richfield.smartpantry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class PantryListActivity extends Activity {
    private DatabaseHelper database;
    private ListView list;
    private TextView emptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);
        database = new DatabaseHelper(this);
        list = findViewById(R.id.listPantry);
        emptyMessage = findViewById(R.id.textEmpty);
        list.setEmptyView(emptyMessage);
        findViewById(R.id.buttonAdd).setOnClickListener(view ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));
        list.setOnItemClickListener((parent, view, position, id) -> {
            PantryItem item = (PantryItem) parent.getItemAtPosition(position);
            Intent intent = new Intent(this, AddEditIngredientActivity.class);
            intent.putExtra("item_id", item.id);
            startActivity(intent);
        });
        Navigation.setup(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<PantryItem> items = database.getPantryItems();
        list.setAdapter(new PantryAdapter(this, items));
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }
}
