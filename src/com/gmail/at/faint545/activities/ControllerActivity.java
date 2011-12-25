package com.gmail.at.faint545.activities;

import java.util.ArrayList;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.databases.RemoteDatabase;
import com.gmail.at.faint545.fragments.NewRemoteFragment;
import com.gmail.at.faint545.fragments.NewRemoteFragment.NewRemoteListener;
import com.gmail.at.faint545.fragments.RemoteFragment;

public class ControllerActivity extends FragmentActivity implements NewRemoteListener {
	private ArrayList<Remote> remotes = new ArrayList<Remote>();
	private FragmentTransaction mTransaction;
	
	private RemoteFragment remoteFragment;
	private NewRemoteFragment newRemoteFragment;
	
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
		if(remotes.size() < cur.getCount()) {
			cur.moveToPosition(remotes.size()-1);
		}
		while(cur.moveToNext()) {
			String name = cur.getString(RemoteDatabase.NAME_INDEX);
			String address = cur.getString(RemoteDatabase.ADDR_INDEX);
			String port = cur.getString(RemoteDatabase.PORT_INDEX);
			String apiKey = cur.getString(RemoteDatabase.API_KEY_INDEX);
			remotes.add(new Remote(name).setAddress(address).setPort(port).setApiKey(apiKey));
		}
		cur.close();
		db.close();
	}

	private void attachRemoteFragment() {
		mTransaction = getSupportFragmentManager().beginTransaction();
        
		remoteFragment = RemoteFragment.newInstance(remotes);
        mTransaction.add(R.id.main_framelayout, remoteFragment);
        mTransaction.commit();
        mTransaction = null;
	}
	
	/*
	 * This is called when the add new remote
	 * text view is clicked.
	 */
	public void launchNewRemoteFragment(View view) {
		mTransaction = getSupportFragmentManager().beginTransaction();
		
		newRemoteFragment = new NewRemoteFragment();
		mTransaction.setCustomAnimations(R.anim.fragment_slide_left_enter, // Set custom transition animations
										 R.anim.fragment_slide_left_exit,
										 R.anim.fragment_slide_right_enter,
										 R.anim.fragment_slide_right_exit);
		
		mTransaction.replace(R.id.main_framelayout, newRemoteFragment);
		mTransaction.addToBackStack(null);
		mTransaction.commit();
		mTransaction = null;
	}

	/*
	 * This function is called when a remote has been saved
	 */
	@Override
	public void onRemoteSaved() {
		retrieveRemotes();
		remoteFragment.notifyDataSetChanged();
		getSupportFragmentManager().popBackStack();
		newRemoteFragment = null;
	}
}