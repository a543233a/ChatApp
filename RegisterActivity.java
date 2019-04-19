package com.example.instantchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout rUsername;
    private TextInputLayout rEmail;
    private TextInputLayout rPassword;
    private Button rCreateButton;

    private Toolbar MainToolbar;

    private DatabaseReference MainDatabase;

    private ProgressDialog rProgress;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        MainToolbar=(Toolbar) findViewById(R.id.reg_toolbar);
        setSupportActionBar(MainToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rProgress = new ProgressDialog(this);


        mAuth = FirebaseAuth.getInstance();

        rUsername = (TextInputLayout) findViewById(R.id.r_username);
        rEmail = (TextInputLayout) findViewById(R.id.r_email);
        rPassword = (TextInputLayout) findViewById(R.id.r_password);
        rCreateButton = (Button) findViewById(R.id.create_btn);

        rCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = rUsername.getEditText().getText().toString();
                String email = rEmail.getEditText().getText().toString();
                String password = rPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(username)||!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password)){

                    rProgress.setTitle("Registering");
                    rProgress.setMessage("Creating Account");
                    rProgress.setCanceledOnTouchOutside(false);
                    rProgress.show();

                    register_form(username,email,password);

                }


            }
        });
    }

    private void register_form(final String username, String email, String password){

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();

                    MainDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(uid);

                    HashMap<String,String> userMap = new HashMap<>();
                    userMap.put("name", username);
                    userMap.put("status", "Hi there. I'm using ChatApp.");
                    userMap.put("image", "https://firebasestorage.googleapis.com/v0/b/instantchat-23bd2.appspot.com/o/Avatar_image%2Fdefault.jpg?alt=media&token=c54bf13a-f432-4f7f-8beb-cf5babe7f7dc");
                    userMap.put("device_token", deviceToken);
                    userMap.put("search", username.toLowerCase());

                    MainDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                rProgress.dismiss();

                                Intent ReIntent = new Intent(RegisterActivity.this,MainActivity.class);
                                ReIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(ReIntent);
                                finish();
                            }

                        }
                    });

                } else {

                    rProgress.hide();

                    Toast.makeText(RegisterActivity.this, "Something wrong, please check the form",Toast.LENGTH_LONG).show();
                }

            }
        });
    }

}
