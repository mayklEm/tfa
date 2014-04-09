package design.testdesign1;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by maykl on 10.3.2014.
 */
public class QuestionsFragment extends Fragment {
    Context context;
    private TextView questionTextView;
    private int currentQuestionID;
    private JSONObject currentQuestion;



    public QuestionsFragment(Context context) {
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_questions, container, false);

        questionTextView = (TextView) rootView.findViewById(R.id.questionTextView);
        Button buttonNo = (Button)rootView.findViewById(R.id.button_no);
        Button buttonYes = (Button)rootView.findViewById(R.id.button_yes);
        Button button_refresh = (Button)rootView.findViewById(R.id.button_refresh);

        currentQuestion = new JSONObject();
        currentQuestionID = 0;
        SettingsDB settingsDB = new SettingsDB(context);
        QuestionsDB questionsDB = new QuestionsDB(context, settingsDB.getOption("active_language"));

        if (!questionsDB.tableExists()) {
            questionsDB.initialiseTables();
        }
        try {
            currentQuestion = parseQuestionCursor(questionsDB.getQuestion(currentQuestionID));
            questionsDB.close();
            questionTextView.setText(currentQuestion.get("post_title") + "\n");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentNoID = 99999;
                try {
                    currentNoID = Integer.parseInt((String) currentQuestion.get("answer_no"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                SettingsDB settingsDB = new SettingsDB(context);
                QuestionsDB questionsDB = new QuestionsDB(context, settingsDB.getOption("active_language"));
                Cursor cursor = questionsDB.getQuestion(currentNoID);
                if (cursor.getCount() > 0)
                {
                    //next question exists
                    currentQuestionID = currentNoID;
                    currentQuestion = parseQuestionCursor(questionsDB.getQuestion(currentQuestionID));
                    questionsDB.close();
                    try {
                        questionTextView.setText(currentQuestion.get("post_title") + "\n");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    // next question doesn't exist
                    settingsDB = new SettingsDB(context);
                    try {
                        settingsDB.insertOption(2, "active_answer", currentQuestion.get("answer_no").toString());
                        ViewPager pager = (ViewPager) getActivity().findViewById(R.id.pager);
                        pager.setAdapter(pager.getAdapter());
                        pager.setCurrentItem(1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentYesID = 99999;
                try {
                    currentYesID = Integer.parseInt((String) currentQuestion.get("answer_yes"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //next question exists
                SettingsDB settingsDB = new SettingsDB(context);
                QuestionsDB questionsDB = new QuestionsDB(context, settingsDB.getOption("active_language"));
                Cursor cursor = questionsDB.getQuestion(currentYesID);
                if (cursor.getCount() > 0)
                {
                    currentQuestionID = currentYesID;
                    currentQuestion = parseQuestionCursor(questionsDB.getQuestion(currentQuestionID));
                    questionsDB.close();
                    try {
                        questionTextView.setText(currentQuestion.get("post_title") + "\n");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    // next question doesn't exist
                    settingsDB = new SettingsDB(context);
                    try {
                        settingsDB.insertOption(2, "active_answer", currentQuestion.get("answer_yes").toString());
                        ViewPager pager = (ViewPager)getActivity().findViewById(R.id.pager);
                        pager.setAdapter(pager.getAdapter());
                        pager.setCurrentItem(1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        button_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentQuestionID = 0;
                try {
                    SettingsDB settingsDB = new SettingsDB(context);
                    QuestionsDB questionsDB = new QuestionsDB(context, settingsDB.getOption("active_language"));
                    currentQuestion = parseQuestionCursor(questionsDB.getQuestion(currentQuestionID));
                    questionsDB.close();
                    questionTextView.setText(currentQuestion.get("post_title") + "\n");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // download content in current language in local database
    }


    public JSONObject parseQuestionCursor(Cursor cursor) {
        JSONObject question = new JSONObject();
        if(cursor.moveToNext()) {
            int questionIdIndex = cursor.getColumnIndex(QuestionsDB.COLUMN_QUESTION_ID);
            int titleIndex = cursor.getColumnIndex(QuestionsDB.COLUMN_POST_TITLE);
            int answerYesIndex = cursor.getColumnIndex(QuestionsDB.COLUMN_ANSWER_YES);
            int answerNoIndex = cursor.getColumnIndex(QuestionsDB.COLUMN_ANSWER_NO);
            try {
                question.put("question_id", cursor.getString(questionIdIndex));
                question.put("post_title", cursor.getString(titleIndex));
                question.put("answer_yes", cursor.getString(answerYesIndex));
                question.put("answer_no", cursor.getString(answerNoIndex));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return question;
    }

    public JSONArray parsePostsCursor(Cursor cursor) {
        JSONArray posts = new JSONArray();
        while (cursor.moveToNext()) {
            int idIndex = cursor.getColumnIndex(PostsDB.COLUMN_POST_ID);
            int categoryIndex = cursor.getColumnIndex(PostsDB.COLUMN_POST_CATEGORY);
            int titleIndex = cursor.getColumnIndex(PostsDB.COLUMN_POST_TITLE);
            int answerIdIndex = cursor.getColumnIndex(PostsDB.COLUMN_ANSWER_ID);
            int contentIndex = cursor.getColumnIndex(PostsDB.COLUMN_TREATMENT);
            int symptomsIndex = cursor.getColumnIndex(PostsDB.COLUMN_SYMPTOMS);
            int noteIndex = cursor.getColumnIndex(PostsDB.COLUMN_NOTE);
            JSONObject obj = new JSONObject();
            try {
                obj.put("post_id", cursor.getString(idIndex));
                obj.put("post_category", cursor.getString(categoryIndex));
                obj.put("post_title", cursor.getString(titleIndex));
                obj.put("post_content", cursor.getString(contentIndex));
                obj.put("post_symptoms", cursor.getString(symptomsIndex));
                obj.put("post_note", cursor.getString(noteIndex));
                obj.put("answer_id", cursor.getString(answerIdIndex));
                posts.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return posts;
    }



}