package com.gmail.at.faint545.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.bugsense.trace.BugSenseHandler;
import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.fragments.NewRemoteFragment;
import com.gmail.at.faint545.fragments.NewRemoteFragment.NewRemoteListener;
import com.gmail.at.faint545.fragments.RemoteFragment;
import com.gmail.at.faint545.fragments.RemoteFragment.RemoteFragmentListener;
import com.gmail.at.faint545.zxing.IntentIntegrator;
import com.gmail.at.faint545.zxing.IntentResult;

public class ControllerActivity extends FragmentActivity implements NewRemoteListener, RemoteFragmentListener {	
	private RemoteFragment remoteFragment; // Fragment for viewing remotes
	private NewRemoteFragment newRemoteFragment; // Fragment for creating a remote	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);        
        BugSenseHandler.setup(this, "42fc347a");
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(false); // Remove activity/application icon
        attachFragment(remoteFragment = RemoteFragment.newInstance()); // Attach RemoteFragment to this activity
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
	
	// Below here are callback functions
	
	/*
	 * This function is called when a remote has been saved
	 */
	@Override
	public void onRemoteSaved() {
		remoteFragment.loadRemotes();
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

	/*
	 * Called when a user wants to edit a remote via long press
	 */
	@Override
	public void onEditRemote(Remote targetRemote) {
		launchNewRemoteFragment(targetRemote);
	}

	/*
	 * Called when a user wants to add a remote by clicking the
	 * "Add Remote" button
	 */
	@Override
	public void onAddRemoteClicked() {
		launchNewRemoteFragment(null);
	}
}