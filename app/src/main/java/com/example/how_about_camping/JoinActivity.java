package com.example.how_about_camping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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

import java.io.IOException;
import java.util.List;


public class JoinActivity extends AppCompatActivity {

    EditText edt_join_name, edt_join_id, edt_join_pw, edt_join_pwchk, edt_join_phonenumber;
    Button btn_join;
    private FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    private static final String TAG = "JoinActivity";

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
        edt_join_id = (EditText)findViewById(R.id.edt_join_id);
        edt_join_pw = (EditText)findViewById(R.id.edt_join_pw);
        edt_join_pwchk = (EditText)findViewById(R.id.edt_join_pwchk);
        edt_join_phonenumber = (EditText)findViewById(R.id.edt_join_phonenumber);
        btn_join = (Button)findViewById(R.id.btn_join);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edt_join_name.getText().toString().length() == 0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(JoinActivity.this);
                    dialog = builder.setMessage("이름은 빈칸일 수 없습니다.")
                            .setPositiveButton("확인",null).create();
                    dialog.show();
                    return;
                }
                if(edt_join_id.getText().toString().length() == 0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(JoinActivity.this);
                    dialog = builder.setMessage("아이디는 빈칸일 수 없습니다.")
                            .setPositiveButton("확인",null).create();
                    dialog.show();
                    return;
                }
                if(edt_join_pw.getText().toString().length() < 6){
                    AlertDialog.Builder builder = new AlertDialog.Builder(JoinActivity.this);
                    dialog = builder.setMessage("비밀번호 6자리 이상 입력해주세요.")
                            .setPositiveButton("확인",null).create();
                    dialog.show();
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(JoinActivity.this);
                    dialog = builder.setMessage("전화번호는 빈칸일 수 없습니다.")
                            .setPositiveButton("확인",null).create();
                    dialog.show();
                    return;
                }
                join(edt_join_name.getText().toString(), edt_join_id.getText().toString(), edt_join_pw.getText().toString(), edt_join_phonenumber.getText().toString());


                /*
                if(edt_join_id.getText().toString().length() > 0 && edt_join_pw.getText().toString().length() > 5){
                    if(edt_join_pw.getText().toString().equals(edt_join_pwchk.getText().toString())){
                        join(edt_join_id.getText().toString(), edt_join_pw.getText().toString());
                    }else{
                        Toast.makeText(JoinActivity.this, "비밀번호가 서로 일치하지 않습니다.",
                                Toast.LENGTH_SHORT).show();
                    }
                }else{

                    //Toast.makeText(JoinActivity.this, "아이디나 비밀번호(6자리이상) 다시 확인해주세요.",
                    //        Toast.LENGTH_SHORT).show();
                }*/
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

    //회원가입하는 로직
    private void join(final String name, final String email, final String password, final String phone){/*
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(JoinActivity.this, "가입을 환영합니다!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            //Toast.makeText(JoinActivity.this, task.getException().toString(),
                            //        Toast.LENGTH_SHORT).show();
                            AlertDialog.Builder builder = new AlertDialog.Builder(JoinActivity.this);
                            dialog = builder.setMessage(task.getException().toString())
                                    .setPositiveButton("확인",null).create();
                            dialog.show();
                            return;
                        }

                        // ...
                    }
                });*/

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task .isSuccessful()) {
                    Toast.makeText(JoinActivity.this, "가입을 환영합니다!", Toast.LENGTH_SHORT).show();
                    userID = mAuth.getCurrentUser().getUid();
                    userEmail = mAuth.getCurrentUser().getEmail();
                    DocumentReference documentReference = fStore.collection("users").document(userID);

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("name", name);
                    userMap.put("email", email);
                    userMap.put("pwd", password);
                    userMap.put("phone", phone);

                    documentReference.set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "successed. user Profile is created for" + userID);
                        }
                    });
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                } else{
                    Toast.makeText(JoinActivity.this, "회원가입에 실패했습니다." + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        
    }//join()
}