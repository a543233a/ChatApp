package com.example.instantchat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
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
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersProfileActivity extends AppCompatActivity {

    private CircleImageView ProfileImage;
    private TextView ProfileName;
    private TextView ProfileStatus;
    private Button SendRequestBtn;
    private Button DeclineRequestBtn;

    private DatabaseReference MainUserDatabase;
    private DatabaseReference FriendRequestDatabase;
    private DatabaseReference FriendListDatabase;
    private DatabaseReference NotificationDatabase;
    private DatabaseReference UserStatusDatebase;
    private FirebaseAuth mAuth;


    private FirebaseUser current_user;

    private ProgressDialog UPProgress;

    private String current_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        MainUserDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(user_id);
        MainUserDatabase.keepSynced(true);
        FriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Request");
        FriendRequestDatabase.keepSynced(true);
        FriendListDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendListDatabase.keepSynced(true);
        NotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        current_user = FirebaseAuth.getInstance().getCurrentUser();

        mAuth = FirebaseAuth.getInstance();
        UserStatusDatebase = FirebaseDatabase.getInstance().getReference().child("User").child(mAuth.getCurrentUser().getUid());

        ProfileImage = (CircleImageView) findViewById(R.id.profile_image);
        ProfileName = (TextView) findViewById(R.id.profile_username);
        ProfileStatus = (TextView) findViewById(R.id.profile_status);
        SendRequestBtn = (Button) findViewById(R.id.sendRequestBtn);
        DeclineRequestBtn = (Button) findViewById(R.id.DeclineFriendBtn);

        UPProgress = new ProgressDialog(this);
        UPProgress.setTitle("Loading");
        UPProgress.setMessage("Please wait");
        UPProgress.setCanceledOnTouchOutside(false);
        UPProgress.show();

        current_state = "not_friend";

        MainUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String image = dataSnapshot.child("image").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();

                ProfileName.setText(name);
                ProfileStatus.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(ProfileImage);

                FriendRequestDatabase.child(current_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)) {

                            String request_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(request_type.equals("received")){

                                SendRequestBtn.setText("ACCEPT REQUEST");
                                current_state = "request_received";

                                DeclineRequestBtn.setVisibility(View.VISIBLE);
                                DeclineRequestBtn.setEnabled(true);




                            }else if(request_type.equals("sent")){


                                current_state = "request_friend";
                                SendRequestBtn.setText("CANCEL REQUEST");

                                DeclineRequestBtn.setVisibility(View.INVISIBLE);
                                DeclineRequestBtn.setEnabled(false);
                            }

                            UPProgress.dismiss();

                        }else {

                            FriendListDatabase.child(current_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){

                                        SendRequestBtn.setText("UNFRIEND");
                                        current_state = "is_friend";

                                        DeclineRequestBtn.setVisibility(View.INVISIBLE);
                                        DeclineRequestBtn.setEnabled(false);

                                    }
                                    UPProgress.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    UPProgress.dismiss();
                                }
                            });

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {



            }
        });

        SendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendRequestBtn.setEnabled(false);

                //send request
                if(current_state.equals("not_friend")){

                    FriendRequestDatabase.child(current_user.getUid()).child(user_id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                FriendRequestDatabase.child(user_id).child(current_user.getUid()).child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(UsersProfileActivity.this,"Sent Successfully.", Toast.LENGTH_LONG).show();

                                        HashMap<String, String> notification_data = new HashMap<>();
                                        notification_data.put("from", current_user.getUid());
                                        notification_data.put("type", "request");

                                        NotificationDatabase.child(user_id).push().setValue(notification_data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                SendRequestBtn.setText("CANCEL REQUEST");
                                                current_state = "request_friend";

                                                DeclineRequestBtn.setVisibility(View.INVISIBLE);
                                                DeclineRequestBtn.setEnabled(false);

                                            }
                                        });

                                    }
                                });

                            }else{

                                Toast.makeText(UsersProfileActivity.this,"Failed to Send.", Toast.LENGTH_LONG).show();

                            }

                            SendRequestBtn.setEnabled(true);
                        }
                    });
                }

                //cancel request
                if(current_state.equals("request_friend")){

                    FriendRequestDatabase.child(current_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            FriendRequestDatabase.child(user_id).child(current_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    SendRequestBtn.setEnabled(true);
                                    SendRequestBtn.setText("SEND REQUEST");
                                    current_state = "not_friend";

                                    DeclineRequestBtn.setVisibility(View.INVISIBLE);
                                    DeclineRequestBtn.setEnabled(false);

                                }
                            });

                        }
                    });

                }

                // request received
                if(current_state.equals("request_received")){

                    final String current_Date = DateFormat.getDateTimeInstance().format(new Date());

                    FriendListDatabase.child(current_user.getUid()).child(user_id).child("date").setValue(current_Date).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            FriendListDatabase.child(user_id).child(current_user.getUid()).child("date").setValue(current_Date).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    FriendRequestDatabase.child(current_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            FriendRequestDatabase.child(user_id).child(current_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    SendRequestBtn.setEnabled(true);
                                                    SendRequestBtn.setText("UNFRIEND");
                                                    current_state = "is_friend";

                                                    DeclineRequestBtn.setVisibility(View.INVISIBLE);
                                                    DeclineRequestBtn.setEnabled(false);

                                                }
                                            });

                                        }
                                    });

                                }
                            });

                        }
                    });

                }

                //unfriend
                if(current_state.equals("is_friend")){

                    FriendListDatabase.child(current_user.getUid()).child(user_id).child("date").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            FriendListDatabase.child(user_id).child(current_user.getUid()).child("date").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    SendRequestBtn.setEnabled(true);
                                    SendRequestBtn.setText("SEND REQUEST");
                                    current_state = "not_friend";

                                    DeclineRequestBtn.setVisibility(View.INVISIBLE);
                                    DeclineRequestBtn.setEnabled(false);

                                }
                            });

                        }
                    });

                }



            }
        });

        DeclineRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FriendRequestDatabase.child(current_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        FriendRequestDatabase.child(user_id).child(current_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                SendRequestBtn.setEnabled(true);
                                SendRequestBtn.setText("SEND REQUEST");
                                current_state = "not_friend";

                                DeclineRequestBtn.setVisibility(View.INVISIBLE);
                                DeclineRequestBtn.setEnabled(false);

                            }
                        });

                    }
                });



            }
        });


    }

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
