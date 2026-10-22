package za.ac.richfield.smartpantry;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        Switch expirySwitch = findViewById(R.id.switchExpiry);
        expirySwitch.setChecked(settings.getBoolean("highlight_expiry", true));
        expirySwitch.setOnCheckedChangeListener((button, checked) ->
                settings.edit().putBoolean("highlight_expiry", checked).apply());
        Navigation.setup(this);
    }
}
