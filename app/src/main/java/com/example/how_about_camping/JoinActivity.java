package com.example.how_about_camping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.List;


public class JoinActivity extends AppCompatActivity {

    private final int GET_GALLERY_IMAGE = 200;

    EditText edt_join_name, edt_join_nickname, edt_join_id, edt_join_pw, edt_join_pwchk, edt_join_phonenumber;
    Button btn_join;
    ImageView img_userprofile;

    private FirebaseAuth mAuth;
    FirebaseFirestore fStore;

    private static final String TAG = "JoinActivity";

    private Uri filePath;

    boolean check_file = false;

    private long now;                           // 현재시간 가져오기
    private Date date;                          // Date 생성
    private SimpleDateFormat time;              // 가져올 형식 정하기
    private String getTime;                     // 시간을 문자형식으로 저장하기위한 객체
    AlertDialog dialog; // 알림창 띄우는 변수

    // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로가기 버튼을 누를때 표시
    private Toast toast;

    String userID, userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        edt_join_name = (EditText)findViewById(R.id.edt_join_name);
        edt_join_nickname = (EditText)findViewById(R.id.edt_join_nickname);
        edt_join_id = (EditText)findViewById(R.id.edt_join_id);
        edt_join_pw = (EditText)findViewById(R.id.edt_join_pw);
        edt_join_pwchk = (EditText)findViewById(R.id.edt_join_pwchk);
        edt_join_phonenumber = (EditText)findViewById(R.id.edt_join_phonenumber);
        btn_join = (Button)findViewById(R.id.btn_join);
        img_userprofile = (ImageView)findViewById(R.id.img_userprofile);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        img_userprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요."),  GET_GALLERY_IMAGE);
            }
        });

        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(edt_join_name.getText().toString().length() == 0){
                    edt_join_name.setError("이름을 입력하세요.");
                    return;
                }
                if(edt_join_nickname.getText().toString().length() == 0){
                    edt_join_nickname.setError("닉네임을 입력하세요.");
                    return;
                }
                if(edt_join_id.getText().toString().length() == 0){
                    edt_join_id.setError("이메일을 입력하세요.");
                    return;
                }
                int tmp;
                if((tmp = edt_join_id.getText().toString().indexOf("@")) > -1){
                    //@가 있다면 인덱스넘버가 -1보다 큼..
                }else{
                    //@의 인덱스 위치가 0보다 작다면 즉, 입력받은 문자에 @가 없다면
                    edt_join_id.setError("이메일 형식이 아닙니다.");
                    return;
                }
                if(!((edt_join_id.getText().toString().contains(".com")) | (edt_join_id.getText().toString().contains(".net"))) ){
                    edt_join_id.setError("이메일 형식이 아닙니다.");
                    return;
                }
                if(edt_join_pw.getText().toString().length() < 6){
                    edt_join_pw.setError("6자 이상 입력하세요.");
                    return;
                }
                if(!(edt_join_pw.getText().toString().equals(edt_join_pwchk.getText().toString()))){
                    AlertDialog.Builder builder = new AlertDialog.Builder(JoinActivity.this);
                    dialog = builder.setMessage("비밀번호가 서로 일치하지 않습니다.")
                            .setPositiveButton("확인",null).create();
                    dialog.show();
                    return;
                }
                if(edt_join_phonenumber.getText().toString().length() == 0){
                    edt_join_phonenumber.setError("전화번호를 입력하세요.");
                    return;
                }
                if(check_file==false){
                    AlertDialog.Builder builder = new AlertDialog.Builder(JoinActivity.this);
                    dialog = builder.setMessage("프로필 사진을 등록하세요.")
                            .setPositiveButton("확인",null).create();
                    dialog.show();
                    return;
                }
                join(edt_join_name.getText().toString(),
                        edt_join_nickname.getText().toString(),
                        edt_join_id.getText().toString(),
                        edt_join_pw.getText().toString(),
                        edt_join_phonenumber.getText().toString());
            }
        });//btn_join.setOnClickListener()
    }//onCreate()

    //활동을 초기화할 때 사용자가 현재 로그인되어 있는지 확인
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }//onStart()

    //결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //request코드가 0이고 OK를 선택했고 data에 뭔가가 들어 있다면
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK  && data != null && data.getData() != null) {
            filePath = data.getData();
            Log.d(TAG, "uri:" + String.valueOf(filePath));
            try {
                //Uri 파일을 Bitmap으로 만들어서 ImageView에 집어 넣는다.
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                img_userprofile.setImageBitmap(bitmap);
                check_file = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //회원가입하는 로직
    private void join(final String name, final String nickname, final String email, final String password, final String phone){
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task .isSuccessful()) {
                    userID = mAuth.getCurrentUser().getUid();
                    String uri = "gs://mobilesw-a40fa.appspot.com/profiles/"+userID+".png";
                    userEmail = mAuth.getCurrentUser().getEmail();
                    DocumentReference documentReference = fStore.collection("users").document(userID);

                    now = System.currentTimeMillis(); // 현재시간 가져오기
                    date = new Date(now);
                    time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   // 시간 형식 - 년 월 일 시 분 초
                    getTime = time.format(date);

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("uid", userID);
                    userMap.put("name", name);
                    userMap.put("nickName", nickname);
                    userMap.put("phone", phone);
                    userMap.put("email", email);
                    userMap.put("joinTime", getTime);
                    userMap.put("photoUri", uri);
                    userMap.put("joinRoot", "일반회원가입");

                    uploadFile(userID);

                    documentReference.set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            //Toast.makeText(JoinActivity.this, "가입을 환영합니다!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "successed. user Profile is created for" + userID);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("error", e.getMessage());
                            Toast.makeText(JoinActivity.this, "오류가 발생했습니다!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else{
                    Toast.makeText(JoinActivity.this, "회원가입에 실패했습니다." + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }//join()

    //upload the file    // 추가
    private void uploadFile(String id) {
        //업로드할 파일이 있으면 수행
        if (check_file) {
            //업로드 진행 Dialog 보이기
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("업로드중...");
            progressDialog.show();

            //storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            //Unique한 파일명을
            String filename = id + ".png";
            //storage 주소와 폴더 파일명을 지정해 준다.
            StorageReference storageRef = storage.getReferenceFromUrl("gs://mobilesw-a40fa.appspot.com/profiles").child(filename);
            storageRef.putFile(filePath)
                    //성공시
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            Toast.makeText(JoinActivity.this, "가입을 환영합니다!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "successed. user Profile is created for" + userID);
                        }
                    })
                    //실패시
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("error", e.getMessage());
                            Toast.makeText(JoinActivity.this, "오류가 발생했습니다!", Toast.LENGTH_SHORT).show();
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
        }
    }
}