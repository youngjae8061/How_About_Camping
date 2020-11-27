package com.example.how_about_camping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class ReviewActivity extends AppCompatActivity implements LocationListener {

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    //progress dialog
    ProgressDialog pd;


    EditText edt_spot_name, edt_review;
    Button btn_upload;
    Intent intent;
    double latitude_intent, longitude_intent, latitude, longitude;
    TextView textView, textView2, textView3;

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        intent = getIntent();
        latitude_intent = intent.getDoubleExtra("latitude", 0);
        longitude_intent = intent.getDoubleExtra("longitude", 0);
        edt_spot_name = (EditText)findViewById(R.id.edt_spot_name);
        edt_review = (EditText)findViewById(R.id.edt_review);
        btn_upload = (Button)findViewById(R.id.btn_upload);

        textView2 = (TextView)findViewById(R.id.textView2);
        textView3 = (TextView)findViewById(R.id.textView3);
        //textView3.setText(Double.toString(latitude_intent));
        //textView4.setText(Double.toString(longitude_intent));

        //파이어베이스 파이어스토어
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        //progress dialog
        pd = new ProgressDialog(this);

        /*if(ContextCompat.checkSelfPermission(ReviewActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(ReviewActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }*/

        getLocation();

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String spot_name = edt_spot_name.getText().toString().trim();
                String review = edt_review.getText().toString().trim();
                GeoPoint gp = new GeoPoint(latitude_intent, longitude_intent);


                //Toast.makeText(ReviewActivity.this, spot_name, Toast.LENGTH_SHORT).show();
                //Toast.makeText(ReviewActivity.this, review, Toast.LENGTH_SHORT).show();
                //Toast.makeText(ReviewActivity.this, "1단계성공", Toast.LENGTH_SHORT).show();
                uploadData(spot_name, review, gp);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        //Toast.makeText(ReviewActivity.this, "1단계성공", Toast.LENGTH_SHORT).show();
        try{
            //Toast.makeText(ReviewActivity.this, "2단계성공", Toast.LENGTH_SHORT).show();
            //pd.setTitle("현재위치 주소 확인중...");
            //pd.show();
            locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,5, (android.location.LocationListener) ReviewActivity.this);

            //pd.dismiss();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //Toast.makeText(this, ""+location.getLatitude()+","+location.getLongitude(), Toast.LENGTH_SHORT).show();

        try{
            Geocoder geocoder = new Geocoder(ReviewActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String address = addresses.get(0).getAddressLine(0);

            //pd.dismiss();
            textView3.setText(address);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    //파이어스토어에 후기를 올려주는 메소드
    private void uploadData(String sp_n, String rv, GeoPoint geoPoint) {

        //set title of progress bar
        //pd.setTitle("후기 등록중...");
        //show progress bar when user clike save button
        //pd.show();

        String id = UUID.randomUUID().toString();

        //빈칸 여부 체크
        if (sp_n.equals("")) {
            Toast.makeText(ReviewActivity.this, "지역명을..?", Toast.LENGTH_SHORT).show();
            edt_spot_name.setError("지역명을 입력해주세요.");
            return;
        }

        if (rv.equals("")) {
            edt_review.setError("후기를 입력해주세요.");
            return;
        }

        //강의정보 해쉬맵
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("spot_name", sp_n);
        infoMap.put("review", rv);
        infoMap.put("map", geoPoint);

        fStore.collection("review").document(id)
                .set(infoMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //검색에 성공하였을 경우 실행
                        pd.dismiss();
                        Toast.makeText(ReviewActivity.this, "등록되었습니다!",Toast.LENGTH_SHORT).show();
                        intent = new Intent(ReviewActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //검색에 실페하였을 경우 실행
                        pd.dismiss();
                        //오류메시지 get
                        Log.d("error",e.getMessage());
                        Toast.makeText(ReviewActivity.this, "오류가 발생했습니다!",Toast.LENGTH_SHORT).show();
                    }
                });
    }
}