package design.testdesign1;

import android.support.v4.app.Fragment;
//import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by maykl on 9.3.2014.
 */
public class FrontFragment extends Fragment {


    // url to make request
    private static String urlPosts = "http://54.229.97.203/get_posts.php";
    private static String urlQuestions = "http://54.229.97.203/get_questions.php";
    private String lanSlug = "sk";

    private JSONObject activeLanguage;
    private static final String TAG_POSTS = "posts";

    private Spinner spinnerLang;
    private Button btnActivate;
    private Button buttonAddTranslation;
    private JSONArray postsArray;

    private boolean canChangeLanguage;

    private JSONArray questionsArray;
    private JSONArray languagesArray;
    private JSONParser jParser = new JSONParser();

    Context context;
    private Locale myLocale;
    LanguagesDB languagesDB;
    MainActivity.ScreenSlidePagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    public FrontFragment(Context context, ViewPager mViewPager, MainActivity.ScreenSlidePagerAdapter mSectionsPagerAdapter, JSONArray languagesArray) {
        this.context = context;
        this.mViewPager = mViewPager;
        this.mSectionsPagerAdapter = mSectionsPagerAdapter;
        this.languagesArray = languagesArray;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_front, container, false);
        // initialise graphical elements




        spinnerLang = (Spinner) rootView.findViewById(R.id.language_spinner);
        populateLangSpinner(languagesArray);
        buttonAddTranslation = (Button) rootView.findViewById(R.id.buttonAddTranslation);
        buttonAddTranslation.setVisibility(View.INVISIBLE);

        // initialise language database
        languagesDB = new LanguagesDB(context);
        languagesDB.initialiseTables();

        // enable policy mode required to establish connection with server
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }




        btnActivate = (Button) rootView.findViewById(R.id.btn_activate);
        // activate selected language and refresh content of application
        btnActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activateLanguage();

                Toast toast = Toast.makeText(context, R.string.toast_language, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });


        // on click action
        // download all content in selected language
        buttonAddTranslation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add new language in database
                languagesDB.insertLanguage(activeLanguage);

                // download and save posts in local DB
                postsArray = downloadPosts(urlPosts);
                SettingsDB settingsDB = new SettingsDB(context);
                settingsDB.insertOption(1, "active_language", lanSlug);
                PostsDB postsDB = new PostsDB(context, lanSlug);
                // initialise posts table if doesn't exist yet
                if (!postsDB.tableExists()) {
                    postsDB.initialiseTables();
                }
                postsDB.insertPosts(postsArray);
                postsDB.close();

                // download and save questions in local DB
                questionsArray = downloadPosts(urlQuestions);
                settingsDB = new SettingsDB(context);
                QuestionsDB questionsDB = new QuestionsDB(context, settingsDB.getOption("active_language"));
                questionsDB.initialiseTables();
                questionsDB.insertQuestions(questionsArray);
                questionsDB.close();

                buttonAddTranslation.setVisibility(View.INVISIBLE);

//                activateLanguage();
            }
        });

        canChangeLanguage = false;


        // action when language in dropdown spinner is changed


        spinnerLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                if(canChangeLanguage){
                    try {
                        activeLanguage = (JSONObject) languagesArray.get(spinnerLang.getSelectedItemPosition());
                        lanSlug = (String) activeLanguage.get("slug");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // if selected language doesn't exist in local database
                    // display button for download
                    if (languagesDB.languageExists(lanSlug)) {
                        buttonAddTranslation.setVisibility(View.INVISIBLE);
                    } else {
                        buttonAddTranslation.setVisibility(View.VISIBLE);
                    }
                    languagesDB.close();
                    translateFrontPage();
//                    activateLanguage();

//                }
//                canChangeLanguage = true;
            }




            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return rootView;
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);






    }








    /********************PUBLIC METHODS FOR FrontFragment***************************/
    public void populateLangSpinner(JSONArray languages)
    {
        String test = null;
        int length = languages.length();
        List<String> list = new ArrayList<String>(length);
        for (int i = 0; i < length; i++)
        {
            try {
                test = (String) languages.getJSONObject(i).get("name");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            list.add(test);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context,android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLang.setAdapter(dataAdapter);
    }

    // download posts from server in specified language
    // return array of downloaded posts
    public JSONArray downloadPosts(String url) {
        try {
            JSONObject testObject = jParser.getJSONFromUrl(url, lanSlug);
            postsArray = testObject.getJSONArray(TAG_POSTS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postsArray;
    }

    // change locale language settings of application
    public void changeLang(String lang)
    {
        if (lang.equalsIgnoreCase(""))
            return;
        myLocale = new Locale(lang);
        saveLocale(lang);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }


    // save current language settings for application
    public void saveLocale(String lang){
        String langPref = "Language";
        SharedPreferences prefs = context.getSharedPreferences("CommonPrefs", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(langPref, lang);
        editor.commit();
    }

    public void activateLanguage(){


        ((MainActivity)getActivity()).reloadAllFragments();
//        mSectionsPagerAdapter.notifyDataSetChanged();
//        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    public void translateFrontPage() {
        SettingsDB settingsDB = new SettingsDB(context);
        if (!settingsDB.tableExists()) {
            settingsDB.initialiseTables();
        }
        settingsDB.insertOption(1, "active_language", lanSlug);
        // reload all fragments with correct content in new language
        changeLang(lanSlug);

        ((Button)getActivity().findViewById(R.id.btn_activate)).setText(R.string.btn_activate);
        ((Button)getActivity().findViewById(R.id.buttonAddTranslation)).setText(R.string.btn_download);
        ((TextView)getActivity().findViewById(R.id.txtLang)).setText(R.string.choose_language);
        ((TextView)getActivity().findViewById(R.id.textDisclaimer)).setText(R.string.disclaimer);
        ((TextView)getActivity().findViewById(R.id.txt_moreLanguages)).setText(R.string.more_languages);

    }
}