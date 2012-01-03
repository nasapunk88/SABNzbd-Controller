/* 
 * Copyright 2011 Alex Fu
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 		
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gmail.at.faint545.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

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
	private static final String REMOTE_FRAGMENT_TAG = "rf";
	private static final String NEW_REMOTE_FRAGMENT_TAG = "nrf";
	private RemoteFragment remoteFragment; // Fragment for viewing a remote

	@Override
	public void onCreate(Bundle savedInstanceState) {    	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);        
		BugSenseHandler.setup(this, "42fc347a"); // Used for BugSense
		setupActionBar();		
	}

	 /* A helper function to setup the Action Bar */
	private void setupActionBar() {
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		
		remoteFragment = RemoteFragment.newInstance();
		getSupportFragmentManager().beginTransaction().add(R.id.main_framelayout, remoteFragment,REMOTE_FRAGMENT_TAG).commit(); // Attach RemoteFragment to this activity
	}

	/* This function is called when a user edits a remote or creates a new one. */
	public void launchNewRemoteFragment(Remote remote) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		/* Set custom transition animations */
		ft.setCustomAnimations(R.anim.fragment_slide_left_enter, R.anim.fragment_slide_left_exit, R.anim.fragment_slide_right_enter, R.anim.fragment_slide_right_exit);

		ft.replace(R.id.main_framelayout, NewRemoteFragment.newInstance(remote),NEW_REMOTE_FRAGMENT_TAG);
		ft.addToBackStack(null);
		ft.commit();
	}

	/* CALLBACK METHODS */

	/* This function is called when a remote has been saved */
	@Override
	public void onRemoteSaved() {
		remoteFragment.loadProfiles();
		getSupportFragmentManager().popBackStack(); // Pop off the last saved state
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
	
	/* We are handling the results from the QR scan here */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if(result != null && result.getContents() != null) {
			((NewRemoteFragment) getSupportFragmentManager().findFragmentByTag(NEW_REMOTE_FRAGMENT_TAG)).populateApiKey(result.getContents());
		}
		super.onActivityResult(requestCode, resultCode, data);
	}	
}