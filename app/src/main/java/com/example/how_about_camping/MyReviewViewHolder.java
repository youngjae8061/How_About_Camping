package com.example.how_about_camping;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyReviewViewHolder extends RecyclerView.ViewHolder {
    ImageView photo;
    ImageButton img_edit;
    ImageButton img_delete;
    TextView s;
    TextView r;
    TextView t;

    View mView;

    private MyReviewViewHolder.ClickListener mClickListener;

    public MyReviewViewHolder(@NonNull View itemView) {
        super(itemView);

        this.photo = itemView.findViewById(R.id.img_review);
        this.s = itemView.findViewById(R.id.txt_spot);
        this.r = itemView.findViewById(R.id.txt_review);
        this.t = itemView.findViewById(R.id.txt_time);
        this.img_edit = itemView.findViewById(R.id.img_edit);
        this.img_delete = itemView.findViewById(R.id.img_delete);
        mView = itemView;

        img_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListener.onEditlick(view, getAdapterPosition());
            }
        });

        img_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListener.onDeleteClick(view, getAdapterPosition());
            }
        });
    }

    public interface ClickListener{
        void onEditlick(View view, int position);
        void onDeleteClick(View view, int position);
    }

    public void setOnClickListener(MyReviewViewHolder.ClickListener clickListener){
        mClickListener = clickListener;
    }
}
