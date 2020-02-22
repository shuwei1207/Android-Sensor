package com.example.chuchun.ball_bubble;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView test;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button botton0 = (Button)findViewById(R.id.button0);

        botton0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //System.out.println("Click!!");
                Intent i = new Intent();
                i.setClass(MainActivity.this, SurfaceViewActivity.class);
                //System.out.println("New Intent!");
                startActivity(i);
            }
        });

    }

}
