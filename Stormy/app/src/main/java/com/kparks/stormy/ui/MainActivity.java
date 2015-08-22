package com.kparks.stormy.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.kparks.stormy.R;
import com.kparks.stormy.weather.Current;
import com.kparks.stormy.weather.Day;
import com.kparks.stormy.weather.Forecast;
import com.kparks.stormy.weather.Hour;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String DAILY_FORECAST = "DAILY_FORECAST";
    public static final String CITY_NAME = "CITY_NAME";
    public static final String HOURLY_FORECAST = "HOURLY_FORECAST";
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 30000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;


    private String apiKey = "db25cce3262458e32f3f7bc5cf1a9ad2";
    private double mLat = 45.2661; //defaults
    private double mLong = -122.6906; //defaults

    public String getCityName() {
        return mCityName;
    }

    private String mCityName; //locality String
    private Forecast mForecast;
    private GoogleApiClient mGoogleApiClient; //for location updates
    private Location mLastLocation; //last valid location
    private LocationRequest mLocationRequest; //to request location
    private String mLastUpdateTimeString; //update time

    @Bind(R.id.temperatureLabel) TextView mTemperatureLabel;
    @Bind(R.id.humidityValue) TextView mHumidityValue;
    @Bind(R.id.precipValue) TextView mPrecipValue;
    @Bind(R.id.summaryLabel) TextView mSummaryLabel;
    @Bind(R.id.iconImageView) ImageView mIconImageView;
    @Bind(R.id.refreshImageView) ImageView mRefreshImageView;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;
    @Bind(R.id.locationLabel) TextView mLocationLabel;
    @Bind(R.id.lastUpdatedText) TextView mLastUpdateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //build client should trigger Google API services for location
        buildGoogleApiClient();
        //client makes call to build forecast on connect, so this is static until then
        mProgressBar.setVisibility(View.INVISIBLE);
        //allow force update via refresh
        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getForecast();
            }
        });
    }

    /* If network is available, show refresh state, try to update the location.
        if update location fails, we assume Google Services API is failing, show Toast error
        if network isn't available, show Toast error
        Otherwise, get a valid location and use it for forecast call
        If forecast call succeeds, update the display with new data
     */
    private void getForecast() {
        if(isNetworkAvailable()) {
            toggleRefresh();
            getTheLastLocation();
            if(mLastLocation != null) {
                String forecastUrl = "https://api.forecast.io/forecast/" +
                        apiKey + "/" + mLat + "," + mLong;
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(forecastUrl)
                        .build();

                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toggleRefresh();
                            }
                        });
                        alertUserAboutError();
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toggleRefresh();
                            }
                        });
                        try {
                            String jsonData = response.body().string();
                            if (response.isSuccessful()) {
                                mForecast = parseForecastDetails(jsonData);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateDisplay();
                                    }
                                });
                            } else {
                                alertUserAboutError();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Exception caught: ", e);
                        } catch (JSONException e) {
                            Log.e(TAG, "Exception caught: ", e);
                        }
                    }
                });
            } else {
                if(mGoogleApiClient.isConnecting() == false) {
                    Toast.makeText(this, "Cannot find location", Toast.LENGTH_LONG).show();
                    toggleRefresh();
                    return;
                }
            }
        } else {
            Toast.makeText(this, R.string.network_down_toast, Toast.LENGTH_LONG).show();
        }
    }

    /*
        Toggle state of refresh icon
     */
    private void toggleRefresh() {
        if(mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    /*
        Update UI elements with data points
     */
    private void updateDisplay() {
        mTemperatureLabel.setText(mForecast.getCurrent().getTemperature() + "");
        mHumidityValue.setText(mForecast.getCurrent().getHumidity() + "");
        mPrecipValue.setText(mForecast.getCurrent().getPrecipChance() + "%");
        mSummaryLabel.setText(mForecast.getCurrent().getSummary());
        Drawable drawable = getResources().getDrawable(mForecast.getCurrent().getIconId());
        mIconImageView.setImageDrawable(drawable);
        mLocationLabel.setText(mCityName);
        mLastUpdateTime.setText("Last updated: " + mLastUpdateTimeString);
    }

    private Forecast parseForecastDetails(String jsonData) throws JSONException {
        Forecast forecast = new Forecast();
        forecast.setCurrent(getCurrentDetails(jsonData));
        forecast.setHourlyForecast(getHourlyForecast(jsonData));
        forecast.setDailyForecast(getDailyForecast(jsonData));
        return forecast;
    }

    /*
        Get array of Daily data and create Day[] array
     */
    private Day[] getDailyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");
        Day[] days = new Day[data.length()];
        for(int i = 0; i < data.length(); i++) {
            JSONObject jsonDay = data.getJSONObject(i);
            Day day = new Day();

            day.setSummary(jsonDay.getString("summary"));
            day.setIcon(jsonDay.getString("icon"));
            day.setMaxTemperature(jsonDay.getDouble("temperatureMax"));
            day.setTime(jsonDay.getLong("time"));
            day.setTimeZone(timezone);

            days[i] = day;
        }
        return days;
    }

    /*
        Get array of Hourly data and create Hour[] array
     */
    private Hour[] getHourlyForecast(String jsonData)  throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");

        Hour[] hours = new Hour[data.length()];
        for(int i = 0; i < data.length(); i++) {
            JSONObject jsonHour = data.getJSONObject(i);
            Hour hour = new Hour();

            hour.setSummary(jsonHour.getString("summary"));
            hour.setTemperature(jsonHour.getDouble("temperature"));
            hour.setIcon(jsonHour.getString("icon"));
            hour.setTime(jsonHour.getLong("time"));
            hour.setTimeZone(timezone);
            hours[i] = hour;
        }
        return hours;
    }

    /*
        Parse weather details into sensible variables
     */
    private Current getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        JSONObject currently = forecast.getJSONObject("currently");
        Current current = new Current();
        current.setTimeZone(forecast.getString("timezone"));
        current.setHumidity(currently.getDouble("humidity"));
        current.setTime(currently.getLong("time"));
        current.setIcon(currently.getString("icon"));
        current.setPrecipChance(currently.getDouble("precipProbability"));
        current.setSummary(currently.getString("summary"));
        current.setTemperature(currently.getDouble("temperature"));

        return current;
    }

    /*
        Check if the network is available for access
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if(networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    /*
        Display an error message
     */
    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

    /*
        Build the Google Services API client, try to gather location from it
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /*
        Set up location request params
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    /*
        Shut down location requests
     */
    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    /*
        Start location requests
     */
    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /*
        On GoogleServicesApiClient connect, gather the forecast info. This means this occurs
        onResume, onStart, basically anytime you navigate to the app.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        getForecast();
    }

    /*
        Try to gather the location information from Google Services API. Use Geocoder to extract
        a locality name (city name) from the data gathered.
     */
    private void getTheLastLocation() {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mLastUpdateTimeString = DateFormat.getTimeInstance().format(new Date());

        if(mLastLocation != null)
        {
            mLat = mLastLocation.getLatitude();
            mLong = mLastLocation.getLongitude();
            Geocoder geocoder = new Geocoder(this);
            try {
                List<Address> results = geocoder.getFromLocation(mLat, mLong, 1);
                mCityName = results.get(0).getLocality();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        mLastUpdateTimeString = DateFormat.getTimeInstance().format(new Date());
        getForecast();
    }

    /*
        Butterknife creates the onClickListener in onCreate that points to this function
     */
    @OnClick (R.id.dailyButton)
    public void startDailyActivity(View view) {
        Intent intent = new Intent(this, DailyForecastActivity.class);
        intent.putExtra(DAILY_FORECAST, mForecast.getDailyForecast());
        intent.putExtra(CITY_NAME, getCityName());
        startActivity(intent);
    }

    /*
    Butterknife creates the onClickListener in onCreate that points to this function
 */
    @OnClick (R.id.hourlyButton)
    public void startHourlyActivity(View view) {
        Intent intent = new Intent(this, HourlyForecastActivity.class);
        intent.putExtra(HOURLY_FORECAST, mForecast.getHourlyForecast());
        intent.putExtra(CITY_NAME, getCityName());
        startActivity(intent);
    }
}
