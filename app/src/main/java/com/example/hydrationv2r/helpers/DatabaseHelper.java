package com.example.hydrationv2r.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.hydrationv2r.models.DrinkModel;
import com.example.hydrationv2r.models.LoggedDrinkModel;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {


    // Stores information about user-defined drink types
    private static final String TABLE_DRINKS = "drinks";
    private static final String COL_DRINK_ID = "id";
    private static final String COL_DRINK_NAME = "name";
    private static final String COL_DRINK_ML = "ml";
    private static final String COL_ICON = "icon";
    private static final String COL_COLOUR = "colour";
    public static final String COL_IS_HIDDEN = "is_hidden";

    // Stores hydration logs
    private static final String TABLE_LOGS = "drink_logs";
    private static final String COL_LOG_ID = "log_id";
    private static final String COL_LOG_DRINK_ID = "drink_id";
    private static final String COL_LOG_AMOUNT = "amount_ml";
    private static final String COL_LOG_TIMESTAMP = "timestamp";

    public static final String TABLE_DAY_STATUS = "day_status"; // Stores whether a day has been excluded from stats
    public static final String COL_DAY_DATE = "day_date";
    public static final String COL_IS_EXCLUDED = "is_excluded";

    private static final String DATABASE_NAME = "hydration_db";
    private static final int DATABASE_VERSION = 1;

    private static DatabaseHelper instance;

    private DatabaseHelper(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION); }

    // Ensure a singleton pattern
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        try {
            String createDrinksTable = "CREATE TABLE " + TABLE_DRINKS + " (" +
                    COL_DRINK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_DRINK_NAME + " TEXT, " +
                    COL_DRINK_ML + " INTEGER, " +
                    COL_ICON + " TEXT, " +
                    COL_COLOUR + " INTEGER, " +
                    COL_IS_HIDDEN + " INTEGER DEFAULT 0)";
            db.execSQL(createDrinksTable);

            String createLogsTable = "CREATE TABLE " + TABLE_LOGS + " (" +
                    COL_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_LOG_DRINK_ID + " INTEGER, " +
                    COL_LOG_AMOUNT + " INTEGER, " +
                    COL_LOG_TIMESTAMP + " INTEGER, " +
                    "FOREIGN KEY(" + COL_LOG_DRINK_ID + ") REFERENCES " + TABLE_DRINKS + "(" + COL_DRINK_ID + "))";
            db.execSQL(createLogsTable);

            db.execSQL("CREATE TABLE " + TABLE_DAY_STATUS + " (" +
                    COL_DAY_DATE + " TEXT PRIMARY KEY, " +
                    COL_IS_EXCLUDED + " INTEGER DEFAULT 0)");

            // Insert 4 default drink types
            db.execSQL("INSERT INTO drinks (id, name, ml, icon, colour) VALUES (1, 'Tea', 300, 'icon_twelve', 0xFF4CAF50)");
            db.execSQL("INSERT INTO drinks (id, name, ml, icon, colour) VALUES (2, 'Water (Pint)', 550, 'icon_thirteen', 0xFF5FB0B0)");
            db.execSQL("INSERT INTO drinks (id, name, ml, icon, colour) VALUES (3, 'Coffee', 300, 'icon_fourteen', 0xFFF44336)");
            db.execSQL("INSERT INTO drinks (id, name, ml, icon, colour) VALUES (4, 'Beer', 330, 'icon_eleven', 0xFFFFC107)");

            Log.d("DatabaseHelper", "onCreate - Successfully created new tables");
        }
        catch (Exception e) {
            Log.e("DatabaseHelper", "onCreate - Failed to create new tables: " + e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAY_STATUS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DRINKS);
            Log.d("DatabaseHelper", "onUpgrade - Successfully dropped tables");
        }
        catch (Exception e) {
            Log.e("DatabaseHelper", "onUpgrade - Failed to drop table(s) " + e);
        }
        onCreate(db);
    }

    /**
     * Queries the drinks table to see which drink types are 'active'
     * @return DrinkModel list of active drinks
     */
    public List<DrinkModel> getActiveDrinks() {
        List<DrinkModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_DRINKS + " WHERE " + COL_IS_HIDDEN + " = 0", null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(new DrinkModel(cursor.getInt(0), cursor.getString(1),
                            cursor.getInt(2), cursor.getString(3), cursor.getInt(4)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error loading active layout drink templates", e);
        }
        return list;
    }

    /**
     * Total of all logs for the current day, calculated from midnight in the user's current timezone
     * @return hydration amount in ML
     */
    public int getTodayHydration() {
        long startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        int total = 0;

        // Sum from the logs table for today's date
        String query = "SELECT SUM(" + COL_LOG_AMOUNT + ") FROM " + TABLE_LOGS + " WHERE " + COL_LOG_TIMESTAMP + " >= ?";

        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(startOfToday)})) {
            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting hydration: ", e);
        }
        return total;
    }

    /**
     * Create a new log entry in the logs table with a drink.
     */
    public void logDrink(long timeStamp, int drinkId, int amountMl) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_LOG_TIMESTAMP, timeStamp);
        values.put(COL_LOG_DRINK_ID, drinkId);
        values.put(COL_LOG_AMOUNT, amountMl);

        db.insert(TABLE_LOGS, null, values);
        Log.d("DatabaseHelper", "logDrink - Record inserted successfully for ID: " + drinkId);
    }

    /**
     * Delete all history log entries for the current day
     */
    public void resetTodayHydration() {
        long startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int rowsDeleted = db.delete(TABLE_LOGS, COL_LOG_TIMESTAMP + " >= ?", new String[]{String.valueOf(startOfToday)});
            Log.d("DatabaseHelper", "resetTodayHydration - Clear successful. Records dropped: " + rowsDeleted);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error executing dynamic hydration reset", e);
        }
    }

    /**
     * Gets a DrinkModel by its ID
     */
    public DrinkModel getDrinkById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        DrinkModel drink = null;

        try (Cursor cursor = db.query(TABLE_DRINKS, null, COL_DRINK_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_DRINK_NAME));
                int ml = cursor.getInt(cursor.getColumnIndexOrThrow(COL_DRINK_ML));
                String icon = cursor.getString(cursor.getColumnIndexOrThrow(COL_ICON));
                int colour = cursor.getInt(cursor.getColumnIndexOrThrow(COL_COLOUR));
                drink = new DrinkModel(id, name, ml, icon, colour);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error pulling direct target drink model info match", e);
        }
        return drink;
    }

    /**
     * Get a list of drink logs for the current day
     */
    public List<LoggedDrinkModel> getLogsForToday() {
        List<LoggedDrinkModel> logList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        long startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        String query = "SELECT l." + COL_LOG_ID + ", l." + COL_LOG_AMOUNT + ", l." + COL_LOG_TIMESTAMP + ", " +
                "d." + COL_DRINK_NAME + ", d." + COL_ICON + ", d." + COL_COLOUR + ", d." + COL_DRINK_ID +
                " FROM " + TABLE_LOGS + " l " +
                " JOIN " + TABLE_DRINKS + " d ON l." + COL_LOG_DRINK_ID + " = d." + COL_DRINK_ID +
                " WHERE l." + COL_LOG_TIMESTAMP + " >= ?" +
                " ORDER BY l." + COL_LOG_TIMESTAMP + " DESC";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(startOfToday)})) {
            if (cursor != null && cursor.moveToFirst()) {
                int idxLogId = cursor.getColumnIndex(COL_LOG_ID);
                int idxAmount = cursor.getColumnIndex(COL_LOG_AMOUNT);
                int idxTimestamp = cursor.getColumnIndex(COL_LOG_TIMESTAMP);
                int idxName = cursor.getColumnIndex(COL_DRINK_NAME);
                int idxIcon = cursor.getColumnIndex(COL_ICON);
                int idxColor = cursor.getColumnIndex(COL_COLOUR);
                int idxDrinkId = cursor.getColumnIndex(COL_DRINK_ID);

                do {
                    int logId = cursor.getInt(idxLogId);
                    int amount = cursor.getInt(idxAmount);

                    long timestamp = cursor.getLong(idxTimestamp);

                    String name = cursor.getString(idxName);
                    String icon = cursor.getString(idxIcon);
                    int color = cursor.getInt(idxColor);
                    int drinkId = cursor.getInt(idxDrinkId);

                    DrinkModel drinkInfo = new DrinkModel(drinkId, name, 0, icon, color);

                    logList.add(new LoggedDrinkModel(logId, drinkInfo, amount, timestamp));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error building daily historical log parsing loop", e);
        }
        // FIXED: Removed db.close() block to safeguard background data fetching operations
        return logList;
    }



    /**
     * Returns a map of Drink IDs and how many times they have been consumed today.
     */
    public Map<Integer, Integer> getTodayDrinkCounts() {
        Map<Integer, Integer> countsMap = new HashMap<>();
        long startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        String query = "SELECT " + COL_LOG_DRINK_ID + ", COUNT(*) FROM " + TABLE_LOGS +
                " WHERE " + COL_LOG_TIMESTAMP + " >= ?" +
                " GROUP BY " + COL_LOG_DRINK_ID;

        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(startOfToday)})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int drinkId = cursor.getInt(0);
                    int count = cursor.getInt(1);
                    countsMap.put(drinkId, count);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error counting drink frequencies", e);
        }
        return countsMap;
    }

    /**
     * Deletes a specific hydration log entry from the database using its log ID.
     * @param logId The unique ID of the log record to remove.
     * @return true if the row was successfully deleted, false otherwise.
     */
    public boolean deleteSpecificLog(int logId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = 0;

        try {
            rowsDeleted = db.delete(TABLE_LOGS, COL_LOG_ID + " = ?", new String[]{String.valueOf(logId)});
            Log.d("DatabaseHelper", "deleteSpecificLog - Successfully removed log ID: " + logId);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting log with ID: " + logId, e);
        }
        return rowsDeleted > 0;
    }
}