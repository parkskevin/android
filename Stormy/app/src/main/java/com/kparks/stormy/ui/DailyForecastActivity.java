package com.kparks.stormy.ui;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kparks.stormy.R;
import com.kparks.stormy.adapters.DayAdapter;
import com.kparks.stormy.weather.Day;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DailyForecastActivity extends Activity {

    private Day[] mDays;

    @Bind(R.id.locationLabel) TextView mCityNameLabel;
    @Bind(android.R.id.list) ListView mListView;
    @Bind(android.R.id.empty) TextView mEmptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.DAILY_FORECAST);
        mDays = Arrays.copyOf(parcelables, parcelables.length, Day[].class);
        String cityName = intent.getStringExtra(MainActivity.CITY_NAME);
        mCityNameLabel.setText(cityName);

        DayAdapter adapter = new DayAdapter(this, mDays);
        //setListAdapter(adapter);
        mListView.setAdapter(adapter);
        mListView.setEmptyView(mEmptyTextView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Day dayTapped = mDays[position];
                String dayOfTheWeek = dayTapped.getDayOfTheWeek();
                String conditions = dayTapped.getSummary();
                String hiTemp = dayTapped.getMaxTemperature() + "";
                String message = String.format("On %s the high will be %s and it will be %s",
                        dayOfTheWeek, hiTemp, conditions);
                Toast.makeText(DailyForecastActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

//    @Override
//    protected void onListItemClick(ListView l, View v, int position, long id) {
//        super.onListItemClick(l, v, position, id);
//
//        Day dayTapped = mDays[position];
//        String dayOfTheWeek = dayTapped.getDayOfTheWeek();
//        String conditions = dayTapped.getSummary();
//        String hiTemp = dayTapped.getMaxTemperature() + "";
//        String message = String.format("On %s the high will be %s and it will be %s",
//                dayOfTheWeek, hiTemp, conditions);
//        Toast.makeText(DailyForecastActivity.this, message, Toast.LENGTH_LONG).show();
//    }
}
