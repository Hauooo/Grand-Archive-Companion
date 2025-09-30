package my.edu.utar.grandarchivecompanion;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DamageLogActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DamageLogAdapter adapter;
    private ArrayList<String> damageLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_damage_log);

        recyclerView = findViewById(R.id.recycler_view_log);
        damageLogs = getIntent().getStringArrayListExtra("damageLogs");
        adapter = new DamageLogAdapter(damageLogs);
        recyclerView.setAdapter(adapter);
    }
}
