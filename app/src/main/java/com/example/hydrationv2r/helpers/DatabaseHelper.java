package com.example.hydrationv2r.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.hydrationv2r.models.DrinkModel;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {


    // Stores information about user-defined drink types
    private static final String TABLE_DRINKS = "drinks";
    private static final String COL_DRINK_ID = "id";
    private static final String COL_DRINK_NAME = "name";
    private static final String COL_DRINK_ML = "ml";
    private static final String COL_ICON = "icon";
    private static final String COL_COLOUR = "colour";

    // Stores hydration logs
    private static final String TABLE_LOGS = "drink_logs";
    private static final String COL_LOG_ID = "log_id";
    private static final String COL_LOG_DATE = "log_date";
    private static final String COL_LOG_DRINK_ID = "drink_id";
    private static final String COL_LOG_AMOUNT = "amount_ml";
    private static final String COL_LOG_TIMESTAMP = "timestamp";
    public static final String COL_IS_HIDDEN = "is_hidden";


    public static final String TABLE_DAY_STATUS = "day_status"; // Stores whether a day has been excluded from stats
    public static final String COL_DAY_DATE = "day_date";
    public static final String COL_IS_EXCLUDED="is_excluded";

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
                    COL_LOG_DATE + " INTEGER, " +
                    COL_LOG_DRINK_ID + " INTEGER, " +
                    COL_LOG_AMOUNT + " INTEGER, " +
                    COL_LOG_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(" + COL_LOG_DRINK_ID + ") REFERENCES " + TABLE_DRINKS + "(id))";
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
            String dropTable = "DROP TABLE IF EXISTS " + TABLE_LOGS;
            db.execSQL(dropTable);
            dropTable = "DROP TABLE IF EXISTS " + TABLE_DRINKS;

            db.execSQL(dropTable);
            Log.d("DatabaseHelper", "onUpgrade - Successfully dropped tables");
        }
        catch (Exception e) {
            Log.e("DatabaseHelper", "onUpgrade - Failed to create new tables: " + e);
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
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_DRINKS + " WHERE " + COL_IS_HIDDEN + " = 0", null);

        if (cursor.moveToFirst()) {
            do {
                list.add(new DrinkModel(cursor.getInt(0), cursor.getString(1),
                        cursor.getInt(2), cursor.getString(3), cursor.getInt(4)));
            } while (cursor.moveToNext());
        }
        cursor.close();
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
        String query = "SELECT SUM(" + COL_LOG_AMOUNT + ") FROM " + TABLE_LOGS + " WHERE " + COL_LOG_DATE + " >= ?";

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(startOfToday)})) {
            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting hydration: ", e);
        }
        return total;
    }

    /**
     * Create a new log in the logs table with a drink.
     * Drink size can be customised and is not limited to the drink type.
     */
    public void logDrink(String timestamp, int drinkId, int amountMl) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_LOG_DATE, timestamp);
        values.put(COL_LOG_DRINK_ID, drinkId);
        values.put(COL_LOG_AMOUNT, amountMl);
        db.insert(TABLE_LOGS, null, values);

        Log.d("DVB", "addrink");
    }

    public DrinkModel getDrinkById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        DrinkModel drink = null;

        Cursor cursor = db.query(TABLE_DRINKS, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            int ml = cursor.getInt(cursor.getColumnIndexOrThrow("ml"));
            String icon = cursor.getString(cursor.getColumnIndexOrThrow("icon"));
            int colour = cursor.getInt(cursor.getColumnIndexOrThrow("colour"));
            drink = new DrinkModel(id, name, ml, icon, colour);
            cursor.close();
        }
        return drink;
    }

}

