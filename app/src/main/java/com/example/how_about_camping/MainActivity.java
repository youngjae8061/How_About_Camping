package com.example.how_about_camping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback, Serializable {

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private FirebaseFirestore db; //파이어베이스 인스턴스
    private FirebaseStorage storage;

    Switch switch_sch;
    boolean sch = false;

    static final String apimapKey = "AIzaSyBOCI7VOW4uISKkrUjcV5oRsZU658xFOHI";

    private GoogleMap mMap;
    private Marker currentMarker = null;

    List<Marker> previous_marker = null;
    //테스트중==============================================================================================
    List<String> items = new ArrayList<>();

    private static final String TAG = "drugstoremap";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500;

    // 사용 권한 요청을 구별하기 위해 사용함
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;

    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소

    LatLng currentPosition;

    private FusedLocationProviderClient mFusedLocationClient;//기기의 현재 위치 가져오기
    private LocationRequest locationRequest;
    private Location location;

    private View mLayout;
    private TextView edt_sch;
    // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로가기 버튼을 누를때 표시
    private Toast toast;

    private GeoPoint gp;
    private MarkerOptions markerOptions;
    private double get_latitude, get_longitude;
    SupportMapFragment mapFragment;

    View dialogView;
    ImageView imgReview, img_user;
    TextView txtSpotName, txtReview, txtTime, txt_user;

    String title, snippet, time, map, nick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_sch).setOnClickListener(onClickListener);
        findViewById(R.id.btn_review).setOnClickListener(onClickListener);//리뷰버튼
        findViewById(R.id.imgbtn_logout).setOnClickListener(onClickListener);
        findViewById(R.id.btn_maps).setOnClickListener(onClickListener); // 약국 지도 버튼
        findViewById(R.id.btn_weather).setOnClickListener(onClickListener); // 날씨 버튼
        findViewById(R.id.btn_mypage).setOnClickListener(onClickListener); // 마이페이지 버튼
        findViewById(R.id.btn_reviewlist).setOnClickListener(onClickListener);
        switch_sch = (Switch) findViewById(R.id.switch_sch);

       // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //파이어베이스 객체들 인스턴스 생성
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        previous_marker = new ArrayList<Marker>();
        mLayout = findViewById(R.id.layout_maps);

        CheckState();
        showData();//후기등록한거 제목 전체 띄우기

        switch_sch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                CheckState();
            }
        });

        //로그인상태가 아니라면 로그인 화면으로 전환
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
    }//onCreate()

    private void showData() {
        db.collection("review")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String a = document.getString("spot_name");
                                items.add(a);
                            }
                        } else {
                            //실패했을경우
                        }
                    }
                });
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_sch:
                    if (sch == true) {
                        schReview();
                    } else {
                        schSpot();
                    }
                    break;
                case R.id.btn_review:
                    Intent intent_rv = new Intent(MainActivity.this, ReviewActivity.class);
                    intent_rv.putExtra("latitude", get_latitude);
                    intent_rv.putExtra("longitude", get_longitude);
                    startActivity(intent_rv);
                    break;
                case R.id.imgbtn_logout:
                    FirebaseAuth.getInstance().signOut();
                    startLoginActivity();
                    break;
                case R.id.btn_maps:
                    startMapsActivity();
                    break;
                case R.id.btn_weather:
                    startWeatherActivity();
                    break;
                case R.id.btn_mypage:
                    Intent intent = new Intent(MainActivity.this, MyPageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;
                case R.id.btn_reviewlist:
                    Intent intent2 = new Intent(MainActivity.this, ReviewListActivity.class);
                    intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent2);
                    break;
            }
        }
    };

    private void CheckState() {
        edt_sch = (TextView) findViewById(R.id.edt_sch);
        if (switch_sch.isChecked()) {
            edt_sch.setHint("내용으로 검색하세요");
            sch = true;
        } else {
            edt_sch.setHint("장소명을 검색하세요");
            sch = false;
        }

    }

    //장소명으로 검색
    private void schSpot() {
        mMap.clear();
        final String sch = edt_sch.getText().toString();
        if (sch.length() > 0) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            //Toast.makeText(MainActivity.this,String.valueOf(db.collection("review")),Toast.LENGTH_SHORT).show();
            db.collection("review")
                    .whereEqualTo("spot_name", sch)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    gp = (GeoPoint) document.get("map");
                                    mMap.setOnMarkerClickListener(MainActivity.this);
                                    Log.d(TAG, String.valueOf(document.get("spot_name"))); //!!!!!!!!!!!!!!!!!!

                                    String filename = String.valueOf(document.get("id")) + ".png";
                                    final StorageReference storageRef = storage.getReferenceFromUrl("gs://mobilesw-a40fa.appspot.com").child("images/" + filename);

                                    storageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(MainActivity.this, task.getResult().getLastPathSegment(), Toast.LENGTH_SHORT); //안뜸

                                            } else {
                                                // URL을 가져오지 못하면 토스트 메세지
                                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                    LatLng latLng = new LatLng(gp.getLatitude(), gp.getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));    // 화면이 바라볼 곳은 latlng이다.
                                    mMap.moveCamera(CameraUpdateFactory.zoomTo(19));        // 화면은 15만큼 당겨라?  단계는 1~21까지 있음 숫자가 클수록 자세함
                                    BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.tent);
                                    Bitmap b = bitmapdraw.getBitmap();
                                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 70, false);

                                    markerOptions = new MarkerOptions()
                                            .position(latLng)
                                            .title(String.valueOf(document.get("spot_name")))
                                            .snippet(String.valueOf(document.get("review")))
                                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                                    mMap.addMarker(markerOptions);
                                }
                            } else {
                                startToast("다른 단어로 검색해주세요.");
                            }
                        }
                    });
        } else startToast("검색할 장소를 입력해주세요.");
    }

    //후기내용으로 검색
    private void schReview() {
        mMap.clear();
        final String sch = edt_sch.getText().toString();
        if (sch.length() > 0) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            db.collection("review")
                    .whereEqualTo("review", sch)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    gp = (GeoPoint) document.get("map");

                                    Log.d(TAG, String.valueOf(document.get("review"))); //!!!!!!!!!!!!!!!!!!

                                    String filename = String.valueOf(document.get("id")) + ".png";
                                    final StorageReference storageRef = storage.getReferenceFromUrl("gs://mobilesw-a40fa.appspot.com").child("images/" + filename);

                                    storageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if (task.isSuccessful()) {

                                            } else {
                                                // URL을 가져오지 못하면 토스트 메세지
                                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                    LatLng latLng = new LatLng(gp.getLatitude(), gp.getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));    // 화면이 바라볼 곳은 latlng이다.
                                    mMap.moveCamera(CameraUpdateFactory.zoomTo(19));        // 화면은 15만큼 당겨라?  단계는 1~21까지 있음 숫자가 클수록 자세함
                                    BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.tent);
                                    Bitmap b = bitmapdraw.getBitmap();
                                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 70, false);

                                    markerOptions = new MarkerOptions()
                                            .position(latLng)
                                            .title(String.valueOf(document.get("spot_name")))
                                            .snippet(String.valueOf(document.get("review")))
                                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                                    mMap.addMarker(markerOptions);
                                }
                            } else {
                                startToast("다른 단어로 검색해주세요.");
                            }
                        }
                    });
        } else startToast("검색할 내용을 입력해주세요.");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady :");

        mMap = googleMap;
        mMap.setOnMarkerClickListener(MainActivity.this);

        googleMap.setIndoorEnabled(true);   //실내에서작동
        googleMap.setBuildingsEnabled(true);//건물표시
        googleMap.getUiSettings().setZoomControlsEnabled(true);//UI에서 Zoom 컨트롤하겠다.

        setDefaultLocation();

        // 권한 처리
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        // 권한 여부 확인

        // 권한있음
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            startLocationUpdates();
        } else {
            // 권한없음
            // 권한 거부 시
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();
            } else {
                // 권한 요청
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                Log.d(TAG, "onMapClick :");
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        dialogView = (View) View.inflate(MainActivity.this, R.layout.dialog, null);
        AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);

        img_user = (ImageView) dialogView.findViewById(R.id.img_user);
        imgReview = (ImageView) dialogView.findViewById(R.id.imgReview);
        txtSpotName = (TextView) dialogView.findViewById(R.id.txtSpotName);
        txtReview = (TextView) dialogView.findViewById(R.id.txtReview);
        txtTime = (TextView) dialogView.findViewById(R.id.txtTime);
        txt_user = (TextView) dialogView.findViewById(R.id.txt_user);

        //marker.
        //startToast(String.valueOf(marker.getPosition()));
        title = marker.getTitle();
        snippet = marker.getSnippet();
        map = String.valueOf(marker.getPosition());
        String a = marker.getId();
        //Toast.makeText(this, a, Toast.LENGTH_SHORT).show();
        // 리뷰 검색한 마커 정보 가져오기
        db.collection("review")
                .whereEqualTo("spot_name", title)
                .whereEqualTo("review", snippet)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                time = String.valueOf(document.get("uploadTime"));
                                String filename = String.valueOf(document.get("id")) + ".png";
                                String who = String.valueOf(document.get("who"));
                                final StorageReference storageR = storage.getReferenceFromUrl("gs://mobilesw-a40fa.appspot.com").child("images/" + filename);
                                //StorageReference pathReference = storageR.child("images/"+filename);
                                final String imageUrl = String.valueOf(storageR);

                                storageR.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            // Glide 이용하여 이미지뷰에 로딩
                                            Glide.with(MainActivity.this)
                                                    .load(task.getResult())
                                                    .override(1024, 980)
                                                    .into(imgReview);
                                            Log.d("url1", "후기사진 메세지 : "+imageUrl);

                                            txtTime.setText(time);
                                        } else {
                                            // URL을 가져오지 못하면 토스트 메세지
                                            Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                db.collection("users")
                                        .whereEqualTo("uid", who)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()){
                                                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                                        nick = String.valueOf(documentSnapshot.get("nickName"));
                                                        txt_user.setText(nick);
                                                        Log.d("url2", "가입루트 메세지 : "+String.valueOf(documentSnapshot.get("joinRoot")));
                                                        //if (documentSnapshot.get("joinRoot") == "구글회원가입"){
                                                            //Toast.makeText(MainActivity.this,"들어왔다",Toast.LENGTH_SHORT).show();
                                                            String u = String.valueOf(documentSnapshot.get("photoUri"));
                                                            Log.d("url3", "구글 회원 프로필 uri 메세지 : "+u);
                                                            Glide.with(MainActivity.this)
                                                                    .load(u)
                                                                    .override(1024,980)
                                                                    .into(img_user);
                                                        //}else {
                                                        //if (documentSnapshot.get("joinRoot") == "일반회원가입"){
                                                            //Toast.makeText(MainActivity.this,"들어왔다2",Toast.LENGTH_SHORT).show();
                                                            String filename = String.valueOf(documentSnapshot.get("uid")) + ".png";
                                                            final StorageReference storageR = storage.getReferenceFromUrl("gs://mobilesw-a40fa.appspot.com").child("profiles/" + filename);
                                                            //StorageReference pathReference = storageR.child("images/"+filename);
                                                            final String imageUrl = String.valueOf(storageR);

                                                            storageR.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Uri> task) {
                                                                    if (task.isSuccessful()) {
                                                                        // Glide 이용하여 이미지뷰에 로딩
                                                                        Glide.with(MainActivity.this)
                                                                                .load(task.getResult())
                                                                                .override(1024, 980)
                                                                                .into(img_user);
                                                                        Log.d("url4", "일반 회원 프로필 메세지 : "+imageUrl);

                                                                    } /*else {
                                                                        // URL을 가져오지 못하면 토스트 메세지
                                                                        Toast.makeText(MainActivity.this, "2 "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }*/
                                                                }
                                                            });
                                                        //}
                                                    }
                                                }
                                            }
                                        });
                                BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.tent);
                                Bitmap b = bitmapdraw.getBitmap();
                                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 70, false);
                            }
                        } else {
                            startToast("사진을 못가져왔어요 ㅠ");
                        }
                    }
                });
        txtSpotName.setText(title);
        txtReview.setText(snippet);
        //imgReview.setImageResource();
        dlg.setView(dialogView);
        dlg.setPositiveButton("확인", null);
        dlg.show();
        return true;
    }

    //위치 갱신
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            get_latitude = locationResult.getLastLocation().getLatitude();
            get_longitude = locationResult.getLastLocation().getLongitude();
            mFusedLocationClient.removeLocationUpdates(locationCallback);

            LatLng latLng = new LatLng(get_latitude, get_longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));    // 화면이 바라볼 곳은 latlng이다.
            mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
        }
    };

    private void startLocationUpdates() {
        //GPS권한이 있는지 확인
        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        } else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "startLocationUpdates : 권한 없음");
                return;
            }

            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        if (checkPermission()) {
            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if (mMap != null)
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFusedLocationClient != null) {

            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public String getCurrentAddress(LatLng latlng) {
        //GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //이걸 통해 시작하자마자 현재위치를 바라봄
    public void setDefaultLocation() {

        //기본 위치 서울 37.5665, 126.9780
        LatLng DEFAULT_LOCATION = new LatLng(37.5665, 126.9780);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 권한과 GPS 활성 여부를 확인해주세요";


        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);

    }

    //권한 처리
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        return false;

    }

    //권한 요청 결과 받아옴
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            boolean check_result = true;


            // 권한 허용 체크

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {

                // 권한 허용
                startLocationUpdates();
            } else {
                // 권한 거부
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    // 거부한 경우
                    Snackbar.make(mLayout, "권한이 거부되었습니다. 앱을 다시 실행하여 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();

                } else {
                    // "다시 묻지 않음"을 사용자가 체크하고 거부한 경우
                    Snackbar.make(mLayout, "권한이 거부되었습니다. 설정에서 권한을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }
            }
        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 허용해주세요.");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d(TAG, "onActivityResult : GPS 활성화");


                        needRequest = true;

                        return;
                    }
                }

                break;
        }
    }

    @Override
    public void onBackPressed() {
        // 기존 뒤로가기 버튼의 기능을 막기위해 주석처리 또는 삭제
        // super.onBackPressed();

        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지났으면 Toast Show
        // 2000 milliseconds = 2 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
        // 현재 표시된 Toast 취소
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            toast.cancel();
            moveTaskToBack(true);                        // 태스크를 백그라운드로 이동
            //finishAndRemoveTask();						// 액티비티 종료 + 태스크 리스트에서 지우기
            android.os.Process.killProcess(android.os.Process.myPid());    // 앱 프로세스 종료
        }
    }//onBackPressed()

    public void startMapsActivity() { //약국지도화면으로 이동
        Intent intent = new Intent(this, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void startWeatherActivity() { //날씨화면으로 이동
        Intent intent = new Intent(this, WeatherActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}