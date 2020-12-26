package com.example.how_about_camping;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MyPageActivity extends AppCompatActivity {

    private TextView txt_mynickname, txt_myid;
    private ImageView img_myphoto;

    private FirebaseAuth fAuth;
    private FirebaseFirestore firestore;

    String nick, email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        txt_mynickname = findViewById(R.id.txt_mynickname);
        txt_myid = findViewById(R.id.txt_myid);
        img_myphoto = findViewById(R.id.img_myphoto);

        firestore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        my_data();
    }

    private void my_data() {
        // user 컬랙션 구분 uid별
        String uid = fAuth.getCurrentUser().getUid();
        firestore.collection("users")
                .whereEqualTo("uid", uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                String u = String.valueOf(documentSnapshot.get("photoUri"));
                                nick = String.valueOf(documentSnapshot.get("nickName"));
                                email = String.valueOf(documentSnapshot.get("email"));
                                Log.d("url", "사용자 고유 번호 메세지 : "+uid);
                                Log.d("url", "프로필 url 메세지 : "+u);
                                Glide.with(MyPageActivity.this)
                                        .load(u)
                                        .override(1024,980)
                                       .into(img_myphoto);
                            }
                            txt_mynickname.setText(nick);
                            txt_myid.setText(email);
                        }
                    }
                });
    }

}