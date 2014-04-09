package design.testdesign1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by maykl on 11.3.2014.
 */
public class SettingsDB {

    protected static final String DATABASE_NAME = "tfa";
    protected static final int DATABASE_VERSION = 1;

    protected static final String TB_NAME = "tfa_settings";


    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TERM = "term";
    public static final String COLUMN_VALUE = "value";

    public static final String[] columns = { COLUMN_ID, COLUMN_TERM, COLUMN_VALUE };


    private SQLiteOpenHelper openHelper;

    static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TB_NAME + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_TERM + " TEXT,"
                    + COLUMN_VALUE + " TEXT"
                    + ");"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    public SettingsDB(Context ctx) {
        openHelper = new DatabaseHelper(ctx);
    }

    public String getOption(String term) {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = db.query(true, TB_NAME, new String[] {COLUMN_VALUE},  COLUMN_TERM+ " = '" + term+"'", null, null, null, null, null);
        cursor.moveToNext();
        return cursor.getString(0);
    }



    public void insertOption(int id, String term, String value) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, id);
        values.put(COLUMN_TERM, term);
        values.put(COLUMN_VALUE, value);
        db.execSQL("INSERT OR REPLACE INTO "+TB_NAME+" ("+COLUMN_ID+", "+COLUMN_TERM+", "+COLUMN_VALUE+") VALUES ('"
                +id+"', '"+term+"', '"+value+"');");
        db.close();
    }

    public void initialiseTables() {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TB_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_TERM + " TEXT,"
                + COLUMN_VALUE + " TEXT"
                + ");"
        );
    }

    public boolean tableExists() {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + TB_NAME + "'", null);
        if (c.moveToFirst()) {
            db.close();
            return true;
        }
        db.close();
        return false;
    }

    public void close() {
        openHelper.close();
    }
}

