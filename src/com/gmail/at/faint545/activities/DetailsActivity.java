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

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.fragments.HistoryFragment;
import com.gmail.at.faint545.fragments.HistoryFragment.HistoryFragmentListener;
import com.gmail.at.faint545.fragments.QueueFragment;
import com.gmail.at.faint545.fragments.QueueFragment.QueueFragmentListener;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitleProvider;
import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;

public class DetailsActivity extends FragmentActivity implements QueueFragmentListener,HistoryFragmentListener{
	private ViewPager mViewPager;
	private TabsAdapter mAdapter;
	private TitlePageIndicator mTitleIndicator;
	private Remote mRemote;
	private AlertDialog errorDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mRemote = (Remote) getIntent().getParcelableExtra("selected_remote"); // Retrieved the selected remote
		setContentView(R.layout.remote_details);		
		setupIndicator();
		super.onCreate(savedInstanceState);
	}

	/*
	 * A helper function to create/add tabs to the tab host
	 */
	private void setupIndicator() {
		mAdapter = new TabsAdapter(getSupportFragmentManager());
		String queueTitle = getString(R.string.queue_title);
		String historyTitle = getString(R.string.history_title);
		
		mAdapter.add(QueueFragment.newInstance(mRemote),queueTitle);
		mAdapter.add(HistoryFragment.newInstance(mRemote),historyTitle);
		
		mViewPager = (ViewPager) findViewById(R.id.remote_details_pager);
		mViewPager.setAdapter(mAdapter);
		
		mTitleIndicator = (TitlePageIndicator) findViewById(R.id.remote_details_indicator);	
		mTitleIndicator.setViewPager(mViewPager);
		mTitleIndicator.setFooterColor(getResources().getColor(R.color.yellow));
		mTitleIndicator.setFooterIndicatorStyle(IndicatorStyle.Triangle);
	}
	
	/* A helper function to build an alert/error dialog */
	private AlertDialog buildAlertDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setCancelable(false);
		builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});			
		return builder.create();
	}

	/* Tabs adapter subclass */	
	public static class TabsAdapter extends FragmentPagerAdapter implements TitleProvider {
		private ArrayList<TitleFragment> mFrag = new ArrayList<TitleFragment>();
		
		/* A custom class to relate a Fragment to a title for TitlePageIndicator */
		private static class TitleFragment {
			public String title;
			public Fragment frag;
			
			public TitleFragment(Fragment f, String title) {
				this.title = title;
				frag = f;
			}
		}
		
		public TabsAdapter(FragmentManager supportFragmentManager) {
			super(supportFragmentManager);
		}
		
		public void add(Fragment fragment,String title) {
			mFrag.add(new TitleFragment(fragment, title));
		}

		@Override
		public Fragment getItem(int position) {
			return mFrag.get(position).frag;
		}

		@Override
		public int getCount() {
			return mFrag.size();
		}

		@Override
		public String getTitle(int position) {
			return mFrag.get(position).title;
		}
	}

	@Override
	public void onConnectionError(String results) {
		String error = null;
		try {
			JSONObject result = new JSONObject(results);
			if(result.has("error")) {
				error = getString(R.string.error_prefix) + result.getString("error");
			}
			else {
				error = getString(R.string.connect_error);
			}
		}
		catch(JSONException e) {
			error = getString(R.string.connect_error);
		}
		
		if(errorDialog == null) {
			errorDialog = buildAlertDialog(error);
		}
		else {
			errorDialog.setMessage(error);
		}
		
		if(!errorDialog.isShowing()) {
			errorDialog.show();
		}
	}
}