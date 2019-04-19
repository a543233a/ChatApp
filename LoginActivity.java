package com.example.instantchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Toolbar MainToolbar;

    private TextInputLayout LoginEmail;
    private TextInputLayout LoginPassword;
    private Button LoginButton;

    private ProgressDialog lProgress;

    private FirebaseAuth mAuth;
    private DatabaseReference UserTokenDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        MainToolbar=(Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(MainToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lProgress = new ProgressDialog(this);

        UserTokenDatabase = FirebaseDatabase.getInstance().getReference().child("User");

        LoginEmail = (TextInputLayout) findViewById(R.id.login_Email);
        LoginPassword = (TextInputLayout) findViewById(R.id.login_password);
        LoginButton = (Button) findViewById(R.id.loginPage_btn);

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = LoginEmail.getEditText().getText().toString();
                String password = LoginPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password)){

                    lProgress.setTitle("Logging In");
                    lProgress.setMessage("Please Wait");
                    lProgress.setCanceledOnTouchOutside(false);
                    lProgress.show();

                    loginUser(email,password);

                }

            }
        });

    }

    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    lProgress.dismiss();

                    String current_user = mAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    UserTokenDatabase.child(current_user).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Intent logIntent = new Intent(LoginActivity.this,MainActivity.class);
                            logIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(logIntent);
                            finish();

                        }
                    });


                }else {

                    lProgress.hide();

                    Toast.makeText(LoginActivity.this, "Something wrong, please check the form",Toast.LENGTH_LONG).show();
                }

            }
        });

    }
}
