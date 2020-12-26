package com.example.how_about_camping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 10;   // 구글 로그인한다는 것을 구분하기위한 코드
    private SignInButton login_google;          // 구글 로그인 버튼
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;                 // 파이어 베이스 인증 객체
    private GoogleApiClient googleApiClient;    // 구글 API 클라이언트 객체
    private FirebaseFirestore fStore;           // 파이어 스토어 객체
    private long now;                           // 현재시간 가져오기
    private Date date;                          // Date 생성
    private SimpleDateFormat time;              // 가져올 형식 정하기
    private String getTime;                     // 시간을 문자형식으로 저장하기위한 객체

    EditText edt_id, edt_pw;
    TextView txt_join;
    Button btn_login;
    private static final String TAG = "LoginActivity";
    // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로가기 버튼을 누를때 표시
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login_google = (SignInButton)findViewById(R.id.login_google);
        mAuth = FirebaseAuth.getInstance(); // 파이어베이스 인증 객체 초기화
        fStore = FirebaseFirestore.getInstance(); // 파이어스토어 객체 초기화
        edt_id = (EditText)findViewById(R.id.edt_id);
        edt_pw = (EditText)findViewById(R.id.edt_pw);
        btn_login = (Button)findViewById(R.id.btn_login);
        txt_join = (TextView)findViewById(R.id.txt_join);

        //로그인상태라면 메인 화면으로 전환
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        }

        // Configure Google Sign In 구글 로그인 인증하기
        // SignInButton을 이용할때 사용하는 옵션 정리
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // 이 사람이 구글 사용자니??
        login_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);   // 해당 액티비티가 종료되었을때 onActivityResult에 코드값을 보낸다.
            }
        });//login_google.setOnClickListener()

        // 로그인 버튼 클릭
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAuth.signInWithEmailAndPassword(edt_id.getText().toString(), edt_pw.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final ProgressDialog mDialog = new ProgressDialog(LoginActivity.this);
                            mDialog.setMessage("로그인 중...");
                            mDialog.show();
                            //Toast.makeText(LoginActivity.this, edt_id.getText().toString()+" 님 로그인 성공", Toast.LENGTH_SHORT).show();
                            //startActivity(new Intent(getApplicationContext(),MainActivity.class));

                            startMainActivity();

                        } else
                            Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        // 회원가입 버튼 클릭시 회원가입 화면으로 넘어감
        txt_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), JoinActivity.class);
                startActivity(intent);
            }
        });

    }//onCreate()

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){
                //인증 결과가 성공이라면
                GoogleSignInAccount account = result.getSignInAccount();    // account라는 데이터는 구글 로그인 정보를 담고잇다.(닉네임, 프로필사진Uri, 이메일 주소 등등...)
                resultLogin(account);   //로그인 결과 값 출력 수행하는 메서드

            }
        }
    }//onActivityResult()

    private void resultLogin(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);   //해당 사용자의 idToken을 가져와라
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //로그인이 성공했으면
                            now = System.currentTimeMillis(); // 현재시간 가져오기
                            date = new Date(now);
                            time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   // 시간 형식 - 년 월 일 시 분 초
                            getTime = time.format(date);

                            String userID = mAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users").document(userID);

                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("uid",  userID);
                            userMap.put("name", account.getGivenName());    //getFamilyName() 성, getGivenName() 이름
                            userMap.put("nickName", account.getDisplayName());
                            userMap.put("phone", "");
                            userMap.put("email", account.getEmail());
                            userMap.put("joinTime", getTime);
                            userMap.put("photoUri", String.valueOf(account.getPhotoUrl()));
                            userMap.put("joinRoot", "구글회원가입");

                            documentReference.set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                    //Toast.makeText(LoginActivity.this, "가입을 환영합니다!", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "successed. user Profile is created for" + userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("error", e.getMessage());
                                    Toast.makeText(LoginActivity.this, "오류가 발생했습니다!", Toast.LENGTH_SHORT).show();
                                }
                            });
                            Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                            startMainActivity();
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(),"로그인에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
        });
    }//resultLogin()

    private void startMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
            moveTaskToBack(true);						// 태스크를 백그라운드로 이동
            //finishAndRemoveTask();						// 액티비티 종료 + 태스크 리스트에서 지우기
            android.os.Process.killProcess(android.os.Process.myPid());	// 앱 프로세스 종료
        }
    }//onBackPressed()
}