package fr.upjv.miage2025.carnetdevoyage.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import fr.upjv.miage2025.carnetdevoyage.model.Voyage;

public class VoyageDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "voyages.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_VOYAGE = "voyage";
    public static final String COL_ID = "id";
    public static final String COL_DESTINATION = "destination";
    public static final String COL_DATE_DEPART = "dateDepart";
    public static final String COL_DATE_RETOUR = "dateRetour";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_VOYAGE + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_DESTINATION + " TEXT, " +
                    COL_DATE_DEPART + " TEXT, " +
                    COL_DATE_RETOUR + " TEXT);";

    public VoyageDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOYAGE);
        onCreate(db);
    }

    public void ajouterVoyage(String destination, String dateDepart, String dateRetour) {
        SQLiteDatabase db = this.getWritableDatabase();

        String sql = "INSERT INTO " + TABLE_VOYAGE +
                " (" + COL_DESTINATION + ", " + COL_DATE_DEPART + ", " + COL_DATE_RETOUR + ") " +
                "VALUES (?, ?, ?)";

        db.execSQL(sql, new String[]{destination, dateDepart, dateRetour});
        db.close();
    }

    public List<Voyage> getAllVoyages() {
        List<Voyage> voyageList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_VOYAGE, null);

        if (cursor.moveToFirst()) {
            do {
                String destination = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESTINATION));
                String dateDepart = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE_DEPART));
                String dateRetour = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE_RETOUR));

                Voyage voyage = new Voyage(destination, dateDepart, dateRetour);
                voyageList.add(voyage);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return voyageList;
    }

}
