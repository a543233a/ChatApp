package com.example.instantchat;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar MainToolbar;

    private RecyclerView All_usersList;

    private DatabaseReference MainUserDatabase;

    private DatabaseReference UserStatusDatebase;
    private FirebaseAuth mAuth;

    private TextView noResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        MainUserDatabase = FirebaseDatabase.getInstance().getReference().child("User");
        MainUserDatabase.keepSynced(true);
        noResult = (TextView)findViewById(R.id.no_result);

        MainToolbar=(Toolbar) findViewById(R.id.users_toolbar);
        setSupportActionBar(MainToolbar);
        getSupportActionBar().setTitle("Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //
        All_usersList =(RecyclerView) findViewById(R.id.all_users_list);
        All_usersList.setLayoutManager((new LinearLayoutManager(this)));



        mAuth = FirebaseAuth.getInstance();
        UserStatusDatebase = FirebaseDatabase.getInstance().getReference().child("User").child(mAuth.getCurrentUser().getUid());



    }

    @Override
    protected void onStart() {
        super.onStart();

        UserStatusDatebase.child("online").setValue("true");

        final String user_name = getIntent().getStringExtra("search_name");

        FirebaseRecyclerOptions<Users> options =
                new  FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(MainUserDatabase, Users.class)
                .build();

        FirebaseRecyclerAdapter<Users,UserListViewHolder> adapter =
                new FirebaseRecyclerAdapter<Users, UserListViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull UserListViewHolder holder, int position, @NonNull Users model) {

                        String name = model.getSearch();
                        if(name.equals(user_name)){

                            holder.displayedName.setText(model.getName());
                            holder.displayStatus.setText(model.getStatus());
                            Picasso.get().load(model.getImage()).into(holder.AvatarImage);
                            noResult.setVisibility(View.GONE);
                        }else {
                            holder.displayedName.setVisibility(View.GONE);
                            holder.displayStatus.setVisibility(View.GONE);
                            holder.AvatarImage.setVisibility(View.GONE);
                            holder.Online_Icon.setVisibility(View.GONE);
                            holder.uView.setVisibility(View.GONE);
                        }



                        //
                        final String user_id =getRef(position).getKey();

                        holder.uView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent profileIntent = new Intent(UsersActivity.this, UsersProfileActivity.class);
                                profileIntent.putExtra("user_id", user_id);
                                startActivity(profileIntent);

                            }
                        });



                    }

                    @NonNull
                    @Override
                    public UserListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.single_user_layout,viewGroup,false);
                        UserListViewHolder viewHolder = new UserListViewHolder(view);
                        return viewHolder;
                    }
                };

        All_usersList.setAdapter(adapter);

        adapter.startListening();


    }

    @Override
    protected void onPause() {
        super.onPause();
        UserStatusDatebase.child("online").setValue(ServerValue.TIMESTAMP);
    }


    public static class UserListViewHolder extends RecyclerView.ViewHolder{

        TextView displayedName;
        TextView displayStatus;
        CircleImageView AvatarImage;
        ImageView Online_Icon;
        View uView;



        public UserListViewHolder(@NonNull View itemView) {
            super(itemView);

            uView = itemView;
            displayedName = (TextView)itemView.findViewById(R.id.single_username);
            displayStatus = (TextView)itemView.findViewById(R.id.single_status);
            AvatarImage = (CircleImageView)itemView.findViewById(R.id.user_thumb_image);
            Online_Icon = (ImageView) itemView.findViewById(R.id.online_icon);

        }

    }
}
