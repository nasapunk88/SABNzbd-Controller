package com.gmail.at.faint545;

import com.gmail.at.faint545.fragments.RemoteFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class ControllerActivity extends FragmentActivity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getSupportActionBar().setDisplayShowHomeEnabled(false); // Remove activity/application icon                
        attachRemoteFragment(); // Attach RemoteFragment to this activity
    }

	private void attachRemoteFragment() {
		FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        
        RemoteFragment remote = new RemoteFragment();
        transaction.add(R.id.main_framelayout, remote);
        transaction.commit();
	}
}