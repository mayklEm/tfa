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
 * Created by maykl on 9.3.2014.
 */
public class PostsDB {

    protected static final String DATABASE_NAME = "tfa";
    protected static final int DATABASE_VERSION = 1;

    protected static String TB_NAME = "tfa_conditions";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_POST_ID = "post_id";
    public static final String COLUMN_POST_CATEGORY = "post_category";
    public static final String COLUMN_POST_TITLE = "post_title";
    public static final String COLUMN_TREATMENT = "treatment";
    public static final String COLUMN_SYMPTOMS = "symptoms";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_ANSWER_ID = "answer_id";
    public static final String COLUMN_POST_LANGUAGE = "post_language";


    public static final String[] columns = { COLUMN_ID, COLUMN_POST_ID, COLUMN_POST_CATEGORY, COLUMN_POST_TITLE, COLUMN_TREATMENT, COLUMN_SYMPTOMS, COLUMN_NOTE, COLUMN_ANSWER_ID, COLUMN_POST_LANGUAGE };

    protected static final String ORDER_BY = COLUMN_POST_TITLE + " ASC";

    private SQLiteOpenHelper openHelper;

    public PostsDB(Context ctx, String activeLanguage) {
        openHelper = new DatabaseHelper(ctx);
        this.TB_NAME = "tfa_conditions_" + activeLanguage;
    }

    static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }



    public Cursor getPosts() {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        return db.query(TB_NAME, columns, null, null, null, null, ORDER_BY);
    }

    public Cursor getCategoires() {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        return db.query(true, TB_NAME, new String[] { COLUMN_POST_CATEGORY }, null, null, COLUMN_POST_CATEGORY, null, null, null);
    }

    public Cursor getPost(String id) {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String substr = "[" + id + "]";
        return db.query(true, TB_NAME, columns,  COLUMN_ANSWER_ID+ " LIKE '%" + substr+"%'", null, null, null, null, null);
    }

    public Cursor getPostsByCategory(String category) {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        return db.query(true, TB_NAME, columns,  COLUMN_POST_CATEGORY+ " = '" + category+"'", null, null, null, ORDER_BY, null);
    }



    public void insertPosts(JSONArray posts) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        try {
            int length = posts.length();
            for (int i = 0; i < length; i++) {
                values.put(COLUMN_POST_ID, Integer.parseInt((String)posts.getJSONObject(i).get("post_id")));
                values.put(COLUMN_POST_CATEGORY, (String) posts.getJSONObject(i).get("cat"));
                values.put(COLUMN_POST_TITLE, (String) posts.getJSONObject(i).get("post_title"));
                values.put(COLUMN_TREATMENT, (String) posts.getJSONObject(i).get("post_content"));
                values.put(COLUMN_SYMPTOMS, (String) posts.getJSONObject(i).get("symptom"));
                values.put(COLUMN_NOTE, (String) posts.getJSONObject(i).get("note"));
                values.put(COLUMN_ANSWER_ID, (String) posts.getJSONObject(i).get("answer_id"));
                values.put(COLUMN_POST_LANGUAGE, (String) posts.getJSONObject(i).get("lan"));
                db.insert(TB_NAME, null, values);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        db.close();
    }

    public void initialiseTables() {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TB_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_POST_ID + " INTEGER NOT NULL,"
                + COLUMN_POST_CATEGORY + " TEXT,"
                + COLUMN_POST_TITLE + " TEXT,"
                + COLUMN_TREATMENT + " TEXT,"
                + COLUMN_SYMPTOMS + " TEXT,"
                + COLUMN_NOTE + " TEXT,"
                + COLUMN_ANSWER_ID + " TEXT,"
                + COLUMN_POST_LANGUAGE + " TEXT"
                + ");"
        );
        db.close();
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