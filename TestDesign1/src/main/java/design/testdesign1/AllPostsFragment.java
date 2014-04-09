package design.testdesign1;

import android.support.v4.app.ListFragment;
//import android.app.ListFragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maykl on 9.3.2014.
 */
public class AllPostsFragment extends ListFragment {
    Context context;
    private JSONArray postsArray;
    private JSONArray categoriesArray;
    private String activeLanguage;
    private ListView lv1;
    private ArrayAdapter<String> adapter1;
    private ArrayAdapter<String> adapter2;

    private ScrollView scroll;
    Button btn_list_back;

    // this variable checks which layout (category list or single post view)
    // is currently visible
    private boolean isCategoriesView = true;

    // variable holds last answer ID from diagnostic tool
    private String diagnosticAnswer = "";

    /************ CONSTRUCTOR ************/
    public AllPostsFragment(Context context) {
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_posts_list, container, false);
        scroll = (ScrollView)rootView.findViewById(R.id.scroll);
        btn_list_back = (Button)rootView.findViewById(R.id.btn_list_back);
        btn_list_back.setVisibility(View.GONE);
        scroll.setVisibility(View.INVISIBLE);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        postsArray = new JSONArray();
        categoriesArray = new JSONArray();

        // initialise settings table and get active language
        SettingsDB settingsDB = new SettingsDB(context);
        if (!settingsDB.tableExists()) {
            settingsDB.initialiseTables();
            settingsDB.insertOption(1, "active_language", "");
        }
        try {
            activeLanguage = settingsDB.getOption("active_language");

            // get last answer ID from diagnostic tool and reset value
            diagnosticAnswer = settingsDB.getOption("active_answer");
            settingsDB.insertOption(2, "active_answer", "");
        } catch (Exception e){}

        // initialise posts table
        PostsDB postsDB = new PostsDB(context, activeLanguage);
        if (!postsDB.tableExists()) {
            postsDB.initialiseTables();
        }

        // get categories from local database if there is no return from diagnostic tool
        // otherwise get list of posts matched with last answer ID from diagnostic tool
        try {
            if (diagnosticAnswer.equals("")) {
                categoriesArray = parseCategoriesCursor(postsDB.getCategoires());
                isCategoriesView = true;
            }
            else {
                postsArray = parsePostsCursor(postsDB.getPost(diagnosticAnswer));
                isCategoriesView = false;
            }
            postsDB.close();
        } catch(Exception e) {}

        if (!isCategoriesView) {
            // initialise view for response from diagnostic tool
            btn_list_back.setVisibility(View.VISIBLE);

            int postsCount = postsArray.length();
            List<String> postsList = new ArrayList<String>(postsCount);
            for (int i = 0; i < postsCount; i++) {
                try {
                    postsList.add((String) postsArray.getJSONObject(i).get("post_title"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            adapter1 = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, postsList);
            setListAdapter(adapter1);
            adapter1.notifyDataSetChanged();
            lv1 = getListView();
        }
        else {
            // initialise default view for list of all categories
            int categoriesCount = categoriesArray.length();
            List<String> categoriesList = new ArrayList<String>(categoriesCount);
            for (int i = 0; i < categoriesCount; i++) {
                try {
                    categoriesList.add((String) categoriesArray.getJSONObject(i).get("post_category"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            adapter1 = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, categoriesList);
            setListAdapter(adapter1);
            adapter1.notifyDataSetChanged();
            lv1 = getListView();
        }

        // on Back button click
        btn_list_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_list_back.setVisibility(View.GONE);
                ViewPager pager = (ViewPager) getActivity().findViewById(R.id.pager);
                pager.setAdapter(pager.getAdapter());
                pager.setCurrentItem(1);
                isCategoriesView = true;
            }
        });

        // listening to category item on click
        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                btn_list_back.setVisibility(View.VISIBLE);
                if (isCategoriesView) {
                    // listener is active for list of all categories
                    try {
                        SettingsDB settingsDB = new SettingsDB(context);
                        PostsDB postsDB = new PostsDB(context, settingsDB.getOption("active_language"));
                        Cursor cursor = postsDB.getPostsByCategory((String) categoriesArray.getJSONObject(position).get("post_category"));
                        postsArray = parsePostsCursor(cursor);

                        // create array list of all categories
                        int postsCount = postsArray.length();
                        List<String> postsList = new ArrayList<String>(postsCount);
                        for (int i = 0; i < postsCount; i++) {
                            try {
                                postsList.add((String) postsArray.getJSONObject(i).get("post_title"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // create array list of main categories
                    int postsCount = postsArray.length();
                    List<String> postsList = new ArrayList<String>(postsCount);
                    for (int i = 0; i < postsCount; i++) {
                        try {
                            postsList.add((String) postsArray.getJSONObject(i).get("post_title"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    adapter2 = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, postsList);
                    setListAdapter(adapter2);
                    isCategoriesView = false;
                }
                else {
                    // load view for clicked single post
                    JSONObject clickedPost = new JSONObject();
                    try {
                        clickedPost = postsArray.getJSONObject(position);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String itemTitle = "";
                    String itemSymptoms = "";
                    String itemTreatment = "";
                    String itemNote = "";
                    try {
                        itemTitle = (String) clickedPost.get("post_title");
                        itemSymptoms = (String) clickedPost.get("post_symptoms");
                        itemTreatment = (String) clickedPost.get("post_content");
                        itemNote = (String) clickedPost.get("post_note");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // assign text views into variables
                    TextView titleView = (TextView) getView().findViewById(R.id.itemTitle);
                    TextView symptomsView = (TextView) getView().findViewById(R.id.itemSymptoms);
                    TextView treatmentView = (TextView) getView().findViewById(R.id.itemTreatment);
                    TextView noteView = (TextView) getView().findViewById(R.id.itemNote);

                    // populate text views with data
                    titleView.setText(itemTitle);
                    symptomsView.append(itemSymptoms);
                    treatmentView.append(itemTreatment);
                    // experimental type which parse html tags
                    treatmentView.setText(Html.fromHtml(itemTreatment), TextView.BufferType.SPANNABLE);
                    noteView.append(itemNote);
                    lv1.setVisibility(View.INVISIBLE);
                    scroll.setVisibility(View.VISIBLE);
                }
            }
        });

    }



    /********************PUBLIC METHODS FOR AllPostsFragment***************************/

    // parse cursor from posts database
    // return array of posts
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

    // parse cursor from posts database
    // return array of categories
    public JSONArray parseCategoriesCursor(Cursor cursor) {
        JSONArray posts = new JSONArray();
        while (cursor.moveToNext()) {
            int categoryIndex = cursor.getColumnIndex(PostsDB.COLUMN_POST_CATEGORY);
            JSONObject obj = new JSONObject();
            try {
                obj.put("post_category", cursor.getString(categoryIndex));
                posts.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return posts;
    }
}