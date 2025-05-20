package fr.upjv.miage2025.carnetdevoyage;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class CreerVoyageActivity extends AppCompatActivity {

    private EditText etNomVoyage, etDateDebut, etDateFin;
    private Button btnEnregistrer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creer_voyage);

        etNomVoyage = findViewById(R.id.et_nom_voyage);
        etDateDebut = findViewById(R.id.et_date_debut);
        etDateFin = findViewById(R.id.et_date_fin);
        btnEnregistrer = findViewById(R.id.btn_enregistrer);

        // Choix des dates
        etDateDebut.setOnClickListener(v -> showDatePicker(etDateDebut));
        etDateFin.setOnClickListener(v -> showDatePicker(etDateFin));

        btnEnregistrer.setOnClickListener(v -> {
            String nom = etNomVoyage.getText().toString();
            String debut = etDateDebut.getText().toString();
            String fin = etDateFin.getText().toString();

            if (nom.isEmpty() || debut.isEmpty() || fin.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Voyage enregistré !", Toast.LENGTH_SHORT).show();
                // ici on enregistrera dans la base SQLite plus tard
            }
        });
    }

    private void showDatePicker(EditText targetEditText) {
        Calendar calendar = Calendar.getInstance();
        int annee = calendar.get(Calendar.YEAR);
        int mois = calendar.get(Calendar.MONTH);
        int jour = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String date = dayOfMonth + "/" + (month + 1) + "/" + year;
                    targetEditText.setText(date);
                }, annee, mois, jour);

        datePickerDialog.show();
    }
}
