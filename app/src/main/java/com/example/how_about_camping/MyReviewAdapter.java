package com.example.how_about_camping;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MyReviewAdapter extends RecyclerView.Adapter<MyReviewAdapter.MyReviewViewHolder> {

    ReviewListActivity reviewListActivity;
    private List<MyReview> arrayList;
    Context context;

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

        return myReviewViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyReviewViewHolder holder, int position) {
        Glide.with(holder.itemView)
                .load(arrayList.get(position).getUri())
                .into(holder.photo);
        holder.s.setText(arrayList.get(position).getSpot());
        holder.r.setText(arrayList.get(position).getReview());
        holder.t.setText(arrayList.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView photo;
        TextView s;
        TextView r;
        TextView t;
        View mView;
        public MyReviewViewHolder(@NonNull View itemView) {
            super(itemView);

            this.photo = itemView.findViewById(R.id.img_review);
            this.s = itemView.findViewById(R.id.txt_spot);
            this.r = itemView.findViewById(R.id.txt_review);
            this.t = itemView.findViewById(R.id.txt_time);
            mView = itemView;
        }
    }
}
