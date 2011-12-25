package com.gmail.at.faint545.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.databases.RemoteDatabase;
import com.gmail.at.faint545.fragments.NewRemoteFragment;
import com.gmail.at.faint545.fragments.NewRemoteFragment.NewRemoteListener;
import com.gmail.at.faint545.fragments.RemoteFragment;
import com.gmail.at.faint545.zxing.IntentIntegrator;
import com.gmail.at.faint545.zxing.IntentResult;

public class ControllerActivity extends FragmentActivity implements NewRemoteListener {
	private ArrayList<Remote> remotes = new ArrayList<Remote>(); // A list of user defined remotes
	
	private RemoteFragment remoteFragment; // Fragment for viewing remotes
	private NewRemoteFragment newRemoteFragment;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);        
        getSupportActionBar().setDisplayShowHomeEnabled(false); // Remove activity/application icon                        
        retrieveRemotes();
        attachFragment(remoteFragment = RemoteFragment.newInstance(remotes)); // Attach RemoteFragment to this activity
    }

    /*
     * Obtain all SABNzbd remote profiles from the local data store.
     */
	private void retrieveRemotes() {
		RemoteDatabase db = new RemoteDatabase(this);		
		db.open();
		Cursor cur = db.getAllRows();
		
		if(remotes.size() < cur.getCount()) cur.moveToPosition(remotes.size()-1); // If a new remote has been added, move to that remote in the database
		
		while(cur.moveToNext()) { // Collect all columns from each row and process it
			String name = cur.getString(RemoteDatabase.NAME_INDEX);
			String address = cur.getString(RemoteDatabase.ADDR_INDEX);
			String port = cur.getString(RemoteDatabase.PORT_INDEX);
			String apiKey = cur.getString(RemoteDatabase.API_KEY_INDEX);
			remotes.add(new Remote(name).setAddress(address).setPort(port).setApiKey(apiKey));
		}
		cur.close();
		db.close();
	}

	/*
	 * Attach the RemoteFragment to the current Activity.
	 */
	private void attachFragment(Fragment newFragment) {
		getSupportFragmentManager().beginTransaction().add(R.id.main_framelayout, newFragment).commit();
	}
	
	/*
	 * This is called when the add new remote
	 * text view is clicked.
	 */
	public void launchNewRemoteFragment(View view) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
				
		transaction.setCustomAnimations(R.anim.fragment_slide_left_enter, // Set custom transition animations
										 R.anim.fragment_slide_left_exit,
										 R.anim.fragment_slide_right_enter,
										 R.anim.fragment_slide_right_exit);
		
		transaction.replace(R.id.main_framelayout, newRemoteFragment = new NewRemoteFragment());
		transaction.addToBackStack(null);
		transaction.commit();
	}

	/*
	 * This function is called when a remote has been saved
	 */
	@Override
	public void onRemoteSaved() {
		retrieveRemotes();
		remoteFragment.notifyDataSetChanged();
		getSupportFragmentManager().popBackStack(); // Pop off the last saved state
		newRemoteFragment = null;
	}
	
	/*
	 * This function is called when QR button is clicked
	 */
	public void launchQrScanner(View view) {
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan();
	}	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if(result != null) {
			newRemoteFragment.populateApiKey(result.getContents());
		}
		super.onActivityResult(requestCode, resultCode, data);
	}	
}