package com.example.instantchat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private  String chat_user;
    private  String current_user_id;

    private Toolbar MainChatToolbar;

    private DatabaseReference chatDatebase;
    private DatabaseReference UserStatusDatebase;
    private FirebaseAuth mAuth;

    private TextView main_chat_username;
    private TextView main_chat_lastseen;
    private CircleImageView chat_profileimage;

    private ImageButton ChatAttachBtn;
    private ImageButton ChatSendBtn;
    private EditText ChatEnterMessage;

    private RecyclerView ChatMessage;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager LinearLayout;
    private MessageAdapter adapter;

    private  static final  int pick_image = 1;
    private StorageReference ImageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        UserStatusDatebase = FirebaseDatabase.getInstance().getReference().child("User").child(mAuth.getCurrentUser().getUid());
        current_user_id = mAuth.getCurrentUser().getUid();

        chatDatebase = FirebaseDatabase.getInstance().getReference();
        chatDatebase.keepSynced(true);

        ImageStorage = FirebaseStorage.getInstance().getReference();

        chat_user = getIntent().getStringExtra("user_id");
        String chat_user_name = getIntent().getStringExtra("user_name");

        MainChatToolbar=(Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(MainChatToolbar);
        //getSupportActionBar().setTitle(chat_user_name);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater =(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_bar,null);

        actionBar.setCustomView(action_bar_view);

        adapter = new MessageAdapter(messagesList);

        ChatAttachBtn = (ImageButton) findViewById(R.id.chat_attach_btn);
        ChatSendBtn = (ImageButton) findViewById(R.id.chat_send_btn);
        ChatEnterMessage =(EditText) findViewById(R.id.chat_enter_message);

        ChatMessage = (RecyclerView) findViewById(R.id.chat_message);
        LinearLayout = new LinearLayoutManager(this);

        ChatMessage.setLayoutManager(LinearLayout);
        ChatMessage.setAdapter(adapter);

        loadMessage();

        main_chat_username = (TextView) findViewById(R.id.chat_username);
        main_chat_lastseen = (TextView) findViewById(R.id.chat_lastseen);
        chat_profileimage = (CircleImageView) findViewById(R.id.chat_avatar);

        main_chat_username.setText(chat_user_name);

        chatDatebase.child("User").child(chat_user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(chat_profileimage);

                if(online.equals("true")){

                    main_chat_lastseen.setText("Online");

                }else {

                    GetTime get_time = new GetTime();
                    long lastTime = Long.parseLong(online);
                    String lastSeen = get_time.getTimeAgo(lastTime,getApplicationContext());
                    main_chat_lastseen.setText(lastSeen);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });


        chatDatebase.child("chat").child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(chat_user)){

                    Map chatMap = new HashMap();
                    chatMap.put("seen", false);
                    chatMap.put("time", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("chat/" + current_user_id + "/" + chat_user, chatMap);
                    chatUserMap.put("chat/" + chat_user + "/" + current_user_id, chatMap);

                    chatDatebase.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError != null){

                                Log.d("CHAT_LOG", databaseError.getMessage().toString());

                            }

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //chat
        ChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                sendMessage();
                
            }
        });

        ChatAttachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageIntent = new Intent();
                imageIntent.setType("image/*");
                imageIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(imageIntent,"Select Image"), pick_image);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == pick_image && resultCode == RESULT_OK){

            Uri image_Uri = data.getData();

            final String current_user_ref = "message/" + current_user_id + "/" + chat_user;
            final String chat_user_ref = "message/" + chat_user + "/" + current_user_id;

            DatabaseReference message_push = chatDatebase.child("message").child(current_user_id).child(chat_user).push();

            final String push_id = message_push.getKey();

            StorageReference file_path = ImageStorage.child("message_images").child( push_id + ".jpg");

            file_path.putFile(image_Uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String download_link = uri.toString();

                            Map messageMap = new HashMap();
                            messageMap.put("message", download_link);
                            messageMap.put("seen", false);
                            messageMap.put("type", "image");
                            messageMap.put("time", ServerValue.TIMESTAMP);
                            messageMap.put("from", current_user_id);

                            Map messageUserMap = new HashMap();
                            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                            ChatEnterMessage.setText("");

                            chatDatebase.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                    if(databaseError != null){

                                        Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                    }

                                }
                            });


                        }
                    });

                }
            });

        }

    }

    private void loadMessage() {

        chatDatebase.child("message").child(current_user_id).child(chat_user).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                messagesList.add(message);
                adapter.notifyDataSetChanged();

                ChatMessage.scrollToPosition(messagesList.size() - 1);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {

        String message = ChatEnterMessage.getText().toString();

        if(!TextUtils.isEmpty(message)){

            String curent_user_ref = "message/" + current_user_id + "/" + chat_user;
            String chat_user_ref =  "message/" + chat_user+ "/" + current_user_id;

            DatabaseReference user_message_pushID = chatDatebase.child("message").child(current_user_id).child(chat_user).push();

            String push_id = user_message_pushID.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", current_user_id);

            Map messageUserMap = new HashMap();
            messageUserMap.put(curent_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            ChatEnterMessage.setText("");

            chatDatebase.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                    if(databaseError != null){

                        Log.d("CHAT_LOG", databaseError.getMessage().toString());

                    }

                }
            });

        }

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
