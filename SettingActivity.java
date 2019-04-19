package com.example.instantchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private DatabaseReference MainUserDatabase;
    private FirebaseUser MainCurrentUser;
    private DatabaseReference UserStatusDatebase;
    private FirebaseAuth mAuth;

    private CircleImageView AvatarImage;
    private TextView display_username;
    private TextView display_status;

    private Button statusBtn;
    private Button AvatarBtn;

    private  static  final  int pick_image = 1;

    private StorageReference AvatarStorage;

    private ProgressDialog uploadProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        AvatarImage = (CircleImageView) findViewById(R.id.Avatar_image);
        display_username =(TextView) findViewById(R.id.setting_username);
        display_status =(TextView) findViewById(R.id.setting_status);
        statusBtn = (Button) findViewById(R.id.setting_statusBtn);
        AvatarBtn = (Button) findViewById(R.id.setting_AvatarBtn);

        AvatarStorage = FirebaseStorage.getInstance().getReference();

        MainCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        String current_uid = MainCurrentUser.getUid();

        MainUserDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(current_uid);
        MainUserDatabase.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        UserStatusDatebase = FirebaseDatabase.getInstance().getReference().child("User").child(mAuth.getCurrentUser().getUid());

        MainUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("name").getValue().toString();
                String avatar =  dataSnapshot.child("image").getValue().toString();
                String status =  dataSnapshot.child("status").getValue().toString();


                //Change

                display_username.setText(username);
                display_status.setText(status);

                Picasso.get().load(avatar).placeholder(R.drawable.default_avatar).into(AvatarImage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        statusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String status_value = display_status.getText().toString();

                Intent status_intent = new Intent(SettingActivity.this, StatusActivity.class);
                status_intent.putExtra("status_value",status_value);
                startActivity(status_intent);
            }
        });

        AvatarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                Intent imageIntent = new Intent();
                imageIntent.setType("image/*");
                imageIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(imageIntent,"Select Image"), pick_image);


                /*
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingActivity.this);

                 */

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == pick_image && resultCode == RESULT_OK){

            Uri image_Uri = data.getData();

            CropImage.activity(image_Uri)
                    .setAspectRatio(1,1)
                    .setMinCropWindowSize(500,500)
                    .start(this);

            //Toast.makeText(SettingActivity.this, image_Uri, Toast.LENGTH_LONG).show();

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                uploadProgress = new ProgressDialog(SettingActivity.this);
                uploadProgress.setTitle("Uploading");
                uploadProgress.setMessage("Please wait.");
                uploadProgress.setCanceledOnTouchOutside(false);
                uploadProgress.show();

                Uri resultUri = result.getUri();

                String get_current_uid = MainCurrentUser.getUid();

                StorageReference file_path = AvatarStorage.child("Avatar_image").child(get_current_uid + ".jpg");

                file_path.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String download_link = uri.toString();

                                MainUserDatabase.child("image").setValue(download_link).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){

                                            uploadProgress.dismiss();
                                            Toast.makeText(SettingActivity.this, "Success Uploading", Toast.LENGTH_LONG).show();

                                        }else{

                                            Toast.makeText(SettingActivity.this, "Error in uploading", Toast.LENGTH_LONG).show();
                                            uploadProgress.dismiss();
                                        }


                                    }
                                });
                            }
                        });

                    }
                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }


}
