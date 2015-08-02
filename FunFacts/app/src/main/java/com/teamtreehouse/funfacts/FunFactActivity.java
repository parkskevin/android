package com.teamtreehouse.funfacts;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class FunFactActivity extends Activity {

    public static final String TAG = FunFactActivity.class.getSimpleName();

    private FactBook mFactBook = new FactBook();
    private LayoutColor mLayoutColor = new LayoutColor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fun_fact);

        // Declare our View variables & assign Views from layout file
        final TextView factLabel = (TextView) findViewById(R.id.factTextView);
        final Button showFactButton = (Button) findViewById(R.id.showFactButton);
        final RelativeLayout rLayout = (RelativeLayout) findViewById(R.id.factRelativeLayout);

        final View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // The button was clicked, update text label with a new fact and change color
                factLabel.setText(mFactBook.getFact());
                int newColor = mLayoutColor.getColor();
                showFactButton.setTextColor(newColor);
                rLayout.setBackgroundColor(newColor);
            }
        };
        showFactButton.setOnClickListener(listener);
        //Toast.makeText(this, "Activity Created!", Toast.LENGTH_LONG).show();
        Log.d(TAG, "We're logging from the onCreate method.");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fun_fact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
