package fr.picsou.mangafinder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private EditText serverUrlEditText;
    private EditText serverPortEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        serverUrlEditText = findViewById(R.id.server_url);
        serverPortEditText = findViewById(R.id.server_port);
        saveButton = findViewById(R.id.save_button);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");

        toolbar.setNavigationOnClickListener(v -> finish());

        SharedPreferences preferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        serverUrlEditText.setText(preferences.getString("server_url", ""));
        serverPortEditText.setText(preferences.getString("server_port", ""));

        saveButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("server_url", serverUrlEditText.getText().toString());
            editor.putString("server_port", serverPortEditText.getText().toString());
            editor.apply();

            setResult(RESULT_OK);
            finish();
        });

    }
}