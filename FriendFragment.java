package com.example.instantchat;


import android.app.AlertDialog;
import android.content.Context;
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
public class FriendFragment extends Fragment {

    private RecyclerView FriendList;
    private DatabaseReference FriendDatatbase;
    private DatabaseReference MainUserDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser current_user;

    private  String current_user_id;
    private  View FriendsView;


    public FriendFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FriendsView = inflater.inflate(R.layout.fragment_friend, container, false);
        FriendList = (RecyclerView) FriendsView.findViewById(R.id.friend_List);
        FriendList.setLayoutManager(new LinearLayoutManager(getContext()));

        current_user = FirebaseAuth.getInstance().getCurrentUser();
        current_user_id = current_user.getUid();

        FriendDatatbase = FirebaseDatabase.getInstance().getReference().child("Friends").child(current_user_id);
        FriendDatatbase.keepSynced(true);
        MainUserDatabase = FirebaseDatabase.getInstance().getReference().child("User");
        MainUserDatabase.keepSynced(true);

        // Inflate the layout for this fragment
        return FriendsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Friends> options =
                new  FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(FriendDatatbase, Friends.class)
                        .build();

        FirebaseRecyclerAdapter<Friends,FriendListViewHolder> adapter =
                new FirebaseRecyclerAdapter<Friends, FriendListViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final FriendListViewHolder holder, int position, @NonNull Friends model) {


                        final String FD_list_user_id = getRef(position).getKey();
                        MainUserDatabase.child(FD_list_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                final String username = dataSnapshot.child("name").getValue().toString();
                                String status = dataSnapshot.child("status").getValue().toString();
                                String Avatar = dataSnapshot.child("image").getValue().toString();

                                if(dataSnapshot.hasChild("online")){

                                    String user_online =  dataSnapshot.child("online").getValue().toString();
                                    holder.setOnline(user_online);

                                }

                                holder.setName(username);
                                holder.setStatus(status);
                                holder.setAvatar(Avatar);

                                holder.fView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        CharSequence options[] = new CharSequence[]{"Personal Profile","Create Chat"};
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                        builder.setTitle("Options");
                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                if(which == 0){

                                                    Intent profileIntent = new Intent(getContext(), UsersProfileActivity.class);
                                                    profileIntent.putExtra("user_id", FD_list_user_id);
                                                    startActivity(profileIntent);

                                                }
                                                if(which == 1){

                                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                    chatIntent.putExtra("user_id", FD_list_user_id);
                                                    chatIntent.putExtra("user_name", username);
                                                    startActivity(chatIntent);

                                                }

                                            }
                                        });

                                        builder.show();

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
                    public FriendListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.single_user_layout,viewGroup,false);
                        FriendListViewHolder viewHolder = new FriendListViewHolder(view);
                        return viewHolder;
                    }
                };

        FriendList.setAdapter(adapter);

        adapter.startListening();

    }

    public static class FriendListViewHolder extends RecyclerView.ViewHolder{

        TextView displaystatus;
        TextView displayedName;
        CircleImageView AvatarImage;
        ImageView Online_Icon;
        View fView;

        public FriendListViewHolder(@NonNull View itemView) {
            super(itemView);

            fView = itemView;

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
