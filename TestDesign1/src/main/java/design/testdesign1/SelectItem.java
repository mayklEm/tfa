package design.testdesign1;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

/**
 * Created by maykl on 9.3.2014.
 */
public class SelectItem implements AdapterView.OnItemSelectedListener {
    public String langSlug;

    public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
        Toast.makeText(parent.getContext(),
                "Selecting Item : " + parent.getItemAtPosition(pos).toString(), Toast.LENGTH_SHORT).show();
        langSlug = parent.getItemAtPosition(pos).toString();
    }


    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }
}