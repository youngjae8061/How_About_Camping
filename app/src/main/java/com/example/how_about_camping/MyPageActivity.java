package com.example.how_about_camping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MyPageActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private FragMy fragMy;
    private FragReviewList fragReviewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        bottomNavigationView = findViewById(R.id.btm_bar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.menu_home:
                        setFrag(0);
                        break;
                    case R.id.menu_reviewlist:
                        setFrag(1);
                        break;
                }
                return true;
            }
        });
        fragMy = new FragMy();
        fragReviewList = new FragReviewList();
        setFrag(0); //Fragment 첫 화면에 무엇을 띄울건지 지정
    }


    //Fragment 교체가 일어나느 메서드
    private void setFrag(int n ){
        fragmentManager = getSupportFragmentManager();              //
        fragmentTransaction = fragmentManager.beginTransaction();   // 실제로 fragment 교체가 이루어 질때 fragment를 가져와서 getTransaction을 하기위한 함수
        switch (n){
            case 0:
                fragmentTransaction.replace(R.id.frame, fragMy);
                fragmentTransaction.commit(); //저장
                break;
            case 1:
                fragmentTransaction.replace(R.id.frame, fragReviewList);
                fragmentTransaction.commit(); //저장
                break;
        }
    }
}