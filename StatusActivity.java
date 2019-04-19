package com.example.instantchat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class StatusActivity extends AppCompatActivity {

    private Toolbar MainToolbar;

    private TextInputLayout Mainstatus;
    private Button sSaveBtn;

    private DatabaseReference statusDatabase;
    private FirebaseUser current_user;

    private ProgressDialog statusProgress;

    private DatabaseReference UserStatusDatebase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mAuth = FirebaseAuth.getInstance();
        UserStatusDatebase = FirebaseDatabase.getInstance().getReference().child("User").child(mAuth.getCurrentUser().getUid());

        current_user= FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = current_user.getUid();

        statusDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(current_uid);

        MainToolbar=(Toolbar) findViewById(R.id.status_toolbar);
        setSupportActionBar(MainToolbar);
        getSupportActionBar().setTitle("Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String status_value = getIntent().getStringExtra("status_value");
        Mainstatus=(TextInputLayout) findViewById(R.id.status_update);
        sSaveBtn = (Button) findViewById(R.id.status_saveBtn);

        Mainstatus.getEditText().setText(status_value);

        sSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                statusProgress = new ProgressDialog(StatusActivity.this);
                statusProgress.setTitle("Saving");
                statusProgress.setMessage("Please wait.");
                statusProgress.show();

                String status = Mainstatus.getEditText().getText().toString();

                statusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){

                            statusProgress.dismiss();

                        }else {

                            Toast.makeText(getApplicationContext(),"Something wrong here.", Toast.LENGTH_LONG);
                        }

                    }
                });


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
