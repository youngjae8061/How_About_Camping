package com.example.how_about_camping;

import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReviewListActivity extends AppCompatActivity {

    private List<MyReview> myReviewList = new ArrayList<>();
    private RecyclerView recyclerView;
    private Context context;

    private RecyclerView.LayoutManager layoutManager;

    private FirebaseAuth fAuth;
    private FirebaseFirestore firestore;
    private MyReviewAdapter myReviewAdapter;

    private ImageButton img_edit, img_delete;

    private static final String TAG = "drugstoremap";
    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviewlist);

        firestore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        img_edit = findViewById(R.id.img_edit);
        img_delete = findViewById(R.id.img_delete);

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);// 리사이클러뷰 기존 성능 강화
        //recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        layoutManager = new LinearLayoutManager(ReviewListActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.scrollToPosition(0);
        myReviewAdapter = new MyReviewAdapter(ReviewListActivity.this, myReviewList, context);
        //recyclerView.setAdapter(myReviewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        showData();
    }

    private void showData() {
        String uid = fAuth.getUid();
        firestore.collection("review")
                .whereEqualTo("who", uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(DocumentSnapshot doc : task.getResult()){
                            MyReview myReview = new MyReview(
                                    String.valueOf(doc.get("url")),
                                    String.valueOf(doc.get("spot_name")),
                                    String.valueOf(doc.get("review")),
                                    String.valueOf(doc.get("uploadTime"))
                            );
                            Log.d(TAG, ">>> "+String.valueOf(doc.get("url"))+", "
                                    +String.valueOf(doc.get("spot_name"))+", "
                                    +String.valueOf(doc.get("review"))+", "
                                    +String.valueOf(doc.get("uploadTime")));
                            myReviewList.add(myReview);
                        }
                        myReviewAdapter = new MyReviewAdapter(ReviewListActivity.this, myReviewList, context);
                        recyclerView.setAdapter(myReviewAdapter);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ReviewListActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

    }
}
