package design.testdesign1;

import android.content.Context;
import android.content.res.AssetManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.*;

/**
 * Created by maykl on 14.3.2014.
 */
public class CustomCSVParser {
public static Context context;

    public CustomCSVParser(Context context) {
        this.context = context;
    }

    /********************PUBLIC METHODS FOR CSVParser***************************/

    // return data from csv in array
    public JSONArray getParsedCSV() {
        String next[];
        List<String[]> list = new ArrayList<String[]>();
        AssetManager assetManager = context.getAssets();
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(assetManager.open("tfa-numbers.csv")), ';');
            //in open();
            for(;;) {
                next = reader.readNext();
                if(next != null) {
                    list.add(next);
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONArray result = new JSONArray();
        for (int i = 1; i < list.size(); i++) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("country",list.get(i)[0]);
                obj.put("general",list.get(i)[1]);
                obj.put("police",list.get(i)[2]);
                obj.put("medical",list.get(i)[3]);
                obj.put("fire",list.get(i)[4]);
                obj.put("note",list.get(i)[5]);
                result.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
