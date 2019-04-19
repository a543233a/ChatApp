package com.example.instantchat;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class SearchActivity extends AppCompatActivity {

    private Button searchBtn;
    private TextInputLayout searchName;

    private DatabaseReference UserStatusDatebase;
    private FirebaseAuth mAuth;

    private Toolbar MainToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        MainToolbar=(Toolbar) findViewById(R.id.users_toolbar);
        setSupportActionBar(MainToolbar);
        getSupportActionBar().setTitle("Search");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchBtn = (Button)findViewById(R.id.search_btn);
        searchName = (TextInputLayout)findViewById(R.id.search_bar);

        mAuth = FirebaseAuth.getInstance();
        UserStatusDatebase = FirebaseDatabase.getInstance().getReference().child("User").child(mAuth.getCurrentUser().getUid());

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = searchName.getEditText().getText().toString().toLowerCase();

                if(!TextUtils.isEmpty(username)){

                    Intent searchIntent = new Intent(SearchActivity.this, UsersActivity.class);
                    searchIntent.putExtra("search_name",username);
                    startActivity(searchIntent);

                }


            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        UserStatusDatebase.child("online").setValue("true");
    }

    @Override
    protected void onPause() {
        super.onPause();
        UserStatusDatebase.child("online").setValue(ServerValue.TIMESTAMP);
    }
}
