package fr.upjv.miage2025.carnetdevoyage;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.upjv.miage2025.carnetdevoyage.adapter.VoyageAdapter;
import fr.upjv.miage2025.carnetdevoyage.database.VoyageDatabaseHelper;
import fr.upjv.miage2025.carnetdevoyage.model.Voyage;

public class ListeVoyagesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VoyageAdapter adapter;
    private List<Voyage> voyages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_voyages);

        recyclerView = findViewById(R.id.recycler_voyages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        VoyageDatabaseHelper dbHelper = new VoyageDatabaseHelper(this);
        voyages = dbHelper.getAllVoyages();  // Cette méthode, on va la créer juste après

        adapter = new VoyageAdapter(voyages);
        recyclerView.setAdapter(adapter);
    }
}
