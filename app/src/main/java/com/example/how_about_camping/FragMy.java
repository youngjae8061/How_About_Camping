package com.example.how_about_camping;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class FragMy extends Fragment {

    private View view;
    private TextView txt_mynickname, txt_myid;
    private ImageView img_myphoto;

    private FirebaseAuth fAuth;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_my, container, false);

        Toast.makeText(getActivity(), "0 hi", Toast.LENGTH_SHORT).show();

        txt_mynickname = view.findViewById(R.id.txt_mynickname);
        txt_myid = view.findViewById(R.id.txt_myid);
        img_myphoto = view.findViewById(R.id.img_myphoto);
        firestore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        Toast.makeText(getActivity(), "1 "+fAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
        Toast.makeText(getActivity(),"2 "+fAuth.getCurrentUser().getUid(), Toast.LENGTH_SHORT).show();

        my_Settings();

        return view;
    }

    private void my_Settings() {
        String uid = fAuth.getCurrentUser().getUid();
        DocumentReference documentReference = firestore.collection("users").document(uid);

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                Uri uri = Uri.parse(value.getString("photoUri"));
                txt_mynickname.setText(value.getString("nickName"));
                txt_myid.setText(value.getString("email"));
                img_myphoto.setImageURI(uri);
                Glide.with(getActivity())
                        .load(uri)
                        .override(1024, 980)
                        .into(img_myphoto);
            }
        });
    }
}
