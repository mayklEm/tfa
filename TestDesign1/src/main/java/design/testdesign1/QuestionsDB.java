package design.testdesign1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by maykl on 9.3.2014.
 */
public class QuestionsDB {

    protected static final String DATABASE_NAME = "tfa";
    protected static final int DATABASE_VERSION = 1;

    protected static String TB_NAME = "tfa_questions";


    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_QUESTION_ID = "question_id";
    public static final String COLUMN_POST_TITLE = "post_title";
    public static final String COLUMN_ANSWER_YES = "answer_YES";
    public static final String COLUMN_ANSWER_NO = "answer_NO";
    public static final String COLUMN_QUESTION_LANGUAGE = "question_language";


    public static final String[] columns = { COLUMN_ID, COLUMN_QUESTION_ID, COLUMN_POST_TITLE, COLUMN_ANSWER_YES, COLUMN_ANSWER_NO, COLUMN_QUESTION_LANGUAGE};

    protected static final String ORDER_BY = COLUMN_ID + " DESC";

    private SQLiteOpenHelper openHelper;

    public QuestionsDB(Context ctx, String activeLanguage) {
        openHelper = new DatabaseHelper(ctx);
        TB_NAME = "tfa_questions_" +activeLanguage;
    }

    static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TB_NAME + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_QUESTION_ID + " INTEGER NOT NULL,"
                    + COLUMN_POST_TITLE + " TEXT,"
                    + COLUMN_ANSWER_YES + " TEXT,"
                    + COLUMN_ANSWER_NO + " TEXT,"
                    + COLUMN_QUESTION_LANGUAGE + " TEXT"
                    + ");"
            );
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    public Cursor getQuestion(int id) {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        return db.query(true, TB_NAME, columns,  COLUMN_QUESTION_ID+ " = " + id, null, null, null, null, null);
    }

    public void insertQuestions(JSONArray questions) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        try {
            int length = questions.length();
            for (int i = 0; i < length; i++) {
                values.put(COLUMN_QUESTION_ID, (String) questions.getJSONObject(i).get("question_id"));
                values.put(COLUMN_POST_TITLE, (String) questions.getJSONObject(i).get("post_title"));
                values.put(COLUMN_ANSWER_YES, (String) questions.getJSONObject(i).get("answer_yes"));
                values.put(COLUMN_ANSWER_NO, (String) questions.getJSONObject(i).get("answer_no"));
                values.put(COLUMN_QUESTION_LANGUAGE, (String) questions.getJSONObject(i).get("lan"));
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
                + COLUMN_QUESTION_ID + " INTEGER NOT NULL,"
                + COLUMN_POST_TITLE + " TEXT,"
                + COLUMN_ANSWER_YES + " TEXT,"
                + COLUMN_ANSWER_NO + " TEXT,"
                + COLUMN_QUESTION_LANGUAGE + " TEXT"
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

