package design.testdesign1;

import android.support.v4.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maykl on 15.3.2014.
 */
public class NumbersFragment extends ListFragment {

    private Context context;
    private CustomCSVParser customCSVParser;
    private JSONArray numbersArray;
    Button back;
    ListView lv;
    TableLayout tableNumbers;
    LinearLayout row1col2;
    LinearLayout row2col2;
    LinearLayout row3col2;
    LinearLayout row4col2;

    /********constructor********/
    public NumbersFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_numbers, container, false);
        back = (Button)rootView.findViewById(R.id.back);
        back.setVisibility(View.INVISIBLE);
        tableNumbers = (TableLayout)rootView.findViewById(R.id.tableNumbers);
        tableNumbers.setVisibility(View.INVISIBLE);
        row1col2 = (LinearLayout)rootView.findViewById(R.id.row1col2);
        row2col2 = (LinearLayout)rootView.findViewById(R.id.row2col2);
        row3col2 = (LinearLayout)rootView.findViewById(R.id.row3col2);
        row4col2 = (LinearLayout)rootView.findViewById(R.id.row4col2);

        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        customCSVParser = new CustomCSVParser(context);
        numbersArray = customCSVParser.getParsedCSV();

        int length = numbersArray.length();
        List<String> numbersList = new ArrayList<String>(length);
        for (int i = 0; i < length; i++) {
            try {
                numbersList.add((String) numbersArray.getJSONObject(i).get("country"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        setListAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, numbersList));
        lv = getListView();

        // listening to single list item on click
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String[] sections = {"general", "police", "medical", "fire"};
                row1col2.removeAllViews();
                row2col2.removeAllViews();
                row3col2.removeAllViews();
                row4col2.removeAllViews();

                try {

                    for (int i = 0; i < 4; i++) {
                        for (String line : stripText(numbersArray.getJSONObject(position).get(sections[i]).toString())) {
                            Button btn = new Button(context);
                            btn.setText(line);
                            btn.setBackgroundResource(R.drawable.green_button);
                            btn.setMinimumWidth(150);

                            LinearLayout ll = null;
                            switch (i) {
                                case 0:
                                    ll = (LinearLayout)getView().findViewById(R.id.row1col2);
                                    break;
                                case 1:
                                    ll = (LinearLayout)getView().findViewById(R.id.row2col2);
                                    break;
                                case 2:
                                    ll = (LinearLayout)getView().findViewById(R.id.row3col2);
                                    break;
                                case 3:
                                    ll = (LinearLayout)getView().findViewById(R.id.row4col2);
                                    break;
                            }
                            ll.addView(btn);
                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btn.getLayoutParams();
                            layoutParams.bottomMargin = 10;

                            btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Button b = (Button)v;
                                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                                    callIntent.setData(Uri.parse("tel:"+b.getText().toString()));
                                    startActivity(callIntent);

                                }
                            });
                        }
                    }

                } catch (JSONException e) { e.printStackTrace(); }

                getListView().setVisibility(View.INVISIBLE);
                back.setVisibility(View.VISIBLE);
                tableNumbers.setVisibility(View.VISIBLE);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back.setVisibility(View.INVISIBLE);
                tableNumbers.setVisibility(View.INVISIBLE);
                lv.setVisibility(View.VISIBLE);
            }
        });

    }

    public String getOnlyNumbers(String str) {
        if (str == null) {
            return null;
        }

        StringBuffer strBuff = new StringBuffer();
        char c;

        for (int i = 0; i < str.length() ; i++) {
            c = str.charAt(i);

            if (Character.isDigit(c)) {
                strBuff.append(c);
            }
        }
        return strBuff.toString();
    }

    public ArrayList<String> stripText(String s) {
        ArrayList<String> result = new ArrayList<String>();
        String[] arr = s.split("\\W+");
        for ( String ss : arr) {
            ss = getOnlyNumbers(ss);
            if (ss != "") {
                result.add(ss);
            }
        }
        return result;
    }
}