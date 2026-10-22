package za.ac.richfield.smartpantry;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/** Custom ListView adapter displaying pantry data read from SQLite. */
public class PantryAdapter extends BaseAdapter {
    private final List<PantryItem> items;
    private final LayoutInflater inflater;
    private final boolean highlightExpiry;

    public PantryAdapter(Context context, List<PantryItem> items) {
        this.items = items;
        inflater = LayoutInflater.from(context);
        SharedPreferences preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        highlightExpiry = preferences.getBoolean("highlight_expiry", true);
    }

    @Override public int getCount() { return items.size(); }
    @Override public PantryItem getItem(int position) { return items.get(position); }
    @Override public long getItemId(int position) { return getItem(position).id; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView == null ? inflater.inflate(R.layout.row_pantry, parent, false) : convertView;
        PantryItem item = getItem(position);
        TextView name = row.findViewById(R.id.rowName);
        TextView quantity = row.findViewById(R.id.rowQuantity);
        TextView expiry = row.findViewById(R.id.rowExpiry);
        name.setText(item.name);
        quantity.setText(RecipeMatcher.niceNumber(item.quantity) + " " + item.unit + " · Tap to edit");
        expiry.setVisibility(View.GONE);
        if (!item.expiry.isEmpty()) {
            String status = "Expires: " + item.expiry;
            if (highlightExpiry) {
                int days = daysUntil(item.expiry);
                if (days < 0) status += " · Expired";
                else if (days <= 3) status += " · Expiring soon";
            }
            expiry.setText(status);
            expiry.setVisibility(View.VISIBLE);
        }
        return row;
    }

    private int daysUntil(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        format.setLenient(false);
        try {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            long difference = format.parse(date).getTime() - today.getTimeInMillis();
            return (int) TimeUnit.MILLISECONDS.toDays(difference);
        } catch (ParseException e) {
            return 9999;
        }
    }
}
