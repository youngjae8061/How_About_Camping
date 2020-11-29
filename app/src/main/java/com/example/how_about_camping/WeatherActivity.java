package com.example.how_about_camping;

import android.content.Context;
import android.content.SharedPreferences;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class WeatherActivity extends AppCompatActivity {
    Handler handler = new Handler();
    Location location;
   // double latitude;
  //  double longitude;

    String latitude2, longitude2;
    final String TAG = "GPS";

    LocationManager locationManager;
    TextView dlt1, dlt2, dlt3, dlt4, dlt5, dht1, dht2, dht3, dht4, dht5, daily_yo1, daily_yo2,
            daily_yo3, daily_yo4, daily_yo5, humidityId, current_rain,tvCity,
            tvdescription, weatherMain, tvDate, tvTemp_c, temp_high, temp_low, feels_temp, tvlocation;
    RelativeLayout vProgressLayer;
    RequestQueue requestQueue;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    public static final Integer MY_PERMISSIONS_REQUEST_LOCATION = 0x5;
    ImageView weatherIconR, daily_ico1, daily_ico2,daily_ico3,daily_ico4,daily_ico5;

    private FrameLayout address_find;
    private FrameLayout redirect;

    Bitmap bitmap;
    private String rain_1h;
    private String rain_3h;

    private String temp_f;
    private String dailyLow;
    private String dailyHigh;
    private String temp_extra;

    ConstraintLayout background;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_main);

        daily_yo1 = (TextView)findViewById(R.id.time_daily);
        daily_yo2 = (TextView)findViewById(R.id.time_daily2);
        daily_yo3 = (TextView)findViewById(R.id.time_daily3);
        daily_yo4 = (TextView)findViewById(R.id.time_daily4);
        daily_yo5 = (TextView)findViewById(R.id.time_daily5);
        daily_ico1 = (ImageView)findViewById(R.id.daily_image);
        daily_ico2 = (ImageView)findViewById(R.id.daily_image2);
        daily_ico3 = (ImageView)findViewById(R.id.daily_image3);
        daily_ico4 = (ImageView)findViewById(R.id.daily_image4);
        daily_ico5 = (ImageView)findViewById(R.id.daily_image5);
        dht1 = (TextView)findViewById(R.id.daily_high_temp);
        dht2 = (TextView)findViewById(R.id.daily_high_temp2);
        dht3 = (TextView)findViewById(R.id.daily_high_temp3);
        dht4 = (TextView)findViewById(R.id.daily_high_temp4);
        dht5 = (TextView)findViewById(R.id.daily_high_temp5);
        dlt1 = (TextView)findViewById(R.id.daily_low_temp);
        dlt2 = (TextView)findViewById(R.id.daily_low_temp2);
        dlt3 = (TextView)findViewById(R.id.daily_low_temp3);
        dlt4 = (TextView)findViewById(R.id.daily_low_temp4);
        dlt5 = (TextView)findViewById(R.id.daily_low_temp5);


        background = (ConstraintLayout)findViewById(R.id.weatherBackground);
        weatherIconR = (ImageView)findViewById(R.id.weatherIcon);
        current_rain = (TextView)findViewById(R.id.textViewPrecipitation);
        tvCity = (TextView)findViewById(R.id.tvLocation);
        tvlocation = (TextView)findViewById(R.id.tvLocation);
        tvdescription = (TextView)findViewById(R.id.weather_description);
        tvDate = (TextView)findViewById(R.id.tvDate);
        tvTemp_c = (TextView)findViewById(R.id.tvTemp_c);
        vProgressLayer = (RelativeLayout)findViewById(R.id.progressLayer);
        temp_high = (TextView)findViewById(R.id.temp_high);
        temp_low = (TextView)findViewById(R.id.temp_low);
        feels_temp = (TextView)findViewById(R.id.feels_temp);
        weatherMain = (TextView)findViewById(R.id.weather_main);
        humidityId = (TextView)findViewById(R.id.humidity);
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        address_find = findViewById(R.id.search_layout);
        address_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

        redirect= findViewById(R.id.redirect_layout);
        redirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GpsTracker gpsTracker = new GpsTracker(WeatherActivity.this);
                double latitude = gpsTracker.getLatitude();
                double longitude = gpsTracker.getLongitude();

                PreferenceManager.setFloat(WeatherActivity.this,"LATITUDE",(float)latitude);
                PreferenceManager.setFloat(WeatherActivity.this,"LONGITUDE",(float)longitude);

                String address = getCurrentAddress(latitude, longitude);
                address = AddressParsingUtil.getSigunguFromFullAddress(address);

                Toast.makeText(WeatherActivity.this, "주소가 "+address+"로 설정되었습니다",Toast.LENGTH_SHORT).show();

                PreferenceManager.setBoolean(WeatherActivity.this,"IS_ADDRESS_CHANGED",true);
                PreferenceManager.setString(getApplicationContext(),"CITY",address);
                startActivity(new Intent(getApplicationContext(),WeatherActivity.class));
            }
        });

        displayWeather(getApplicationContext());
        ImageButton backBtn = (ImageButton) findViewById(R.id.weatherBackBtn);
        setBackgroundByTime();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
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
            background.setBackground(ContextCompat.getDrawable(this, R.drawable.sunny_night_background));
        } else if (time >= 6 && time < 15) {
            background.setBackground(ContextCompat.getDrawable(this, R.drawable.sunny_afternoon_background));
        } else if (time >= 15 && time < 20) {
            background.setBackground(ContextCompat.getDrawable(this, R.drawable.sunny_sunset_background));
        } else if (time >= 20 && time < 24) {
            background.setBackground(ContextCompat.getDrawable(this, R.drawable.sunny_night_background));
        }
    }

    @SuppressLint("MissingPermission")
    public void setupListeners() {
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
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

    public void fetchLocationData(float lat, float lon) {
        requestQueue = Volley.newRequestQueue(this);

        Log.d(TAG, "새로운 위치가 업데이트되었습니다.");


        String url = "https://api.openweathermap.org/data/2.5/weather?appid=8367e9646913ff43229d761791043b73&units=metric&id=1835848&lang=kr";
        url += "&lat=" + String.valueOf(lat) + "&lon=" + String.valueOf(lon);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "DAta: " + response);
                        try {
                            String locationTemp = response.getJSONObject("main").getString("temp");
                            //String country = response.getJSONObject("sys").getString("country");
                            //String place = response.getString("name") + ", " + country;
                            String temp_highR = response.getJSONObject("main").getString("temp_max");
                            String temp_lowR = response.getJSONObject("main").getString("temp_min");
                            String feels_tempR = response.getJSONObject("main").getString("feels_like");
                            String humidityR = response.getJSONObject("main").getString("humidity");
                            String locationWeather = response.getJSONArray("weather").getJSONObject(0).getString("main");
                            String weatherIconLoad = response.getJSONArray("weather").getJSONObject(0).getString("icon");
                            String weatherDescription = response.getJSONArray("weather").getJSONObject(0).getString("description");
                            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                            tvdescription.setText(weatherDescription);
                            weatherMain.setText(locationWeather);
                            tvTemp_c.setText(locationTemp + "\u2103");
                            tvDate.setText(currentDateTimeString);
                           // tvCity.setText(place);
                            vProgressLayer.setVisibility(View.GONE);
                            temp_high.setText("최고온도" + " " + temp_highR + "\u2103");
                            temp_low.setText("최저온도" + " " + temp_lowR + "\u2103");
                            feels_temp.setText(feels_tempR + "\u2103");
                            humidityId.setText(humidityR + "%");

                            int resID = getResId("icon_" + weatherIconLoad, R.drawable.class);
                            weatherIconR.setImageResource(resID);
                            /*
                            switch(weatherIconLoad){
                                case "01d":
                                    weatherIconR.setImageResource(R.drawable.icon_01d);
                                    break;
                                case "01n":
                                    weatherIconR.setImageResource(R.drawable.icon_01n);
                                    break;
                                case "02d":
                                    weatherIconR.setImageResource(R.drawable.icon_02d);
                                    break;
                                case "02n":
                                    weatherIconR.setImageResource(R.drawable.icon_02n);
                                    break;
                                case "03d":
                                    weatherIconR.setImageResource(R.drawable.icon_03d);
                                    break;
                                case "03n":
                                    weatherIconR.setImageResource(R.drawable.icon_03n);
                                    break;
                                case "04d":
                                    weatherIconR.setImageResource(R.drawable.icon_04d);
                                    break;
                                case "04n":
                                    weatherIconR.setImageResource(R.drawable.icon_04n);
                                    break;
                                case "09d":
                                    weatherIconR.setImageResource(R.drawable.icon_09d);
                                    break;
                                case "09n":
                                    weatherIconR.setImageResource(R.drawable.icon_09n);
                                    break;
                                case "10d":
                                    weatherIconR.setImageResource(R.drawable.icon_10d);
                                    break;
                                case "10n":
                                    weatherIconR.setImageResource(R.drawable.icon_10n);
                                    break;
                                case "11d":
                                    weatherIconR.setImageResource(R.drawable.icon_11d);
                                    break;
                                case "11n":
                                    weatherIconR.setImageResource(R.drawable.icon_11n);
                                    break;
                                case "13d":
                                    weatherIconR.setImageResource(R.drawable.icon_13d);
                                    break;
                                case "13n":
                                    weatherIconR.setImageResource(R.drawable.icon_13n);
                                    break;
                                case "50d":
                                    weatherIconR.setImageResource(R.drawable.icon_50d);
                                    break;
                                case "50n":
                                    weatherIconR.setImageResource(R.drawable.icon_50n);
                                    break;
                                default:
                                    break;
                            }*/

                            if (response.has("rain")) {
                                JSONObject rain_object = response.getJSONObject("rain");
                                if (rain_object.has("1h")) {
                                    rain_1h = rain_object.getString("1h");
                                    rain_1h = String.valueOf(Math.round(Double.valueOf(rain_1h) * 10));
                                    current_rain.setText(rain_1h + "mm");

                                } else if (rain_object.has("3h")) {
                                    rain_3h = rain_object.getString("3h");
                                    rain_3h = String.valueOf(Math.round(Double.valueOf(rain_3h) * 10));
                                    current_rain.setText(rain_3h + "mm");
                                } else {
                                    current_rain.setText("0" + "mm");
                                }
                            } else {
                                current_rain.setText("0" + "mm");
                            }


                            //날씨 아이콘

                        /*

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

*/
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
                     //   Toast.makeText(getApplicationContext(), "check your internet connection", Toast.LENGTH_LONG).show();
                    }
                });

        requestQueue.add(jsonObjectRequest);

    }



    public void fetchFutureLocaionData(float lat, float lon) {
        requestQueue = Volley.newRequestQueue(this);

        Log.d(TAG, "새로운 위치가 업데이트되었습니다.");

        Log.d(TAG, "DAta3: " + lat + lon);

        String url="http://api.openweathermap.org/data/2.5/onecall?appid=8367e9646913ff43229d761791043b73&units=metric&id=1835848&lang=kr";
        url += "&lat="+String.valueOf(lat)+"&lon="+String.valueOf(lon);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray daily_object = response.getJSONArray("daily");

                    for (int i = 1; i < daily_object.length(); i++) {
                        JSONObject rec = response.getJSONArray("daily").getJSONObject(i);
                        JSONObject get_temp = rec.getJSONObject("temp");
                        JSONArray weather_object = rec.getJSONArray("weather");
                        JSONObject weather = weather_object.getJSONObject(0);
                        String icon = weather.getString("icon");

                    switch(i){
                        case 1:
                            String date = rec.getString("dt");
                            long timestamp = Long.parseLong(date);
                            Date d = new java.util.Date(timestamp * 1000L);
                            SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE", Locale.KOREA);
                            sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+9"));
                            date = sdf.format(d);
                            daily_yo1.setText(date);

                            dailyLow = get_temp.getString("min");
                            dlt1.setText(dailyLow + "\u2103");

                            dailyHigh = get_temp.getString("max");
                            dht1.setText(dailyHigh + "\u2103");


                            int resID = getResId("icon_" + icon, R.drawable.class);
                            daily_ico1.setImageResource(resID);
                        case 2:
                            String date2 = rec.getString("dt");
                            long timestamp2 = Long.parseLong(date2);
                            Date d2 = new java.util.Date(timestamp2 * 1000L);
                            SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("EEEE", Locale.KOREA);
                            sdf2.setTimeZone(java.util.TimeZone.getTimeZone("GMT+9"));
                            date2 = sdf2.format(d2);
                            daily_yo2.setText(date2);

                            dailyLow = get_temp.getString("min");
                            dlt2.setText(dailyLow + "\u2103");

                            dailyHigh = get_temp.getString("max");
                            dht2.setText(dailyHigh + "\u2103");

                            int resID2 = getResId("icon_" + icon, R.drawable.class);
                            daily_ico2.setImageResource(resID2);
                        case 3:
                            String date3 = rec.getString("dt");
                            long timestamp3 = Long.parseLong(date3);
                            Date d3 = new java.util.Date(timestamp3 * 1000L);
                            SimpleDateFormat sdf3 = new java.text.SimpleDateFormat("EEEE", Locale.KOREA);
                            sdf3.setTimeZone(java.util.TimeZone.getTimeZone("GMT+9"));
                            date3 = sdf3.format(d3);
                            daily_yo3.setText(date3);

                            dailyLow = get_temp.getString("min");
                            dlt3.setText(dailyLow + "\u2103");

                            dailyHigh = get_temp.getString("max");
                            dht3.setText(dailyHigh + "\u2103");

                            int resID3 = getResId("icon_" + icon, R.drawable.class);
                            daily_ico3.setImageResource(resID3);
                        case 4:
                            String date4 = rec.getString("dt");
                            long timestamp4 = Long.parseLong(date4);
                            Date d4 = new java.util.Date(timestamp4 * 1000L);
                            SimpleDateFormat sdf4 = new java.text.SimpleDateFormat("EEEE", Locale.KOREA);
                            sdf4.setTimeZone(java.util.TimeZone.getTimeZone("GMT+9"));
                            date4 = sdf4.format(d4);
                            daily_yo4.setText(date4);

                            dailyLow = get_temp.getString("min");
                            dlt4.setText(dailyLow + "\u2103");

                            dailyHigh = get_temp.getString("max");
                            dht4.setText(dailyHigh + "\u2103");

                            int resID4 = getResId("icon_" + icon, R.drawable.class);
                            daily_ico4.setImageResource(resID4);
                        case 5:
                            String date5 = rec.getString("dt");
                            long timestamp5 = Long.parseLong(date5);
                            Date d5 = new java.util.Date(timestamp5 * 1000L);
                            SimpleDateFormat sdf5 = new java.text.SimpleDateFormat("EEEE", Locale.KOREA);
                            sdf5.setTimeZone(java.util.TimeZone.getTimeZone("GMT+9"));
                            date5 = sdf5.format(d5);
                            daily_yo5.setText(date5);

                            dailyLow = get_temp.getString("min");
                            dlt5.setText(dailyLow + "\u2103");

                            dailyHigh = get_temp.getString("max");
                            dht5.setText(dailyHigh + "\u2103");

                            int resID5 = getResId("icon_" + icon, R.drawable.class);
                            daily_ico5.setImageResource(resID5);

                    }

                }


                }catch(JSONException e)
                {
                    Log.d(TAG, "error occured: " + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Something went wrong: " + error);
                //Toast.makeText(getApplicationContext(), "check your internet connection", Toast.LENGTH_LONG).show();
            }
        }
        );

        requestQueue.add(jsonObjectRequest);

    }



    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(WeatherActivity.this);
        alertDialog.setTitle("GPS에 연결되지 않았습니다.");
        alertDialog.setMessage("GPS를 활성화하시겠습니까?");

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



    public String getCurrentAddress( double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latitude,longitude,1);
        } catch (IOException ioException) {
            return null;
        } catch (IllegalArgumentException illegalArgumentException) {
            return null;
        }
        if (addresses == null || addresses.size() == 0) {
            return null;
        }

        Address address = addresses.get(0);
        return address.getAddressLine(0);
    }

    public void displayWeather(Context context) {

        float lat = PreferenceManager.getFloat(context,"LATITUDE");
        float lon = PreferenceManager.getFloat(context,"LONGITUDE");

        String address = getCurrentAddress(lat, lon);

        fetchLocationData(lat,lon);
        fetchFutureLocaionData(lat,lon);

        if(address!=null){
            tvCity.setText(PreferenceManager.getString(getApplicationContext(),"CITY"));
        }
        else {
            PreferenceManager.setFloat(getApplicationContext(),"LATITUDE",35F);
            PreferenceManager.setFloat(getApplicationContext(),"LONGITUDE",(float)127F);
            displayWeather(getApplicationContext());

            Toast.makeText(getApplicationContext(),"주소가 잘못되었습니다.",Toast.LENGTH_SHORT);
        }


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

}