package com.example.how_about_camping;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherActivity extends AppCompatActivity {
    Handler handler = new Handler();


    String lat, lon;
    final String TAG = "GPS";

    LocationManager locationManager;
    TextView dlt1, dlt2, dlt3, dlt4, dlt5, dht1, dht2, dht3, dht4, dht5, daily_yo1, daily_yo2, daily_yo3, daily_yo4, daily_yo5, humidityId, current_rain,tvCity, tvLatitude, tvLongitude, tvdescription, weatherMain, tvDate, tvTemp_c, temp_high, temp_low, feels_temp;
    RelativeLayout vProgressLayer;
    RequestQueue requestQueue;

    public static final Integer MY_PERMISSIONS_REQUEST_LOCATION = 0x5;
    ImageView weatherIconR, daily_ico1, daily_ico2,daily_ico3,daily_ico4,daily_ico5;

    private FrameLayout address_find;

    Bitmap bitmap;
    private String rain_1h;
    private String rain_3h;

    private String temp_f;
    private String dailyLow;
    private String dailyHigh;
    private String temp_extra;

    ConstraintLayout background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_main);

          /* daily_yo1 = (TextView)findViewById(R.id.daily_yoil1);
        daily_yo2 = (TextView)findViewById(R.id.daily_yoil2);
        daily_yo3 = (TextView)findViewById(R.id.daily_yoil3);
        daily_yo4 = (TextView)findViewById(R.id.daily_yoil4);
        daily_yo5 = (TextView)findViewById(R.id.daily_yoil5);
        daily_ico1 = (ImageView)findViewById(R.id.daily_image1);
        daily_ico2 = (ImageView)findViewById(R.id.daily_image2);
        daily_ico3 = (ImageView)findViewById(R.id.daily_image3);
        daily_ico4 = (ImageView)findViewById(R.id.daily_image4);
        daily_ico5 = (ImageView)findViewById(R.id.daily_image5);
        dht1 = (TextView)findViewById(R.id.daily_high_temp1);
        dht2 = (TextView)findViewById(R.id.daily_high_temp2);
        dht3 = (TextView)findViewById(R.id.daily_high_temp3);
        dht4 = (TextView)findViewById(R.id.daily_high_temp4);
        dht5 = (TextView)findViewById(R.id.daily_high_temp5);
        dlt1 = (TextView)findViewById(R.id.daily_low_temp1);
        dlt2 = (TextView)findViewById(R.id.daily_low_temp2);
        dlt3 = (TextView)findViewById(R.id.daily_low_temp3);
        dlt4 = (TextView)findViewById(R.id.daily_low_temp4);
        dlt5 = (TextView)findViewById(R.id.daily_low_temp5);

*/
        background = (ConstraintLayout)findViewById(R.id.weatherBackground);
        weatherIconR = (ImageView) findViewById(R.id.weatherIcon);
        current_rain = (TextView) findViewById(R.id.textViewPrecipitation);
        tvCity = (TextView) findViewById(R.id.tvCity);
        // tvLatitude = (TextView) findViewById(R.id.tvLatitude);
        //  tvLongitude = (TextView) findViewById(R.id.tvLongitude);
        tvdescription = (TextView) findViewById(R.id.weather_description);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvTemp_c = (TextView) findViewById(R.id.tvTemp_c);
        vProgressLayer = (RelativeLayout) findViewById(R.id.progressLayer);
        temp_high = (TextView)findViewById(R.id.temp_high);
        temp_low = (TextView)findViewById(R.id.temp_low);
        feels_temp = (TextView)findViewById(R.id.feels_temp);
        weatherMain = (TextView)findViewById(R.id.weather_main);
        humidityId = (TextView)findViewById(R.id.humidity);
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        address_find= findViewById(R.id.search_layout);
        address_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

        ImageButton backBtn = (ImageButton) findViewById(R.id.weatherBackBtn);
        setBackgroundByTime();

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
                fetchLocationData(location);
                //futureFetchLocaionData(location);
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

        Log.d(TAG, "새로운 위치가 업데이트되었습니다.");

        lat = Double.toString(myLocation.getLatitude());
        lon = Double.toString(myLocation.getLongitude());
        Log.d(TAG, "DAta: " + myLocation);


         String url = "https://api.openweathermap.org/data/2.5/weather?appid=8367e9646913ff43229d761791043b73&units=metric&id=1835848&lang=kr";
         url += "&lat=" + String.valueOf(lat) + "&lon=" + String.valueOf(lon);
      //  String url = "https://fcc-weather-api.glitch.me/api/current?lat=" + lat + "&lon=" + lon;
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
                            //   tvLatitude.setText(lat);
                            // tvLongitude.setText(lon);
                            tvDate.setText(currentDateTimeString);
                            tvCity.setText(place);
                            vProgressLayer.setVisibility(View.GONE);
                            temp_high.setText("최고온도"+" "+temp_highR+"\u2103");
                            temp_low.setText("최저온도"+" "+temp_lowR+"\u2103");
                            feels_temp.setText(feels_tempR+"\u2103");
                            humidityId.setText(humidityR + "%");

                            // weatherIconR.setIma

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
                            }

                            //강우량
                            if(response.has("rain")){
                                JSONObject rain_object = response.getJSONObject("rain");
                                if(rain_object.has("1h")){
                                    rain_1h = rain_object.getString("1h");
                                    rain_1h = String.valueOf(Math.round(Double.valueOf(rain_1h)*10));
                                    current_rain.setText(rain_1h + "mm");

                                }
                                else if(rain_object.has("3h")){
                                    rain_3h = rain_object.getString("3h");
                                    rain_3h = String.valueOf(Math.round(Double.valueOf(rain_3h)*10));
                                    current_rain.setText(rain_3h + "mm");
                                }
                                else {
                                    current_rain.setText("0"+ "mm");
                                }
                            }
                            else {
                                current_rain.setText("0"+ "mm");
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
                        Toast.makeText(getApplicationContext(), "check your internet connection", Toast.LENGTH_LONG).show();
                    }
                });

        //add request to queue

        requestQueue.add(jsonObjectRequest);

    }



/*
    private void futureFetchLocaionData(Location location) {
        requestQueue = Volley.newRequestQueue(this);

        Log.d(TAG, "새로운 위치가 업데이트되었습니다.");

        lat = Double.toString(location.getLatitude());
        lon = Double.toString(location.getLongitude());
        Log.d(TAG, "DAta: " + location);

        String url="http://api.openweathermap.org/data/2.5/onecall?appid=8367e9646913ff43229d761791043b73&units=metric&id=1835848&lang=kr";
        url += "&lat="+String.valueOf(lat)+"&lon="+String.valueOf(lon);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{

                   /* for(int i=0;i<response.getJSONArray("hourly").length() && i<36; i+=2){ //2시간 간격 | 18번만 나오게
                        JSONObject rec= response.getJSONArray("hourly").getJSONObject(i);

                        //시간
                        String dt = rec.getString("dt");
                        long timestamp = Long.parseLong(dt);
                        Date date = new java.util.Date(timestamp*1000L);
                        SimpleDateFormat sdf = new java.text.SimpleDateFormat("a h" + "시");
                        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+9"));
                        dt = sdf.format(date);

                        //온도
                        temp_f = rec.getString("temp");
                        temp_f = String.valueOf(Math.round(Double.valueOf(temp_f)));

                        JSONObject weather = rec.getJSONArray("weather").getJSONObject(0);
                        String icon = weather.getString("icon");

                        if(i==0){
                            TextView time_h = (TextView) findViewById(R.id.time_hourly);
                            time_h.setText("지금");
                            TextView temp_h = (TextView) findViewById(R.id.temp_hourly);
                            ImageView image = (ImageView) findViewById(R.id.hourly_image);
                         //   hourlyItemList.add(new HourlyItem("지금",icon,temp_f+"hPa"));
                        }
                        else {
                            TextView time_h = (TextView) findViewById(getResources().getIdentifier("time_hourly"+(i), "id", "패키지"));
                            TextView temp_h = (TextView) findViewById(getResources().getIdentifier("temp_hourly"+(i), "id", "패키지"));
                            ImageView image = (ImageView) findViewById(getResources().getIdentifier("image_hourly"+(i), "id", "패키지"));
                            //hourlyItemList.add(new HourlyItem(dt,icon,temp_f+"hPa"));
                        }
                    }*/

        /*            for(int i=1; i<response.getJSONArray("daily").length(); i++){
                        JSONObject rec = response.getJSONArray("daily").getJSONObject(i);
                        JSONObject get_temp = rec.getJSONObject("temp");

                        int j=0;
                        j++;
                        if(j==1) {
                            //요일
                            String dt = rec.getString("dt");
                            long timestamp = Long.parseLong(dt);
                            Date date = new java.util.Date(timestamp * 1000L);
                            SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE", Locale.KOREAN);
                            sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+9"));
                            dt = sdf.format(date);
                            daily_yo1.setText(dt);

                            //최저기온
                            dailyLow = get_temp.getString("min");
                            dailyLow = String.valueOf(Math.round(Double.valueOf(dailyLow)));
                            dlt1.setText(dailyLow + "\u2103  ");

                            //최고기온
                            dailyHigh = get_temp.getString("max");
                            dailyHigh = String.valueOf(Math.round(Double.valueOf(dailyHigh)));
                            dht1.setText(dailyHigh + "\u2103");
                            //아이콘
                       //     JSONArray weather_object = rec.getJSONArray("weather");
                       //     JSONObject weather = weather_object.getJSONObject(0);
                       //     String icon = weather.getString("icon");

                        }
                        if(j==2) {
                            //요일
                            String dt = rec.getString("dt");
                            long timestamp = Long.parseLong(dt);
                            Date date = new java.util.Date(timestamp * 1000L);
                            SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE", Locale.KOREAN);
                            sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+9"));
                            dt = sdf.format(date);
                        //    daily_yo2.setText(dt);

                            //최저기온
                            dailyLow = get_temp.getString("min");
                          //  dailyLow = String.valueOf(Math.round(Double.valueOf(dailyLow)));
                         //   dlt2.setText(dailyLow + "\u2103");

                            //최고기온
                            dailyHigh = get_temp.getString("max");
                          //  dailyHigh = String.valueOf(Math.round(Double.valueOf(dailyHigh)));
                      //      dht2.setText(dailyHigh + "\u2103");
                            //아이콘
                      //      JSONArray weather_object = rec.getJSONArray("weather");
                       //     JSONObject weather = weather_object.getJSONObject(0);
                       //     String icon = weather.getString("icon");

                        }
                        if(j==3) {
                            //요일
                            String dt = rec.getString("dt");
                            long timestamp = Long.parseLong(dt);
                            Date date = new java.util.Date(timestamp * 1000L);
                            SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE", Locale.KOREAN);
                            sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+9"));
                            dt = sdf.format(date);
                       //     daily_yo3.setText(dt);

                            //최저기온
                            dailyLow = get_temp.getString("min");
                            dailyLow = String.valueOf(Math.round(Double.valueOf(dailyLow)));
                      //      dlt3.setText(dailyLow + "\u2103");

                            //최고기온
                            dailyHigh = get_temp.getString("max");
                            dailyHigh = String.valueOf(Math.round(Double.valueOf(dailyHigh)));
                      //      dht3.setText(dailyHigh + "\u2103");
                            //아이콘
                          //  JSONArray weather_object = rec.getJSONArray("weather");
                          //  JSONObject weather = weather_object.getJSONObject(0);
                         //   String icon = weather.getString("icon");

                        }
                        if(j==4) {
                            //요일
                            String dt = rec.getString("dt");
                            long timestamp = Long.parseLong(dt);
                            Date date = new java.util.Date(timestamp * 1000L);
                            SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE", Locale.KOREAN);
                            sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+9"));
                            dt = sdf.format(date);
                      //      daily_yo4.setText(dt);

                            //최저기온
                            dailyLow = get_temp.getString("min");
                            dailyLow = String.valueOf(Math.round(Double.valueOf(dailyLow)));
                     //       dlt4.setText(dailyLow + "\u2103");

                            //최고기온
                            dailyHigh = get_temp.getString("max");
                            dailyHigh = String.valueOf(Math.round(Double.valueOf(dailyHigh)));
                       //     dht4.setText(dailyHigh + "\u2103");
                            //아이콘
                         //   JSONArray weather_object = rec.getJSONArray("weather");
                         //   JSONObject weather = weather_object.getJSONObject(0);
                         //   String icon = weather.getString("icon");

                        }
                        if(j==5) {
                            //요일
                            String dt = rec.getString("dt");
                            long timestamp = Long.parseLong(dt);
                            Date date = new java.util.Date(timestamp * 1000L);
                            SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE", Locale.KOREAN);
                            sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+9"));
                            dt = sdf.format(date);
                       //     daily_yo5.setText(dt);

                            //최저기온
                            dailyLow = get_temp.getString("min");
                            dailyLow = String.valueOf(Math.round(Double.valueOf(dailyLow)));
                     //       dlt5.setText(dailyLow + "\u2103");

                            //최고기온
                            dailyHigh = get_temp.getString("max");
                            dailyHigh = String.valueOf(Math.round(Double.valueOf(dailyHigh)));
                       //     dht5.setText(dailyHigh + "\u2103");
                            //아이콘
                            //JSONArray weather_object = rec.getJSONArray("weather");
                          //  JSONObject weather = weather_object.getJSONObject(0);
                          //  String icon = weather.getString("icon");

                        }
                  //      TextView yoil=(TextView)findViewById(getResources().getIdentifier("daily_yoil"+(i), "id", "패키지"));
                  //      TextView low=(TextView)findViewById(getResources().getIdentifier("daily_low"+(i), "id", "패키지"));
                 //       TextView high=(TextView)findViewById(getResources().getIdentifier("daily_high"+(i), "id", "패키지"));
                 //       ImageView image=(ImageView)findViewById(getResources().getIdentifier("daily_image"+(i), "id", "패키지"));

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
                Toast.makeText(getApplicationContext(), "check your internet connection", Toast.LENGTH_LONG).show();
            }
        }
        );

        requestQueue.add(jsonObjectRequest);

    }*/



    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
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
}