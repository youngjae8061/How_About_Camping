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
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class ReviewActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = "ReveiewActivity";//추가

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    //progress dialog
    ProgressDialog pd;

    boolean check_file = false, check_date = false;

    EditText edt_spot_name, edt_review;
    Button btn_upload;

    private Button btChoose; // 추가
    private ImageView ivPreview; //추가

    private Uri filePath; // 추가

    Intent intent;
    double latitude_intent, longitude_intent, latitude, longitude;
    TextView textView, textView2, textView3;

    LocationManager locationManager;

    private long now;                           // 현재시간 가져오기
    private Date date;                          // Date 생성
    private SimpleDateFormat time;              // 가져올 형식 정하기
    private String getTime;                     // 시간을 문자형식으로 저장하기위한 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        btChoose = (Button) findViewById(R.id.btChoose);// 추가
        ivPreview = (ImageView) findViewById(R.id.iv_preview);// 추가

        intent = getIntent();
        latitude_intent = intent.getDoubleExtra("latitude", 0);
        longitude_intent = intent.getDoubleExtra("longitude", 0);
        edt_spot_name = (EditText) findViewById(R.id.edt_spot_name);
        edt_review = (EditText) findViewById(R.id.edt_review);
        btn_upload = (Button) findViewById(R.id.btn_upload);

        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
        //textView3.setText(Double.toString(latitude_intent));
        //textView4.setText(Double.toString(longitude_intent));

        //파이어베이스 파이어스토어
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        //progress dialog
        pd = new ProgressDialog(this);

        ivPreview.setEnabled(false);

        getLocation();

        btChoose.setOnClickListener(new View.OnClickListener() { //btchoose 추가
            @Override
            public void onClick(View view) {
                //이미지를 선택
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요."), 0);

            }
        });

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String spot_name = edt_spot_name.getText().toString().trim();
                String review = edt_review.getText().toString().trim();
                GeoPoint gp = new GeoPoint(latitude_intent, longitude_intent);
                String id = UUID.randomUUID().toString();
                String url = "gs://mobilesw-a40fa.appspot.com/images/"+id+".png";

                //Toast.makeText(ReviewActivity.this, spot_name, Toast.LENGTH_SHORT).show();
                //Toast.makeText(ReviewActivity.this, review, Toast.LENGTH_SHORT).show();
                //Toast.makeText(ReviewActivity.this, "1단계성공", Toast.LENGTH_SHORT).show();
                uploadData(spot_name, review, gp, id, url);
                uploadFile(id);
            }
        });
    }

    //결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //request코드가 0이고 OK를 선택했고 data에 뭔가가 들어 있다면
        if (requestCode == 0 && resultCode == RESULT_OK) {
            filePath = data.getData();
            Log.d(TAG, "uri:" + String.valueOf(filePath));
            try {
                //Uri 파일을 Bitmap으로 만들어서 ImageView에 집어 넣는다.
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                ivPreview.setImageBitmap(bitmap);
                check_file = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //upload the file    // 추가
    private void uploadFile(String id) {
        //업로드할 파일이 있으면 수행
        if (check_date == true & check_file==true) {
            //업로드 진행 Dialog 보이기
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("업로드중...");
            progressDialog.show();

            //storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            //Unique한 파일명을
            String filename = id + ".png";
            //storage 주소와 폴더 파일명을 지정해 준다.
            StorageReference storageRef = storage.getReferenceFromUrl("gs://mobilesw-a40fa.appspot.com/images").child(filename);
            storageRef.putFile(filePath)
                    //성공시
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                            Toast.makeText(getApplicationContext(), "업로드 완료!", Toast.LENGTH_SHORT).show();
                            intent = new Intent(ReviewActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    //실패시
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "업로드 실패!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    //진행중
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests")
                            double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            //dialog에 진행률을 퍼센트로 출력해 준다
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "% ...");
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "사진 업로드 해주세요!", Toast.LENGTH_SHORT).show();
            textViewNULLCheck();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        //Toast.makeText(ReviewActivity.this, "1단계성공", Toast.LENGTH_SHORT).show();
        try {
            //Toast.makeText(ReviewActivity.this, "2단계성공", Toast.LENGTH_SHORT).show();
            //pd.setTitle("현재위치 주소 확인중...");
            //pd.show();
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, (android.location.LocationListener) ReviewActivity.this);

            //pd.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //Toast.makeText(this, ""+location.getLatitude()+","+location.getLongitude(), Toast.LENGTH_SHORT).show();
        //좌표를 주소로 변환...!
        try {
            Geocoder geocoder = new Geocoder(ReviewActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String address = addresses.get(0).getAddressLine(0);

            //pd.dismiss();
            textView3.setText(address);
        } catch (Exception e) {
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
    private void uploadData(String sp_n, String rv, GeoPoint geoPoint, String id, String url) {

        //빈칸 여부 체크
        textViewNULLCheck();
        String me = fAuth.getCurrentUser().getUid();
        now = System.currentTimeMillis(); // 현재시간 가져오기
        date = new Date(now);
        time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   // 시간 형식 - 년 월 일 시 분 초
        getTime = time.format(date);

        if(check_date == true & check_file==true) {
            ivPreview.setEnabled(true);
            //정보 해쉬맵
            Map<String, Object> infoMap = new HashMap<>();
            infoMap.put("who", me);
            infoMap.put("spot_name", sp_n);
            infoMap.put("review", rv);
            infoMap.put("map", geoPoint);
            infoMap.put("id", id);      // 랜덤번호 생성하여 중복 방지
            infoMap.put("url", url);
            infoMap.put("uploadTime", getTime);

            //파이어스토어에 등록
            fStore.collection("review").document(id)
                    .set(infoMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //검색에 성공하였을 경우 실행
                            pd.dismiss();
                            Toast.makeText(ReviewActivity.this, "등록되었습니다!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //검색에 실패하였을 경우 실행
                            pd.dismiss();
                            //오류메시지 get
                            Log.d("error", e.getMessage());
                            Toast.makeText(ReviewActivity.this, "오류가 발생했습니다!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }else{
            Toast.makeText(ReviewActivity.this, "사진 업로드 해주세요!", Toast.LENGTH_SHORT).show();
            textViewNULLCheck();
        }
    }

    private void textViewNULLCheck(){
        if (edt_spot_name.getText().toString().trim().equals("")) {
            edt_spot_name.setError("장소명을 입력해주세요.");
            return;
        }
        if (edt_review.getText().toString().trim().equals("")) {
            edt_review.setError("후기를 입력해주세요.");
            return;
        }
        check_date = true;
    }
}