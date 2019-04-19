package com.example.instantchat;


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
public class RequestFragment extends Fragment {


    private RecyclerView RequestList;
    private DatabaseReference RequestDatatbase;
    private DatabaseReference MainUserDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser current_user;

    private  String current_user_id;
    private  View RequestsView;


    public RequestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RequestsView = inflater.inflate(R.layout.fragment_request, container, false);
        RequestList = (RecyclerView) RequestsView.findViewById(R.id.request_List);
        RequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        current_user = FirebaseAuth.getInstance().getCurrentUser();
        current_user_id = current_user.getUid();

        RequestDatatbase = FirebaseDatabase.getInstance().getReference().child("Friend_Request").child(current_user_id);
        RequestDatatbase.keepSynced(true);
        MainUserDatabase = FirebaseDatabase.getInstance().getReference().child("User");
        MainUserDatabase.keepSynced(true);

        // Inflate the layout for this fragment
        return RequestsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Requests> options =
                new  FirebaseRecyclerOptions.Builder<Requests>()
                        .setQuery(RequestDatatbase, Requests.class)
                        .build();

        FirebaseRecyclerAdapter<Requests, RequestFragment.RequestListViewHolder> adapter =
                new FirebaseRecyclerAdapter<Requests, RequestFragment.RequestListViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestFragment.RequestListViewHolder holder, int position, @NonNull Requests model) {


                        final String request_list_user_id = getRef(position).getKey();
                        MainUserDatabase.child(request_list_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                final String username = dataSnapshot.child("name").getValue().toString();
                                String status = dataSnapshot.child("status").getValue().toString();
                                String Avatar = dataSnapshot.child("image").getValue().toString();


                                holder.setName(username);
                                holder.setStatus(status);
                                holder.setAvatar(Avatar);

                                holder.rView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {


                                        Intent profileIntent = new Intent(getContext(), UsersProfileActivity.class);
                                        profileIntent.putExtra("user_id", request_list_user_id);
                                        startActivity(profileIntent);


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
                    public RequestFragment.RequestListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.single_user_layout,viewGroup,false);
                        RequestFragment.RequestListViewHolder viewHolder = new  RequestFragment.RequestListViewHolder(view);
                        return viewHolder;
                    }
                };

        RequestList.setAdapter(adapter);

        adapter.startListening();

    }

    public static class RequestListViewHolder extends RecyclerView.ViewHolder{

        TextView displaystatus;
        TextView displayedName;
        CircleImageView AvatarImage;
        ImageView Online_Icon;
        View rView;

        public RequestListViewHolder(@NonNull View itemView) {
            super(itemView);

            rView = itemView;

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

    }


}
