package com.example.instantchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private Button stRegBtn;
    private Button stLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        stRegBtn =(Button) findViewById(R.id.reg_btn);
        stLoginBtn =(Button) findViewById(R.id.login_btn);

        stRegBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent reg_intent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(reg_intent);
            }
        });

        stLoginBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent login_intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(login_intent);
            }
        });

    }
}
