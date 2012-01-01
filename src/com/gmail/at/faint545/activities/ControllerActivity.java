package com.gmail.at.faint545.activities;

import android.content.Intent;
import android.os.Bundle;
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
	private RemoteFragment remoteFragment; // Fragment for viewing a remote
	private NewRemoteFragment newRemoteFragment; // Fragment for creating a remote	

	@Override
	public void onCreate(Bundle savedInstanceState) {    	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);        
		BugSenseHandler.setup(this, "42fc347a");
		setupActionBar();		
	}

	 /* A helper function to setup the Action Bar */
	private void setupActionBar() {
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		
		remoteFragment = RemoteFragment.newInstance();
		getSupportFragmentManager().beginTransaction().add(R.id.main_framelayout, remoteFragment).commit(); // Attach RemoteFragment to this activity
	}

	/*
	 * This function is called when a user edits a remote or
	 * creates a new one.
	 */
	public void launchNewRemoteFragment(Remote remote) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		/* Set custom transition animations */
		ft.setCustomAnimations(R.anim.fragment_slide_left_enter, R.anim.fragment_slide_left_exit, R.anim.fragment_slide_right_enter, R.anim.fragment_slide_right_exit);

		ft.replace(R.id.main_framelayout, newRemoteFragment = NewRemoteFragment.newInstance(remote));
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

	/* CALLBACK METHODS */

	/* This function is called when a remote has been saved */
	@Override
	public void onRemoteSaved() {
		remoteFragment.loadProfiles();
		getSupportFragmentManager().popBackStack(); // Pop off the last saved state
		newRemoteFragment = null;
	}	

	/* This function is called when a user wants to edit a remote */
	@Override
	public void onEditRemote(Remote targetRemote) {
		launchNewRemoteFragment(targetRemote);
	}

	/* This function is called when a user want to add a remote */
	@Override
	public void onAddRemoteClicked() {
		launchNewRemoteFragment(null);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if(result != null && result.getContents() != null) {
			newRemoteFragment.populateApiKey(result.getContents());
		}
		super.onActivityResult(requestCode, resultCode, data);
	}	
}