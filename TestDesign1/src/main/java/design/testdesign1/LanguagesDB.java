package design.testdesign1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by maykl on 9.3.2014.
 * controller for local database of languages
 */

public class LanguagesDB {
    protected static final String DATABASE_NAME = "tfa";
    protected static final int DATABASE_VERSION = 1;

    protected static final String TB_NAME = "tfa_languages";


    // list of columns
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SLUG = "slug";
    public static final String[] columns = { COLUMN_ID, COLUMN_NAME, COLUMN_SLUG };

    protected static final String ORDER_BY = COLUMN_ID + " DESC";

    private SQLiteOpenHelper openHelper;

    static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TB_NAME + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_SLUG + " TEXT,"
                    + COLUMN_NAME + " TEXT"
                    + ");"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    public LanguagesDB(Context ctx) {
        openHelper = new DatabaseHelper(ctx);
    }

    /********************PUBLIC METHODS FOR LanguagesDB***************************/

    // return list of all stored languages
    public Cursor getLanguages() {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        return db.query(TB_NAME, columns, null, null, null, null, ORDER_BY);
    }

    // return true if specified language is stored in local DB
    public boolean languageExists(String slug) {
        SQLiteDatabase db = openHelper.getReadableDatabase();

        Cursor cursor = null;
        try{
            cursor = db.query(true, TB_NAME, columns,  COLUMN_SLUG+ " = '" + slug + "'", null, null, null, null, null);
            if (cursor.getCount() > 0) {
                return true;
            }
            else {
                return false;
            }
        }catch(Exception ex){
            return false;
        }finally{
            if (cursor != null)
                cursor.close();
        }
    }

    // insert new language in local DB
    public void insertLanguage(JSONObject language) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        try {
            values.put(COLUMN_SLUG, (String)language.get("slug"));
            values.put(COLUMN_NAME, (String)language.get("name"));
            db.insert(TB_NAME, null, values);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        db.close();
    }

    // create table if doesn't exist yet
    public void initialiseTables() {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TB_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_SLUG + " TEXT,"
                + COLUMN_NAME + " TEXT"
                + ");"
        );
        db.close();
    }

    public void close() {
        openHelper.close();
    }
}
