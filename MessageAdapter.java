package com.example.instantchat;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Messages> MessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference MainUserDatabase;

    public  MessageAdapter(List<Messages> MessageList){

        this.MessageList = MessageList;

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_message_layout,viewGroup,false);
        return new MessageViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {

        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();

        Messages c = MessageList.get(i);

        String from_user = c.getFrom();
        String message_type = c.getType();

        MainUserDatabase = FirebaseDatabase.getInstance().getReference().child("User").child(from_user);
        MainUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String image = dataSnapshot.child("image").getValue().toString();
                Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(messageViewHolder.chatMessageAvatar);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(message_type.equals("text")) {

            messageViewHolder.messageText.setText(c.getMessage());
            messageViewHolder.messageText.setVisibility(View.VISIBLE);
            messageViewHolder.messageImage.setVisibility(View.GONE);

        } else {

            messageViewHolder.messageImage.setVisibility(View.VISIBLE);
            Picasso.get().load(c.getMessage()).into(messageViewHolder.messageImage);
            messageViewHolder.messageText.setVisibility(View.GONE);

        }


        if(from_user.equals(current_user_id)){

            messageViewHolder.messageText.setBackgroundColor(Color.WHITE);
            messageViewHolder.messageText.setTextColor(Color.BLACK);

        }else{

            messageViewHolder.messageText.setBackgroundColor(R.color.colorPrimaryDark);
            messageViewHolder.messageText.setTextColor(Color.WHITE);

        }

    }

    @Override
    public int getItemCount() {
        return MessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText;
        public CircleImageView chatMessageAvatar;
        public ImageView messageImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.message_content);
            chatMessageAvatar = (CircleImageView) itemView.findViewById(R.id.message_list_avatar);
            messageImage = (ImageView) itemView.findViewById(R.id.image_content);
        }
    }



}
