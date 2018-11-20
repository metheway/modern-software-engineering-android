package com.example.ourapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Activity_Transform extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__transform);

        //button trans
        Button trans_trans = (Button)findViewById(R.id.trans_trans);
        trans_trans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //trans func();
                Intent trans_next_1 = new Intent(Activity_Transform.this,ResultActivity.class);
                //trans_next_1.putExtra("path",path);
                startActivity(trans_next_1);
            }
        });


        //button color
        Button trans_color = (Button)findViewById(R.id.trans_color);
        trans_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }
}
