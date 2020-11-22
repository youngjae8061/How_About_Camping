package com.example.how_about_camping;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WeatherActivity extends AppCompatActivity {

    Handler handler = new Handler();

    String lat, lon;
    final String TAG = "GPS";

    LocationManager locationManager;
    TextView tvCity, tvLatitude, tvLongitude, tvWeather, tvDate, tvTemp_c, tvTemp_f;
    RelativeLayout vProgressLayer;
    RequestQueue requestQueue;

    public static final Integer MY_PERMISSIONS_REQUEST_LOCATION = 0x5;
    ImageView weatherIcon;
    Bitmap bitmap;

    //ConstraintLayout weatherBackground = (ConstraintLayout) findViewById(R.id.weatherBackground);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_main);

        tvCity = (TextView) findViewById(R.id.tvCity);
        // tvLatitude = (TextView) findViewById(R.id.tvLatitude);
        //  tvLongitude = (TextView) findViewById(R.id.tvLongitude);
        tvWeather = (TextView) findViewById(R.id.temp_high);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvTemp_c = (TextView) findViewById(R.id.tvTemp_c);
        vProgressLayer = (RelativeLayout) findViewById(R.id.progressLayer);

        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        ImageButton backBtn = (ImageButton) findViewById(R.id.weatherBackBtn);

        // Register the listener with the Location Manager to receive location updates
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }
        } else {
            setupListeners();
        }

        backBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
               startMainActivity();
            }


        });
    }

    public void startMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    private void setBackgroundByTime() {
        long currentTimeMillis = System.currentTimeMillis();
        Date date = new Date(currentTimeMillis);
        SimpleDateFormat sdfHour = new SimpleDateFormat("HH");
        String hourText = sdfHour.format(date);

        int time = Integer.parseInt(hourText);
        if (time >= 0 && time < 6) {
            //weatherBackground.setBackgroundResource(R.drawable.sunny_night_background);
        } else if (time >= 6 && time < 15) {
          //  weatherBackground.setBackgroundResource(R.drawable.sunny_afternoon_background);
        } else if (time >= 15 && time < 20) {
          //  weatherBackground.setBackgroundResource(R.drawable.sunny_sunset_background);
        } else if (time >= 20 && time < 24) {
          //  weatherBackground.setBackgroundResource(R.drawable.sunny_night_background);
        }
    }

    @SuppressLint("MissingPermission")
    public void setupListeners() {
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                fetchLocationData(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);

        if (!netAndGpsEnabled()) {
            showSettingsAlert();
        } else {
            Log.d(TAG, "GPS on");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0x5: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupListeners();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private boolean netAndGpsEnabled() {

        boolean isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isGPS && !isNetwork) {
            return false;
        } else {
            return true;
        }
    }

    public void fetchLocationData(Location myLocation) {
        requestQueue = Volley.newRequestQueue(this);

        Log.d(TAG, "got new location update");

        lat = Double.toString(myLocation.getLatitude());
        lon = Double.toString(myLocation.getLongitude());
        Log.d(TAG, "DAta: " + myLocation);


        //String url = "api.openweathermap.org/data/2.5/forecast?lat=" + lat +"&lon="+lon+"&appid=8367e9646913ff43229d761791043b73=kr";
      //  String url = "http://api.openweathermap.org/data/2.5/forecast?id=524901&lang=zh_cn&appid=8367e9646913ff43229d761791043b73";
        //  String url = "http://api.openweathermap.org/data/2.5/weather?appid=8367e9646913ff43229d761791043b73&units=metric&id=1835848&lang=kr";
     //   url += "&lat=" + String.valueOf(lat) + "&lon=" + String.valueOf(lon);
        String url = "https://fcc-weather-api.glitch.me/api/current?lat=" + lat + "&lon=" + lon;
      //  url += "&lat="+String.valueOf(lat)+"&lon="+String.valueOf(lon);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "DAta: " + response);
                        try {
                            String locationTemp = response.getJSONObject("main").getString("temp");
                            String country = response.getJSONObject("sys").getString("country");
                            String place = response.getString("name") + ", " + country;
                            String locationWeather = response.getJSONArray("weather").getJSONObject(0).getString("main");
                            final String weatherIconUrl = response.getJSONArray("weather").getJSONObject(0).getString("icon");
                            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                            tvWeather.setText(locationWeather);
                            tvTemp_c.setText(locationTemp + " \u2103");
                            Double fahrenheit = Double.valueOf(locationTemp) * 1.8 + 32;
                            //   tvLatitude.setText(lat);
                            // tvLongitude.setText(lon);
                            tvDate.setText(currentDateTimeString);
                            tvCity.setText(place);
                            vProgressLayer.setVisibility(View.GONE);


                            weatherIcon = findViewById(R.id.weatherIcon);

                           // setBackgroundByTime();


                            Thread uThread = new Thread() {

                                @Override

                                public void run() {

                                    try {

                                        URL url = new URL(weatherIconUrl);


                                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();


                                        conn.setDoInput(true);

                                        conn.connect();


                                        InputStream is = conn.getInputStream();

                                        bitmap = BitmapFactory.decodeStream(is);


                                    } catch (MalformedURLException e) {

                                        e.printStackTrace();

                                    } catch (IOException e) {

                                        e.printStackTrace();

                                    }

                                }

                            };

                            uThread.start();


                            try {

                                uThread.join();

                                weatherIcon.setImageBitmap(bitmap);

                            } catch (InterruptedException e) {

                                e.printStackTrace();

                            }


                        } catch (JSONException e) {
                            //some exception handler code.
                            Log.d(TAG, "error occured: " + e);
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Something went wrong: " + error);
                        Toast.makeText(getApplicationContext(), "check your internet connection", Toast.LENGTH_LONG).show();
                    }
                });

        //add request to queue

        requestQueue.add(jsonObjectRequest);

    }

    public static int getResId(String resName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS is not Enabled!");
        alertDialog.setMessage("Do you want to turn on GPS?");

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });

        alertDialog.show();
    }
}