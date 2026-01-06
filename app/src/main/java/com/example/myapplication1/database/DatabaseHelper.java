package com.example.myapplication1.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.myapplication1.models.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "garden.db";
    private static final int DATABASE_VERSION = 4; // Updated version

    // Sensor readings table
    private static final String TABLE_SENSORS = "sensor_readings";
    private static final String COL_ID = "id";
    private static final String COL_PARCEL_ID = "parcel_id";
    private static final String COL_HUMIDITY = "humidity";
    private static final String COL_TEMPERATURE = "temperature";
    private static final String COL_LIGHT = "light_level";
    private static final String COL_PH = "ph";
    private static final String COL_TIMESTAMP = "timestamp";

    // Parcels table
    private static final String TABLE_PARCELS = "parcels";
    private static final String COL_PARCEL_NUMBER = "parcel_number";
    private static final String COL_OWNER_EMAIL = "owner_email";
    private static final String COL_PLANT_TYPE = "plant_type";
    private static final String COL_PLANTING_DATE = "planting_date";
    private static final String COL_HARVEST_DATE = "harvest_date";
    private static final String COL_IS_OCCUPIED = "is_occupied";

    // Photos table
    private static final String TABLE_PHOTOS = "photos";
    private static final String COL_PHOTO_ID = "id";
    private static final String COL_PHOTO_PARCEL_ID = "parcel_id";
    private static final String COL_USER_EMAIL = "user_email";
    private static final String COL_FILE_PATH = "file_path";
    private static final String COL_NOTES = "notes";
    private static final String COL_PHOTO_TIMESTAMP = "timestamp";

    // Plants library table
    private static final String TABLE_PLANTS = "plants";
    private static final String COL_PLANT_NAME = "name";
    private static final String COL_SCIENTIFIC_NAME = "scientific_name";
    private static final String COL_CATEGORY = "category";
    private static final String COL_PLANTING_PERIOD = "planting_period";
    private static final String COL_HARVEST_PERIOD = "harvest_period";
    private static final String COL_CARE_INSTRUCTIONS = "care_instructions";
    private static final String COL_WATERING_FREQ = "watering_frequency";
    private static final String COL_SUNLIGHT_REQ = "sunlight_requirement";
    private static final String COL_SOIL_TYPE = "soil_type";
    private static final String COL_COMPATIBILITY = "compatibility";
    private static final String COL_GROWTH_DURATION = "growth_duration_days";

    // Journal entries table
    private static final String TABLE_JOURNAL = "journal_entries";
    private static final String COL_JOURNAL_ID = "id";
    private static final String COL_ENTRY_TYPE = "entry_type";
    private static final String COL_WATER_AMOUNT = "water_amount";

    // Notification history table
    private static final String TABLE_NOTIFICATIONS = "notification_history";
    private static final String COL_NOTIF_ID = "id";
    private static final String COL_ALERT_TYPE = "alert_type";
    private static final String COL_MESSAGE = "message";
    private static final String COL_SEVERITY = "severity";
    private static final String COL_IS_READ = "is_read";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create sensor readings table
        String createSensorsTable = "CREATE TABLE " + TABLE_SENSORS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_PARCEL_ID + " TEXT NOT NULL, "
                + COL_HUMIDITY + " INTEGER NOT NULL, "
                + COL_TEMPERATURE + " REAL NOT NULL, "
                + COL_LIGHT + " INTEGER NOT NULL, "
                + COL_PH + " REAL NOT NULL, "
                + COL_TIMESTAMP + " INTEGER NOT NULL)";
        db.execSQL(createSensorsTable);

        // Create parcels table
        String createParcelsTable = "CREATE TABLE " + TABLE_PARCELS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_PARCEL_NUMBER + " TEXT UNIQUE NOT NULL, "
                + COL_OWNER_EMAIL + " TEXT, "
                + COL_PLANT_TYPE + " TEXT, "
                + COL_PLANTING_DATE + " TEXT, "
                + COL_HARVEST_DATE + " TEXT, "
                + COL_IS_OCCUPIED + " INTEGER DEFAULT 0)";
        db.execSQL(createParcelsTable);

        // Create photos table
        String createPhotosTable = "CREATE TABLE " + TABLE_PHOTOS + " ("
                + COL_PHOTO_ID + " TEXT PRIMARY KEY, "
                + COL_PHOTO_PARCEL_ID + " TEXT NOT NULL, "
                + COL_USER_EMAIL + " TEXT NOT NULL, "
                + COL_FILE_PATH + " TEXT NOT NULL, "
                + COL_NOTES + " TEXT, "
                + COL_PHOTO_TIMESTAMP + " INTEGER NOT NULL)";
        db.execSQL(createPhotosTable);

        // Create plants library table
        String createPlantsTable = "CREATE TABLE " + TABLE_PLANTS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_PLANT_NAME + " TEXT NOT NULL, "
                + COL_SCIENTIFIC_NAME + " TEXT, "
                + COL_CATEGORY + " TEXT, "
                + COL_PLANTING_PERIOD + " TEXT, "
                + COL_HARVEST_PERIOD + " TEXT, "
                + COL_CARE_INSTRUCTIONS + " TEXT, "
                + COL_WATERING_FREQ + " TEXT, "
                + COL_SUNLIGHT_REQ + " TEXT, "
                + COL_SOIL_TYPE + " TEXT, "
                + COL_COMPATIBILITY + " TEXT, "
                + COL_GROWTH_DURATION + " INTEGER)";
        db.execSQL(createPlantsTable);

        // Create journal entries table
        String createJournalTable = "CREATE TABLE " + TABLE_JOURNAL + " ("
                + COL_JOURNAL_ID + " TEXT PRIMARY KEY, "
                + COL_PARCEL_ID + " TEXT NOT NULL, "
                + COL_USER_EMAIL + " TEXT NOT NULL, "
                + COL_ENTRY_TYPE + " TEXT NOT NULL, "
                + COL_NOTES + " TEXT, "
                + COL_WATER_AMOUNT + " REAL DEFAULT 0, "
                + COL_TIMESTAMP + " INTEGER NOT NULL)";
        db.execSQL(createJournalTable);

        // Create notification history table
        String createNotificationsTable = "CREATE TABLE " + TABLE_NOTIFICATIONS + " ("
                + COL_NOTIF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_PARCEL_ID + " TEXT NOT NULL, "
                + COL_ALERT_TYPE + " TEXT NOT NULL, "
                + COL_MESSAGE + " TEXT NOT NULL, "
                + COL_SEVERITY + " TEXT NOT NULL, "
                + COL_IS_READ + " INTEGER DEFAULT 0, "
                + COL_TIMESTAMP + " INTEGER NOT NULL)";
        db.execSQL(createNotificationsTable);

        insertDefaultParcels(db);
        insertDefaultPlants(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String createPhotosTable = "CREATE TABLE IF NOT EXISTS " + TABLE_PHOTOS + " ("
                    + COL_PHOTO_ID + " TEXT PRIMARY KEY, "
                    + COL_PHOTO_PARCEL_ID + " TEXT NOT NULL, "
                    + COL_USER_EMAIL + " TEXT NOT NULL, "
                    + COL_FILE_PATH + " TEXT NOT NULL, "
                    + COL_NOTES + " TEXT, "
                    + COL_PHOTO_TIMESTAMP + " INTEGER NOT NULL)";
            db.execSQL(createPhotosTable);
        }

        if (oldVersion < 3) {
            // Add plants table
            String createPlantsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_PLANTS + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_PLANT_NAME + " TEXT NOT NULL, "
                    + COL_SCIENTIFIC_NAME + " TEXT, "
                    + COL_CATEGORY + " TEXT, "
                    + COL_PLANTING_PERIOD + " TEXT, "
                    + COL_HARVEST_PERIOD + " TEXT, "
                    + COL_CARE_INSTRUCTIONS + " TEXT, "
                    + COL_WATERING_FREQ + " TEXT, "
                    + COL_SUNLIGHT_REQ + " TEXT, "
                    + COL_SOIL_TYPE + " TEXT, "
                    + COL_COMPATIBILITY + " TEXT, "
                    + COL_GROWTH_DURATION + " INTEGER)";
            db.execSQL(createPlantsTable);
            insertDefaultPlants(db);
        }

        if (oldVersion < 4) {
            // Add journal and notifications tables
            String createJournalTable = "CREATE TABLE IF NOT EXISTS " + TABLE_JOURNAL + " ("
                    + COL_JOURNAL_ID + " TEXT PRIMARY KEY, "
                    + COL_PARCEL_ID + " TEXT NOT NULL, "
                    + COL_USER_EMAIL + " TEXT NOT NULL, "
                    + COL_ENTRY_TYPE + " TEXT NOT NULL, "
                    + COL_NOTES + " TEXT, "
                    + COL_WATER_AMOUNT + " REAL DEFAULT 0, "
                    + COL_TIMESTAMP + " INTEGER NOT NULL)";
            db.execSQL(createJournalTable);

            String createNotificationsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_NOTIFICATIONS + " ("
                    + COL_NOTIF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_PARCEL_ID + " TEXT NOT NULL, "
                    + COL_ALERT_TYPE + " TEXT NOT NULL, "
                    + COL_MESSAGE + " TEXT NOT NULL, "
                    + COL_SEVERITY + " TEXT NOT NULL, "
                    + COL_IS_READ + " INTEGER DEFAULT 0, "
                    + COL_TIMESTAMP + " INTEGER NOT NULL)";
            db.execSQL(createNotificationsTable);
        }
    }

    private void insertDefaultParcels(SQLiteDatabase db) {
        String[] parcelNumbers = {"A1", "A2", "B1", "B2", "B3", "B4", "C1", "C2",
                "D1", "D2", "D3", "E1", "E2", "E3", "E4", "E5"};

        for (String parcelNumber : parcelNumbers) {
            ContentValues values = new ContentValues();
            values.put(COL_PARCEL_NUMBER, parcelNumber);
            values.put(COL_IS_OCCUPIED, 0);
            db.insert(TABLE_PARCELS, null, values);
        }
    }

    private void insertDefaultPlants(SQLiteDatabase db) {
        insertPlant(db, "Tomates Cerises", "Solanum lycopersicum var. cerasiforme", "Légume-fruit",
                "Mars-Mai", "Juillet-Octobre", "Arrosage régulier, tuteurage nécessaire. Pincer les gourmands.",
                "2-3 fois/semaine", "Plein soleil (6-8h/jour)", "Riche en matière organique, bien drainé",
                "Basilic, Persil, Carotte", 70);

        insertPlant(db, "Laitue", "Lactuca sativa", "Légume-feuille",
                "Mars-Septembre", "Mai-Novembre", "Arrosage modéré, protection contre limaces.",
                "3-4 fois/semaine", "Mi-ombre", "Léger, frais, humifère",
                "Radis, Carotte, Fraise", 45);

        insertPlant(db, "Basilic", "Ocimum basilicum", "Herbe aromatique",
                "Avril-Juin", "Juin-Septembre", "Pincer régulièrement pour favoriser la ramification.",
                "2-3 fois/semaine", "Plein soleil", "Riche et bien drainé",
                "Tomate, Poivron", 60);

        insertPlant(db, "Courgettes", "Cucurbita pepo", "Légume-fruit",
                "Avril-Juin", "Juillet-Octobre", "Arrosage abondant, paillis recommandé.",
                "3-4 fois/semaine", "Plein soleil", "Riche en humus",
                "Haricot, Maïs, Capucine", 55);

        insertPlant(db, "Fraises", "Fragaria × ananassa", "Fruit",
                "Mars-Avril ou Septembre", "Mai-Juillet", "Paillage, protection contre oiseaux.",
                "2-3 fois/semaine", "Plein soleil à mi-ombre", "Léger, bien drainé",
                "Ail, Thym, Laitue", 90);

        insertPlant(db, "Carottes", "Daucus carota", "Légume-racine",
                "Mars-Juillet", "Juin-Novembre", "Éclaircir à 5cm, désherbage régulier.",
                "1-2 fois/semaine", "Plein soleil", "Léger, sableux, profond",
                "Oignon, Poireau, Radis", 80);

        insertPlant(db, "Radis", "Raphanus sativus", "Légume-racine",
                "Mars-Septembre", "Avril-Octobre", "Croissance rapide, récolte échelonnée.",
                "Quotidien par temps sec", "Mi-ombre", "Frais, léger",
                "Laitue, Carotte", 25);

        insertPlant(db, "Persil", "Petroselinum crispum", "Herbe aromatique",
                "Mars-Août", "Toute l'année", "Arrosage régulier, paillis en été.",
                "3-4 fois/semaine", "Mi-ombre", "Frais, humifère",
                "Tomate, Asperge", 75);
    }

    private void insertPlant(SQLiteDatabase db, String name, String scientificName, String category,
                             String plantingPeriod, String harvestPeriod, String care,
                             String watering, String sunlight, String soil, String compat, int growth) {
        ContentValues values = new ContentValues();
        values.put(COL_PLANT_NAME, name);
        values.put(COL_SCIENTIFIC_NAME, scientificName);
        values.put(COL_CATEGORY, category);
        values.put(COL_PLANTING_PERIOD, plantingPeriod);
        values.put(COL_HARVEST_PERIOD, harvestPeriod);
        values.put(COL_CARE_INSTRUCTIONS, care);
        values.put(COL_WATERING_FREQ, watering);
        values.put(COL_SUNLIGHT_REQ, sunlight);
        values.put(COL_SOIL_TYPE, soil);
        values.put(COL_COMPATIBILITY, compat);
        values.put(COL_GROWTH_DURATION, growth);
        db.insert(TABLE_PLANTS, null, values);
    }

    // ==================== SENSOR READING OPERATIONS ====================

    public long insertSensorReading(SensorReading reading) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_PARCEL_ID, reading.getParcelId());
        values.put(COL_HUMIDITY, reading.getHumidity());
        values.put(COL_TEMPERATURE, reading.getTemperature());
        values.put(COL_LIGHT, reading.getLightLevel());
        values.put(COL_PH, reading.getPh());
        values.put(COL_TIMESTAMP, reading.getTimestamp());

        long id = db.insert(TABLE_SENSORS, null, values);
        db.close();
        return id;
    }

    public SensorReading getLatestReading(String parcelId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_SENSORS
                + " WHERE " + COL_PARCEL_ID + " = ? "
                + " ORDER BY " + COL_TIMESTAMP + " DESC LIMIT 1";

        Cursor cursor = db.rawQuery(query, new String[]{parcelId});
        SensorReading reading = null;

        if (cursor.moveToFirst()) {
            reading = cursorToSensorReading(cursor);
        }

        cursor.close();
        db.close();
        return reading;
    }

    public List<SensorReading> getReadingsByParcel(String parcelId, int days) {
        List<SensorReading> readings = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        long cutoffTime = System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000);

        String query = "SELECT * FROM " + TABLE_SENSORS
                + " WHERE " + COL_PARCEL_ID + " = ? "
                + " AND " + COL_TIMESTAMP + " >= ? "
                + " ORDER BY " + COL_TIMESTAMP + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{parcelId, String.valueOf(cutoffTime)});

        while (cursor.moveToNext()) {
            readings.add(cursorToSensorReading(cursor));
        }

        cursor.close();
        db.close();
        return readings;
    }

    public void deleteOldReadings(int daysToKeep) {
        SQLiteDatabase db = this.getWritableDatabase();
        long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24L * 60 * 60 * 1000);

        db.delete(TABLE_SENSORS, COL_TIMESTAMP + " < ?", new String[]{String.valueOf(cutoffTime)});
        db.close();
    }

    private SensorReading cursorToSensorReading(Cursor cursor) {
        SensorReading reading = new SensorReading();
        reading.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        reading.setParcelId(cursor.getString(cursor.getColumnIndexOrThrow(COL_PARCEL_ID)));
        reading.setHumidity(cursor.getInt(cursor.getColumnIndexOrThrow(COL_HUMIDITY)));
        reading.setTemperature(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TEMPERATURE)));
        reading.setLightLevel(cursor.getInt(cursor.getColumnIndexOrThrow(COL_LIGHT)));
        reading.setPh(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PH)));
        reading.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)));
        return reading;
    }

    // ==================== PARCEL OPERATIONS ====================

    public long insertParcel(Parcel parcel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_PARCEL_NUMBER, parcel.getParcelNumber());
        values.put(COL_OWNER_EMAIL, parcel.getOwnerEmail());
        values.put(COL_PLANT_TYPE, parcel.getPlantType());
        values.put(COL_PLANTING_DATE, parcel.getPlantingDate());
        values.put(COL_HARVEST_DATE, parcel.getHarvestDate());
        values.put(COL_IS_OCCUPIED, parcel.isOccupied() ? 1 : 0);

        long id = db.insert(TABLE_PARCELS, null, values);
        db.close();
        return id;
    }

    public Parcel getParcelByNumber(String parcelNumber) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_PARCELS
                + " WHERE " + COL_PARCEL_NUMBER + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{parcelNumber});
        Parcel parcel = null;

        if (cursor.moveToFirst()) {
            parcel = cursorToParcel(cursor);
        }

        cursor.close();
        db.close();
        return parcel;
    }

    public List<Parcel> getParcelsByOwner(String ownerEmail) {
        List<Parcel> parcels = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_PARCELS
                + " WHERE " + COL_OWNER_EMAIL + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{ownerEmail});

        while (cursor.moveToNext()) {
            parcels.add(cursorToParcel(cursor));
        }

        cursor.close();
        db.close();
        return parcels;
    }

    public List<Parcel> getAllParcels() {
        List<Parcel> parcels = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PARCELS, null);

        while (cursor.moveToNext()) {
            parcels.add(cursorToParcel(cursor));
        }

        cursor.close();
        db.close();
        return parcels;
    }

    public int updateParcel(Parcel parcel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_OWNER_EMAIL, parcel.getOwnerEmail());
        values.put(COL_PLANT_TYPE, parcel.getPlantType());
        values.put(COL_PLANTING_DATE, parcel.getPlantingDate());
        values.put(COL_HARVEST_DATE, parcel.getHarvestDate());
        values.put(COL_IS_OCCUPIED, parcel.isOccupied() ? 1 : 0);

        int rows = db.update(TABLE_PARCELS, values,
                COL_PARCEL_NUMBER + " = ?",
                new String[]{parcel.getParcelNumber()});

        db.close();
        return rows;
    }

    private Parcel cursorToParcel(Cursor cursor) {
        Parcel parcel = new Parcel();
        parcel.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        parcel.setParcelNumber(cursor.getString(cursor.getColumnIndexOrThrow(COL_PARCEL_NUMBER)));
        parcel.setOwnerEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_OWNER_EMAIL)));
        parcel.setPlantType(cursor.getString(cursor.getColumnIndexOrThrow(COL_PLANT_TYPE)));
        parcel.setPlantingDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_PLANTING_DATE)));
        parcel.setHarvestDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_HARVEST_DATE)));
        parcel.setOccupied(cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_OCCUPIED)) == 1);
        return parcel;
    }

    // ==================== PHOTO OPERATIONS ====================

    public long insertPhoto(Photo photo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_PHOTO_ID, photo.getId());
        values.put(COL_PHOTO_PARCEL_ID, photo.getParcelId());
        values.put(COL_USER_EMAIL, photo.getUserEmail());
        values.put(COL_FILE_PATH, photo.getFilePath());
        values.put(COL_NOTES, photo.getNotes());
        values.put(COL_PHOTO_TIMESTAMP, photo.getTimestamp());

        long id = db.insert(TABLE_PHOTOS, null, values);
        db.close();
        return id;
    }

    public List<Photo> getPhotosByParcel(String parcelId) {
        List<Photo> photos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_PHOTOS
                + " WHERE " + COL_PHOTO_PARCEL_ID + " = ? "
                + " ORDER BY " + COL_PHOTO_TIMESTAMP + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{parcelId});

        while (cursor.moveToNext()) {
            photos.add(cursorToPhoto(cursor));
        }

        cursor.close();
        db.close();
        return photos;
    }

    public void deletePhoto(String photoId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PHOTOS, COL_PHOTO_ID + " = ?", new String[]{photoId});
        db.close();
    }

    private Photo cursorToPhoto(Cursor cursor) {
        Photo photo = new Photo();
        photo.setId(cursor.getString(cursor.getColumnIndexOrThrow(COL_PHOTO_ID)));
        photo.setParcelId(cursor.getString(cursor.getColumnIndexOrThrow(COL_PHOTO_PARCEL_ID)));
        photo.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)));
        photo.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_PATH)));
        photo.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES)));
        photo.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COL_PHOTO_TIMESTAMP)));
        return photo;
    }

    // ==================== PLANT LIBRARY OPERATIONS ====================

    public List<Plant> getAllPlants() {
        List<Plant> plants = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PLANTS + " ORDER BY " + COL_PLANT_NAME, null);

        while (cursor.moveToNext()) {
            plants.add(cursorToPlant(cursor));
        }

        cursor.close();
        db.close();
        return plants;
    }

    public List<Plant> searchPlants(String query) {
        List<Plant> plants = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String searchQuery = "SELECT * FROM " + TABLE_PLANTS
                + " WHERE " + COL_PLANT_NAME + " LIKE ? "
                + " OR " + COL_SCIENTIFIC_NAME + " LIKE ? "
                + " OR " + COL_CATEGORY + " LIKE ? "
                + " ORDER BY " + COL_PLANT_NAME;

        String searchPattern = "%" + query + "%";
        Cursor cursor = db.rawQuery(searchQuery,
                new String[]{searchPattern, searchPattern, searchPattern});

        while (cursor.moveToNext()) {
            plants.add(cursorToPlant(cursor));
        }

        cursor.close();
        db.close();
        return plants;
    }

    public Plant getPlantById(int plantId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PLANTS + " WHERE " + COL_ID + " = ?",
                new String[]{String.valueOf(plantId)});

        Plant plant = null;
        if (cursor.moveToFirst()) {
            plant = cursorToPlant(cursor);
        }

        cursor.close();
        db.close();
        return plant;
    }

    private Plant cursorToPlant(Cursor cursor) {
        Plant plant = new Plant();
        plant.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        plant.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_PLANT_NAME)));
        plant.setScientificName(cursor.getString(cursor.getColumnIndexOrThrow(COL_SCIENTIFIC_NAME)));
        plant.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)));
        plant.setPlantingPeriod(cursor.getString(cursor.getColumnIndexOrThrow(COL_PLANTING_PERIOD)));
        plant.setHarvestPeriod(cursor.getString(cursor.getColumnIndexOrThrow(COL_HARVEST_PERIOD)));
        plant.setCareInstructions(cursor.getString(cursor.getColumnIndexOrThrow(COL_CARE_INSTRUCTIONS)));
        plant.setWateringFrequency(cursor.getString(cursor.getColumnIndexOrThrow(COL_WATERING_FREQ)));
        plant.setSunlightRequirement(cursor.getString(cursor.getColumnIndexOrThrow(COL_SUNLIGHT_REQ)));
        plant.setSoilType(cursor.getString(cursor.getColumnIndexOrThrow(COL_SOIL_TYPE)));
        plant.setCompatibility(cursor.getString(cursor.getColumnIndexOrThrow(COL_COMPATIBILITY)));
        plant.setGrowthDurationDays(cursor.getInt(cursor.getColumnIndexOrThrow(COL_GROWTH_DURATION)));
        return plant;
    }

    // ==================== JOURNAL OPERATIONS ====================

    public long insertJournalEntry(JournalEntry entry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_JOURNAL_ID, entry.getId());
        values.put(COL_PARCEL_ID, entry.getParcelId());
        values.put(COL_USER_EMAIL, entry.getUserEmail());
        values.put(COL_ENTRY_TYPE, entry.getEntryType());
        values.put(COL_NOTES, entry.getNotes());
        values.put(COL_WATER_AMOUNT, entry.getWaterAmount());
        values.put(COL_TIMESTAMP, entry.getTimestamp());

        long id = db.insert(TABLE_JOURNAL, null, values);
        db.close();
        return id;
    }

    public List<JournalEntry> getJournalEntriesByParcel(String parcelId) {
        List<JournalEntry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_JOURNAL
                + " WHERE " + COL_PARCEL_ID + " = ? "
                + " ORDER BY " + COL_TIMESTAMP + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{parcelId});

        while (cursor.moveToNext()) {
            entries.add(cursorToJournalEntry(cursor));
        }

        cursor.close();
        db.close();
        return entries;
    }

    public void deleteJournalEntry(String entryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_JOURNAL, COL_JOURNAL_ID + " = ?", new String[]{entryId});
        db.close();
    }

    private JournalEntry cursorToJournalEntry(Cursor cursor) {
        JournalEntry entry = new JournalEntry();
        entry.setId(cursor.getString(cursor.getColumnIndexOrThrow(COL_JOURNAL_ID)));
        entry.setParcelId(cursor.getString(cursor.getColumnIndexOrThrow(COL_PARCEL_ID)));
        entry.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)));
        entry.setEntryType(cursor.getString(cursor.getColumnIndexOrThrow(COL_ENTRY_TYPE)));
        entry.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES)));
        entry.setWaterAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_WATER_AMOUNT)));
        entry.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)));
        return entry;
    }

    // ==================== NOTIFICATION OPERATIONS ====================

    public long insertNotification(NotificationRecord notification) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_PARCEL_ID, notification.getParcelId());
        values.put(COL_ALERT_TYPE, notification.getAlertType());
        values.put(COL_MESSAGE, notification.getMessage());
        values.put(COL_SEVERITY, notification.getSeverity());
        values.put(COL_IS_READ, notification.isRead() ? 1 : 0);
        values.put(COL_TIMESTAMP, notification.getTimestamp());

        long id = db.insert(TABLE_NOTIFICATIONS, null, values);
        db.close();
        return id;
    }

    public List<NotificationRecord> getAllNotifications() {
        List<NotificationRecord> notifications = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NOTIFICATIONS
                + " ORDER BY " + COL_TIMESTAMP + " DESC", null);

        while (cursor.moveToNext()) {
            notifications.add(cursorToNotification(cursor));
        }

        cursor.close();
        db.close();
        return notifications;
    }

    public List<NotificationRecord> getUnreadNotifications() {
        List<NotificationRecord> notifications = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NOTIFICATIONS
                + " WHERE " + COL_IS_READ + " = 0 ORDER BY " + COL_TIMESTAMP + " DESC", null);

        while (cursor.moveToNext()) {
            notifications.add(cursorToNotification(cursor));
        }

        cursor.close();
        db.close();
        return notifications;
    }

    public int markNotificationAsRead(int notificationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_IS_READ, 1);

        int rows = db.update(TABLE_NOTIFICATIONS, values,
                COL_NOTIF_ID + " = ?",
                new String[]{String.valueOf(notificationId)});

        db.close();
        return rows;
    }

    public void markAllNotificationsAsRead() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_IS_READ, 1);

        db.update(TABLE_NOTIFICATIONS, values, null, null);
        db.close();
    }

    private NotificationRecord cursorToNotification(Cursor cursor) {
        NotificationRecord notification = new NotificationRecord();
        notification.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_NOTIF_ID)));
        notification.setParcelId(cursor.getString(cursor.getColumnIndexOrThrow(COL_PARCEL_ID)));
        notification.setAlertType(cursor.getString(cursor.getColumnIndexOrThrow(COL_ALERT_TYPE)));
        notification.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(COL_MESSAGE)));
        notification.setSeverity(cursor.getString(cursor.getColumnIndexOrThrow(COL_SEVERITY)));
        notification.setRead(cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_READ)) == 1);
        notification.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)));
        return notification;
    }
}