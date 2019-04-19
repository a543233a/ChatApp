package com.example.instantchat;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private RecyclerView ChatList;
    private DatabaseReference ChatDatatbase;
    private DatabaseReference MainUserDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser current_user;

    private  String current_user_id;
    private  View ChatsView;


    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ChatsView = inflater.inflate(R.layout.fragment_chat, container, false);
        ChatList = (RecyclerView) ChatsView.findViewById(R.id.Chat_List);
        ChatList.setLayoutManager(new LinearLayoutManager(getContext()));

        current_user = FirebaseAuth.getInstance().getCurrentUser();
        current_user_id = current_user.getUid();

        ChatDatatbase = FirebaseDatabase.getInstance().getReference().child("chat").child(current_user_id);
        ChatDatatbase.keepSynced(true);
        MainUserDatabase = FirebaseDatabase.getInstance().getReference().child("User");
        MainUserDatabase.keepSynced(true);

        // Inflate the layout for this fragment
        return ChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Chats> options =
                new  FirebaseRecyclerOptions.Builder<Chats>()
                        .setQuery(ChatDatatbase, Chats.class)
                        .build();

        FirebaseRecyclerAdapter<Chats, ChatFragment.ChatListViewHolder> adapter =
                new FirebaseRecyclerAdapter<Chats, ChatFragment.ChatListViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatFragment.ChatListViewHolder holder, int position, @NonNull Chats model) {


                        final String Chat_list_user_id = getRef(position).getKey();
                        MainUserDatabase.child(Chat_list_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                final String username = dataSnapshot.child("name").getValue().toString();
                                String status = dataSnapshot.child("status").getValue().toString();;
                                String Avatar = dataSnapshot.child("image").getValue().toString();

                                if(dataSnapshot.hasChild("online")){

                                    String user_online =  dataSnapshot.child("online").getValue().toString();
                                    holder.setOnline(user_online);

                                }

                                holder.setName(username);
                                holder.setStatus(status);
                                holder.setAvatar(Avatar);

                                holder.cView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {


                                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                    chatIntent.putExtra("user_id", Chat_list_user_id);
                                                    chatIntent.putExtra("user_name", username);
                                                    startActivity(chatIntent);



                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public ChatFragment.ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.single_user_layout,viewGroup,false);
                        ChatFragment.ChatListViewHolder viewHolder = new  ChatFragment.ChatListViewHolder(view);
                        return viewHolder;
                    }
                };

        ChatList.setAdapter(adapter);

        adapter.startListening();

    }

    public static class ChatListViewHolder extends RecyclerView.ViewHolder{

        TextView displaystatus;
        TextView displayedName;
        CircleImageView AvatarImage;
        ImageView Online_Icon;
        View cView;

        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);

            cView = itemView;

        }
        public void setName(String name){

            displayedName = (TextView)itemView.findViewById(R.id.single_username);
            displayedName.setText(name);

        }

        public void setStatus(String status){

            displaystatus = (TextView)itemView.findViewById(R.id.single_status);
            displaystatus.setText(status);

        }


        public void setAvatar(String image){

            AvatarImage = (CircleImageView)itemView.findViewById(R.id.user_thumb_image);
            Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(AvatarImage);

        }

        public void setOnline(String online_status){

            Online_Icon = (ImageView) itemView.findViewById(R.id.online_icon);

            if(online_status.equals("true")){

                Online_Icon.setVisibility(View.VISIBLE);
            }
            else {

                Online_Icon.setVisibility(View.INVISIBLE);

            }

        }

    }

}
