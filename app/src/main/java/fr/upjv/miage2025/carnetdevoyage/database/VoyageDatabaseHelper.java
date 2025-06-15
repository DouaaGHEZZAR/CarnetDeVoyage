package fr.upjv.miage2025.carnetdevoyage.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import fr.upjv.miage2025.carnetdevoyage.model.PointGps;
import fr.upjv.miage2025.carnetdevoyage.model.Voyage;

public class VoyageDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "voyages.db";
    public static final int DATABASE_VERSION = 3;

    public static final String TABLE_VOYAGE = "voyage";
    public static final String COL_ID = "id";
    public static final String COL_DESTINATION = "destination";
    public static final String COL_DATE_DEPART = "dateDepart";
    public static final String COL_DATE_RETOUR = "dateRetour";
    public static final String TABLE_POINT_GPS = "point_gps";
    public static final String COL_ID_GPS = "id";
    public static final String COL_LATITUDE = "latitude";
    public static final String COL_LONGITUDE = "longitude";
    public static final String COL_DATE_HEURE = "date_heure";
    public static final String COL_VOYAGE_ID = "voyage_id";
    public static final String COL_SUIVI_ACTIF = "suiviActif";
    public static final String COL_PERIODE = "periode";



    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_VOYAGE + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_DESTINATION + " TEXT, " +
                    COL_DATE_DEPART + " TEXT, " +
                    COL_DATE_RETOUR + " TEXT, " +
                    COL_SUIVI_ACTIF + " INTEGER DEFAULT 1, " +
                    COL_PERIODE + " INTEGER DEFAULT 30 )"
            ;

    private static final String CREATE_TABLE_POINT_GPS =
            "CREATE TABLE " + TABLE_POINT_GPS + " (" +
                    COL_ID_GPS + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_LATITUDE + " REAL, " +
                    COL_LONGITUDE + " REAL, " +
                    COL_DATE_HEURE + " TEXT, " +
                    COL_VOYAGE_ID + " INTEGER)";


    public VoyageDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        db.execSQL(CREATE_TABLE_POINT_GPS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS point_gps");
        db.execSQL("DROP TABLE IF EXISTS voyage");
        onCreate(db);
    }


    public void ajouterVoyage(String destination, String dateDepart, String dateRetour) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_DESTINATION, destination);
        values.put(COL_DATE_DEPART, dateDepart);
        values.put(COL_DATE_RETOUR, dateRetour);
        values.put(COL_SUIVI_ACTIF, 1);         // actif par défaut
        values.put(COL_PERIODE, 1800);          // 30 min en secondes

        db.insert(TABLE_VOYAGE, null, values);
        db.close();
    }

    public void ajouterPointGps(double lat, double lon, String dateHeure, int voyageId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_LATITUDE, lat);
        values.put(COL_LONGITUDE, lon);
        values.put(COL_DATE_HEURE, dateHeure);
        values.put(COL_VOYAGE_ID, voyageId);

        db.insert(TABLE_POINT_GPS, null, values);
        db.close();
    }


    public List<PointGps> getPointsGpsForVoyage(int voyageId) {
        List<PointGps> points = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT latitude, longitude, date_heure FROM " + TABLE_POINT_GPS +
                " WHERE voyage_id = ?", new String[]{String.valueOf(voyageId)});

        if (cursor.moveToFirst()) {
            do {
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LATITUDE));
                double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LONGITUDE));
                String dateHeure = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE_HEURE));

                points.add(new PointGps(lat, lon, dateHeure));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return points;
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

                int suivi = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SUIVI_ACTIF));
                int per = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PERIODE));

                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));

                Voyage voyage = new Voyage(destination, dateDepart, dateRetour);
                voyage.setId(id);
                voyage.setSuiviActif(suivi == 1);
                voyage.setPeriode(per);
                voyageList.add(voyage);


            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return voyageList;
    }

    public void deleteVoyage(int voyageId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Supprimer les points GPS liés
        db.delete(TABLE_POINT_GPS, COL_VOYAGE_ID + " = ?", new String[]{String.valueOf(voyageId)});
        // Supprimer le voyage lui-même
        db.delete(TABLE_VOYAGE, COL_ID + " = ?", new String[]{String.valueOf(voyageId)});
        db.close();
    }

    public void updateSuiviEtPeriode(int voyageId, boolean actif, int periodeSecondes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SUIVI_ACTIF, actif ? 1 : 0);
        values.put(COL_PERIODE, periodeSecondes);

        db.update(TABLE_VOYAGE, values, COL_ID + " = ?", new String[]{String.valueOf(voyageId)});
        db.close();
    }

    public int updateVoyageInfos(Voyage voyage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DESTINATION, voyage.getDestination());
        values.put(COL_DATE_DEPART, voyage.getDateDepart());
        values.put(COL_DATE_RETOUR, voyage.getDateRetour());

        int rows = db.update(TABLE_VOYAGE, values, COL_ID + " = ?", new String[]{String.valueOf(voyage.getId())});
        db.close();
        return rows;
    }





}
