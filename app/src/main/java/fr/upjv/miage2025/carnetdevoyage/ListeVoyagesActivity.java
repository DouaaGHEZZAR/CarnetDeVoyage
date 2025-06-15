package fr.upjv.miage2025.carnetdevoyage;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

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
        voyages = dbHelper.getAllVoyages();

        adapter = new VoyageAdapter(voyages);
        recyclerView.setAdapter(adapter);

        adapter.setOnVoyageClickListener(voyage -> {
            Intent intent = new Intent(ListeVoyagesActivity.this, DetailVoyageActivity.class);
            intent.putExtra("voyageId", voyage.getId());
            startActivity(intent);
        });

        adapter.setOnVoyageDeleteListener(voyage -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmer la suppression")
                    .setMessage("Voulez-vous vraiment supprimer ce voyage ?")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        VoyageDatabaseHelper db = new VoyageDatabaseHelper(this);
                        db.deleteVoyage(voyage.getId());
                        // Recharger et rafraîchir l'affichage
                        voyages = dbHelper.getAllVoyages();
                        adapter.setVoyages(voyages);


                        // 2. Supprimer de Firestore //Walid
                        FirebaseFirestore.getInstance()
                                .collection("voyages")
                                .document(String.valueOf(voyage.getId()))
                                .delete();


                        // 3. Supprimer les points GPS de Realtime Database //Walid
                        FirebaseDatabase.getInstance()
                                .getReference("points_gps")
                                .child(String.valueOf(voyage.getId()))
                                .removeValue();

                        voyages.remove(voyage);
                        adapter.filter(""); // recharge la liste complète
                        Toast.makeText(this, "Voyage supprimé", Toast.LENGTH_SHORT).show();


                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        });


        TextView titre = findViewById(R.id.header_title);
        titre.setText("Vos voyages");
        HeaderUtils.setupHeaderMenu(this);

        EditText searchBar = findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }
}
