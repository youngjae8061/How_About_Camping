package com.example.how_about_camping;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyReviewAdapter extends RecyclerView.Adapter<MyReviewViewHolder> {
    private static final String TAG = "ReveiewActivity";
    ReviewListActivity reviewListActivity;
    private List<MyReview> arrayList;
    Context context;
    View dialogView;
    EditText edt_spot_nameupdate, edt_reviewupdate;
    Button btn_choice;
    ImageView img_preview;
    String forDelete;

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
                dialogView = (View) View.inflate(parent.getContext(), R.layout.dialog_reviewupdate, null);
                AlertDialog.Builder dlg = new AlertDialog.Builder(parent.getContext());

                edt_spot_nameupdate = (EditText) dialogView.findViewById(R.id.edt_spot_nameupdate);
                edt_reviewupdate = (EditText) dialogView.findViewById(R.id.edt_reviewupdate);
                btn_choice = (Button) dialogView.findViewById(R.id.btn_choice);
                img_preview = (ImageView) dialogView.findViewById(R.id.img_preview);

                btn_choice.setOnClickListener(new View.OnClickListener() { //btchoose 추가
                    @Override
                    public void onClick(View view) {
                        //이미지를 선택
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        context = parent.getContext();

                        ((Activity)context).startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요."), 0);

                    }
                });

                // firestore에 접근하여 해당 리뷰의
                // 글을 띄우고 수정할건지 말건지를 ...
                //db.collection()



                dlg.setView(dialogView);
                // setPositiveButton listener에 listener만들기
                dlg.setPositiveButton("확인", null);
                dlg.setNegativeButton("취소", null);
                dlg.show();
            }

            @Override
            public void onDeleteClick(View view, int position) {
                dialogView = (View) View.inflate(parent.getContext(), R.layout.dialog_reviewupdate, null);
                AlertDialog.Builder dlg = new AlertDialog.Builder(parent.getContext());
                String title = arrayList.get(position).getSpot();
                String rev = arrayList.get(position).getReview();
                //Toast.makeText(reviewListActivity, title+" "+rev+"를 삭제할까요?", Toast.LENGTH_SHORT).show();
                dlg.setMessage("해당 게시글을 삭제할까요?");
                // setPositiveButton listener에 listener만들기
                dlg.setNegativeButton("취소", null);
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Toast.makeText(reviewListActivity, "!!! " + title, Toast.LENGTH_SHORT).show();
                        db.collection("review")
                                .whereEqualTo("spot_name", title)
                                .whereEqualTo("review", rev)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()){
                                            //Toast.makeText(reviewListActivity, "111 " + title, Toast.LENGTH_SHORT).show();
                                            for (QueryDocumentSnapshot doc : task.getResult()){
                                                //Toast.makeText(reviewListActivity, "222 " + String.valueOf(doc.get("id")), Toast.LENGTH_SHORT).show();
                                                db.collection("review").document(String.valueOf(doc.get("id"))).delete();
                                                //forDelete = String.valueOf(doc.get("id"));
                                            }
                                        }
                                    }
                                });
                        //Log.d(TAG, "DocumentSnapshot!" + forDelete);
                        //Toast.makeText(reviewListActivity, "222 " + forDelete, Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.show();
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
}
