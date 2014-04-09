package design.testdesign1;

import java.util.Locale;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.app.ActionBar;
//import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.StrictMode;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    private static String urlLanguages = "http://54.229.97.203/get_languages.php";
    LanguagesDB languagesDB;
    private JSONArray languagesArray;
    private JSONParser jParser = new JSONParser();
    private Locale myLocale;

    ScreenSlidePagerAdapter mSectionsPagerAdapter;

    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadLocale();
        // enable policy mode required to establish connection with server
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        languagesDB = new LanguagesDB(this);

        languagesDB.initialiseTables();

        // if is online get all languages
        if (isNetworkAvailable()) {
            JSONObject jsonLanguages = jParser.getJSONFromUrl(urlLanguages, null);
            try {
                languagesArray = jsonLanguages.getJSONArray("languages");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // if offline get languages from local database
        } else {
//            languagesDB.initialiseTables();
            Cursor cursor = languagesDB.getLanguages();
            languagesArray = parseLanguagesCursor(cursor);
            languagesDB.close();
        }

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab().setIcon(mSectionsPagerAdapter.getPageIcon(i))
//                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }




    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.

        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class ScreenSlidePagerAdapter extends FragmentPagerAdapter {

        public ScreenSlidePagerAdapter (android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new FrontFragment(getBaseContext(),mViewPager,mSectionsPagerAdapter, languagesArray);
                case 1:
                    return new AllPostsFragment(getBaseContext());
                case 2:
                    return new QuestionsFragment(getBaseContext());
                case 3:
                    return new NumbersFragment(getBaseContext());
            }
            return new AllPostsFragment(getBaseContext());
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

        public int getPageIcon(int position) {
            switch (position) {
                case 0:
                    return R.drawable.globe_48;
                case 1:
                    return R.drawable.document_48;
                case 2:
                    return R.drawable.med_48;
                case 3:
                    return R.drawable.phone_48;
                default:
                    return R.drawable.document_48;

            }
        }


    }

    /********************PUBLIC METHODS FOR MainActivity***************************/

    // change language version of app and save new app preferences
    public void changeLang(String lang)
    {
        if (lang.equalsIgnoreCase(""))
            return;
        myLocale = new Locale(lang);
        saveLocale(lang);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        this.getResources().updateConfiguration(config, this.getResources().getDisplayMetrics());
    }

    // save language version in app preferences
    public void saveLocale(String lang)
    {
        String langPref = "Language";
        SharedPreferences prefs = this.getSharedPreferences("CommonPrefs", this.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(langPref, lang);
        editor.commit();
    }

    // load language version from app preferences
    public void loadLocale()
    {
        String langPref = "Language";
        SharedPreferences prefs = this.getSharedPreferences("CommonPrefs", this.MODE_PRIVATE);
        String language = prefs.getString(langPref, "");
        changeLang(language);
    }


    // check if internet is available
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // parse cursor from languages DB
    // return array of available languages
    public JSONArray parseLanguagesCursor(Cursor cursor) {
        JSONArray languages = new JSONArray();
        while (cursor.moveToNext()) {
            int nameIndex = cursor.getColumnIndex(LanguagesDB.COLUMN_NAME);
            int slugIndex = cursor.getColumnIndex(LanguagesDB.COLUMN_SLUG);
            JSONObject obj = new JSONObject();
            try {
                obj.put("name", cursor.getString(nameIndex));
                obj.put("slug", cursor.getString(slugIndex));
                languages.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return languages;
    }




    public void reloadAllFragments() {
        mSectionsPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
}



}
