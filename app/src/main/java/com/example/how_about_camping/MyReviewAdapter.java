package com.example.how_about_camping;

import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MyReviewAdapter extends RecyclerView.Adapter<MyReviewViewHolder> {

    ReviewListActivity reviewListActivity;
    private List<MyReview> arrayList;
    Context context;

    private FirebaseFirestore db = FirebaseFirestore.getInstance(); //파이어베이스 인스턴스
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public MyReviewAdapter(ReviewListActivity reviewListActivity, List<MyReview> arrayList, Context context) {
        this.reviewListActivity = reviewListActivity;
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 실제 리스트뷰가 어댑터에 연결되었을때 view를 최초로 만듬
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_myreview, parent, false);

        MyReviewViewHolder myReviewViewHolder = new MyReviewViewHolder(view);

        myReviewViewHolder.setOnClickListener(new MyReviewViewHolder.ClickListener() {
            @Override
            public void onEditlick(View view, int position) {
                String test = arrayList.get(position).getSpot();
                String test1 = arrayList.get(position).getReview();
                Toast.makeText(reviewListActivity, test+" "+test1+"를 수정할까요?", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(View view, int position) {
                String test = arrayList.get(position).getSpot();
                String test1 = arrayList.get(position).getReview();
                Toast.makeText(reviewListActivity, test+" "+test1+"를 삭제할까요?", Toast.LENGTH_SHORT).show();
            }
        });

        return myReviewViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyReviewViewHolder holder, int position) {
        db.collection("review")
                .whereEqualTo("spot_name", arrayList.get(position).getSpot())
                .whereEqualTo("review", arrayList.get(position).getReview())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot doc : task.getResult()){
                                String filename = String.valueOf(doc.get("id")) + ".png";
                                final StorageReference storageR = storage.getReferenceFromUrl("gs://mobilesw-a40fa.appspot.com").child("images/" + filename);
                                //StorageReference pathReference = storageR.child("images/"+filename);
                                final String imageUrl = String.valueOf(storageR);

                                storageR.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if(task.isSuccessful()){
                                            Glide.with(holder.itemView)
                                                    .load(task.getResult())
                                                    .override(1024,980)
                                                    .into(holder.photo);
                                            Log.d("url", ">>> "+imageUrl);
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
        holder.s.setText(arrayList.get(position).getSpot());
        holder.r.setText(arrayList.get(position).getReview());
        holder.t.setText(arrayList.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
