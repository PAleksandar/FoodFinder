package com.foodfinder.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.foodfinder.acount.IntentExtraData;
import com.foodfinder.authentication.RegisterActivity;
import com.foodfinder.friends.FriendsFragment;
import com.foodfinder.home.HomeFragment;
import com.foodfinder.rank.RankFragment;
import com.foodfinder.service.NotificationService;
import com.foodfinder.service.UpdatePositionService;
import com.foodfinder.settings.SettingsFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;

public class NavigationActivity extends AppCompatActivity {

    public static final String CHANNEL_ID = "UpdatePositionServiceChannel";
    private TextView mTextMessage;
    private ActionBar actionbar;
    private TextView textview;
    private Context mContext;
    private Fragment homeFragment;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            Fragment selectedFragment=null;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    textview.setText("Home");
                    actionbar.setCustomView(textview);
                    if(homeFragment==null) {
                        selectedFragment = new HomeFragment();
                        homeFragment = selectedFragment;
                    }
                    else
                    {
                        selectedFragment=homeFragment;
                    }
                    break;
                case R.id.navigation_friends:
                    textview.setText("Friends");
                    actionbar.setCustomView(textview);
                    selectedFragment=new FriendsFragment();
                    break;
                case R.id.navigation_rank:
                    textview.setText("Rank");
                    actionbar.setCustomView(textview);
                    selectedFragment=new RankFragment();
                    break;
                case R.id.navigation_settings:
                    textview.setText("Settings");
                    actionbar.setCustomView(textview);
                    selectedFragment=new SettingsFragment();
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFragment,"one").commit();

            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        setActionBar();

        mContext=getApplicationContext();
        Fragment fragment;
        if(homeFragment==null) {
            fragment = new HomeFragment();
            homeFragment = fragment;
        }
        else {
            fragment=homeFragment;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,fragment).commit();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

//        if(!checkServiceRunning("com.foodfinder.service.NotificationService")) {
            startNotificationService();
//        }

//        if(!checkServiceRunning("com.foodfinder.service.UpdatePositionService")) {
            startUpdatePositionService();

//        }

       // createNotificationChannel();

        Intent iin= getIntent();
        Bundle b = iin.getExtras();

        if(b!=null)
        {
            String j =(String) b.get("inputExtra");
            Log.e("test notification extra", "navigation activity: "+j );

            Fragment f;
            if(homeFragment==null) {
                f = new HomeFragment();
                homeFragment = f;
            }
            else {
                f=homeFragment;
            }
            Bundle arguments = new Bundle();
            arguments.putString( "notification_start" , j);
            f.setArguments(arguments);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,f).commit();

        }
        else
        {
            Log.e("ne radi", "inputExtra: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_help:

                break;
            case R.id.menu_log_out:

                String uid = "";
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    uid = user.getUid();
                }
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("active");
                ref.setValue(false).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Write was successful!
                        // ...
                        Toast.makeText(NavigationActivity.this, "LogOut Successfully completed", Toast.LENGTH_LONG).show();



                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Write failed
                                // ...
                                Toast.makeText(NavigationActivity.this, "LogOut failed ", Toast.LENGTH_LONG).show();

                            }
                        });


                stopUpdatePositionService();
                stopNotificationService();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(mContext, MainActivity.class);
                finish();
                startActivity(intent);
                break;
        }

        return true;
    }

    private void setActionBar()
    {
        actionbar = getSupportActionBar();
        textview = new TextView(getApplicationContext());
        ViewGroup.LayoutParams layoutparams = new RelativeLayout.LayoutParams(android.app.ActionBar.LayoutParams.MATCH_PARENT, android.app.ActionBar.LayoutParams.WRAP_CONTENT);
        textview.setLayoutParams(layoutparams);
        textview.setText("Home");
        textview.setTextColor(Color.WHITE);
        textview.setTypeface(null, Typeface.BOLD);
        textview.setGravity(Gravity.CENTER);
        textview.setTextSize(20);
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        textview.setText("Home");
        actionbar.setCustomView(textview);
    }

    public void startUpdatePositionService() {

        if(!checkServiceRunning("com.foodfinder.service.UpdatePositionService")) {

            Intent serviceIntent = new Intent(this, UpdatePositionService.class);
            startService( serviceIntent);
        }

    }

    public void stopUpdatePositionService() {
        if(checkServiceRunning("com.foodfinder.service.UpdatePositionService")) {

            Intent serviceIntent = new Intent(this, UpdatePositionService.class);
            stopService(serviceIntent);
        }

    }

    public void startNotificationService() {
        if(!checkServiceRunning("com.foodfinder.service.NotificationService")) {

            Intent serviceIntent = new Intent(this, NotificationService.class);
            IntentExtraData data=new IntentExtraData(this.getClass());
            serviceIntent.putExtra("inputExtra", data);
            startService( serviceIntent);
        }

    }

    public void stopNotificationService() {
        if(checkServiceRunning("com.foodfinder.service.NotificationService")) {

            Intent serviceIntent = new Intent(this, NotificationService.class);
            stopService(serviceIntent);
        }


    }

    public boolean checkServiceRunning(String serviceName){
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceName.equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }
}
