package com.ybs.myroute;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

public class SplashScreen extends AppCompatActivity {
    private int timeLeft = 3;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        progressBar = findViewById(R.id.progressBarID);

        Thread myTread = new Thread(){
            @Override
            public void run() {
                while(timeLeft>=0)
                {

                    progressBar.setProgress(progressBar.getMax()-timeLeft);
                    SystemClock.sleep(1000); //Thread.sleep(1000);
                    timeLeft--;
                }
                startActivity(new Intent(SplashScreen.this, MainActivity.class));
                finish();
            }
        };
        myTread.start();
    }
}
