package com.example.instantchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar MainToolbar;

    private ViewPager mViewPager;
    private  TabPager_Adapter Tab_pager_Adapter;

    private TabLayout mTabLayout;

    private DatabaseReference UserStatusDatebase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        MainToolbar=(Toolbar) findViewById(R.id.mainpage_toolbar);
        setSupportActionBar(MainToolbar);
        getSupportActionBar().setTitle("PolyChat");

        //Tab
        mViewPager =(ViewPager) findViewById(R.id.main_tab_pager);
        Tab_pager_Adapter = new TabPager_Adapter(getSupportFragmentManager());

        mViewPager.setAdapter(Tab_pager_Adapter);

        mTabLayout =(TabLayout) findViewById(R.id.main_tab);
        mTabLayout.setupWithViewPager(mViewPager);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null) {


            UserStatusDatebase = FirebaseDatabase.getInstance().getReference().child("User").child(mAuth.getCurrentUser().getUid());

        }




    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){

            sendToStart();
            
        }else{

            UserStatusDatebase.child("online").setValue("true");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        UserStatusDatebase.child("online").setValue(ServerValue.TIMESTAMP);
    }


    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout){

            FirebaseAuth.getInstance().signOut();
            sendToStart();

        }

        if(item.getItemId() == R.id.main_setting){

            Intent setting_intent = new Intent(MainActivity.this,SettingActivity.class);
            startActivity(setting_intent);

        }

        if(item.getItemId() == R.id.main_users){

            Intent setting_intent = new Intent(MainActivity.this,SearchActivity.class);
            startActivity(setting_intent);

        }

        return true;
    }

}
