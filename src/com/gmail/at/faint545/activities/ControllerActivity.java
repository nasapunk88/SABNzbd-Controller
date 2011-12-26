package com.gmail.at.faint545.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Toast;

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.databases.RemoteDatabase;
import com.gmail.at.faint545.fragments.NewRemoteFragment;
import com.gmail.at.faint545.fragments.NewRemoteFragment.NewRemoteListener;
import com.gmail.at.faint545.fragments.RemoteFragment;
import com.gmail.at.faint545.fragments.RemoteFragment.RemoteFragmentListener;
import com.gmail.at.faint545.zxing.IntentIntegrator;
import com.gmail.at.faint545.zxing.IntentResult;

public class ControllerActivity extends FragmentActivity implements NewRemoteListener, RemoteFragmentListener {
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
		
		if(remotes.size() < cur.getCount()) { // If a new remote has been added, move to that remote in the database
			cur.moveToPosition(remotes.size()-1);
		}
		else { // A remote has been updated
			remotes.clear();
		}
		
		while(cur.moveToNext()) { // Collect all columns from each row and process it
			String id = cur.getString(RemoteDatabase.ID_INDEX);
			String name = cur.getString(RemoteDatabase.NAME_INDEX);
			String address = cur.getString(RemoteDatabase.ADDR_INDEX);
			String port = cur.getString(RemoteDatabase.PORT_INDEX);
			String apiKey = cur.getString(RemoteDatabase.API_KEY_INDEX);
			remotes.add(new Remote(name).setAddress(address).setPort(port).setApiKey(apiKey).setId(id));
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
	 * This function is called when a user edits a remote or
	 * creates a new one.
	 */
	public void launchNewRemoteFragment(Remote remote) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				
		ft.setCustomAnimations(R.anim.fragment_slide_left_enter, // Set custom transition animations
										 R.anim.fragment_slide_left_exit,
										 R.anim.fragment_slide_right_enter,
										 R.anim.fragment_slide_right_exit);
		
		ft.replace(R.id.main_framelayout, newRemoteFragment = new NewRemoteFragment(remote));
		ft.addToBackStack(null);
		ft.commit();
	}
	
	/*
	 * This function is called when QR button is clicked
	 */
	public void launchQrScanner(View view) {
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan();
	}
	
	/////////////////////////////////////
	// Some callbacks...
	////////////////////////////////////
	
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
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if(result != null && result.getContents() != null) {
			newRemoteFragment.populateApiKey(result.getContents());
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onEditRemote(int position) {
		launchNewRemoteFragment(remotes.get(position));
	}

	@Override
	public void onAddRemoteClicked() {
		launchNewRemoteFragment(null);
	}

	@Override
	public void onDeleteRemote(int position) {
		RemoteDatabase db = new RemoteDatabase(this);
		db.open();
		long result = db.delete(Integer.parseInt(remotes.get(position).getId()));
		db.close();
		if(result > 0) {
			remotes.remove(position);
			remoteFragment.notifyDataSetChanged();			
			Toast.makeText(this, "Delete successful!", Toast.LENGTH_SHORT).show();
		}				
	}
}