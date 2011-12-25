package com.gmail.at.faint545.activities;

import java.util.ArrayList;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.databases.RemoteDatabase;
import com.gmail.at.faint545.fragments.RemoteFragment;

public class ControllerActivity extends FragmentActivity {
	private ArrayList<Remote> remotes = new ArrayList<Remote>();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getSupportActionBar().setDisplayShowHomeEnabled(false); // Remove activity/application icon                        
        retrieveRemotes();
        attachRemoteFragment(); // Attach RemoteFragment to this activity
    }

	private void retrieveRemotes() {
		RemoteDatabase db = new RemoteDatabase(this);		
		db.open();
		Cursor cur = db.getAllRows();		
		while(cur.moveToNext()) {
			String name = cur.getString(RemoteDatabase.NAME_INDEX);
			String address = cur.getString(RemoteDatabase.ADDR_INDEX);
			String port = cur.getString(RemoteDatabase.PORT_INDEX);
			String apiKey = cur.getString(RemoteDatabase.API_KEY_INDEX);
			remotes.add(new Remote(name).setAddress(address).setPort(port).setApiKey(apiKey));
		}
	}

	private void attachRemoteFragment() {
		FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        
        RemoteFragment remote = RemoteFragment.newInstance(remotes);
        transaction.add(R.id.main_framelayout, remote);
        transaction.commit();
	}
}