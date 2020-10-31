package com.example.how_about_camping;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        try{
            Thread.sleep(3000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        // 이 액티비티 다음에 실행시킬 액티비티는 로그인액티비티
        startActivity(new Intent(this,LoginActivity.class));
        finish();

        // 스플래시로 띄울 이미지는 drawable - splash.png
        // 이미지 맵핑을 위해 value - styles.xml로 가서 작성!

    }//onCreate()
}