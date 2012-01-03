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

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.fragments.HistoryFragment;
import com.gmail.at.faint545.fragments.HistoryFragment.HistoryFragmentListener;
import com.gmail.at.faint545.fragments.QueueFragment;
import com.gmail.at.faint545.fragments.QueueFragment.QueueFragmentListener;

public class DetailsActivity extends FragmentActivity implements QueueFragmentListener,HistoryFragmentListener{
	private TabHost mTabHost;
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;
	private Remote mRemote;
	private AlertDialog errorDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mRemote = (Remote) getIntent().getParcelableExtra("selected_remote"); // Retrieved the selected remote
		setContentView(R.layout.remote_details);		
		setupTabs();
		super.onCreate(savedInstanceState);
	}

	/*
	 * A helper function to create/add tabs to the tab host
	 */
	private void setupTabs() {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();
		
		mViewPager = (ViewPager) findViewById(R.id.remote_details_pager);
		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
		
		TextView queueIndicator = (TextView) getLayoutInflater().inflate(R.layout.tab_indicator, null);
		TextView historyIndicator = (TextView) getLayoutInflater().inflate(R.layout.tab_indicator, null);
		
		queueIndicator.setText(R.string.queue_tab_title);
		historyIndicator.setText(R.string.history_tab_title);
		
		mTabsAdapter.add(mTabHost.newTabSpec("queue").setIndicator(queueIndicator),QueueFragment.newInstance(mRemote),null);
		mTabsAdapter.add(mTabHost.newTabSpec("history").setIndicator(historyIndicator),HistoryFragment.newInstance(mRemote),null);
	}
	
	/* A helper function to build an alert/error dialog */
	private AlertDialog buildAlertDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.connect_error);
		builder.setCancelable(false);
		builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});			
		return builder.create();
	}

	@Override
	public void onConnectionError() {
		if(errorDialog == null) {
			errorDialog = buildAlertDialog();
		}
		
		if(!errorDialog.isShowing()) {
			errorDialog.show();
		}
	}		

	/* Tabs adapter subclass */	
	public static class TabsAdapter extends FragmentPagerAdapter implements OnTabChangeListener, OnPageChangeListener {
		private final WeakReference<Context> mWeakContext;
		private final TabHost mTabHost;
		private final ViewPager mViewPager;
		private final ArrayList<Fragment> mTabs = new ArrayList<Fragment>();
		
		static class DummyTabFactory implements TabHost.TabContentFactory {
			private final Context mContext;
			
			public DummyTabFactory(Context context) {
				mContext = context;
			}

			@Override
			public View createTabContent(String tag) {
				View v = new View(mContext);
				v.setMinimumHeight(0);
				v.setMinimumWidth(0);
				return v;
			}
		}
		
		public TabsAdapter(FragmentActivity activity, TabHost tabHost, ViewPager pager) {
			super(activity.getSupportFragmentManager());
			mWeakContext = new WeakReference<Context>(activity);
			mTabHost = tabHost;
			mViewPager = pager;
			mTabHost.setOnTabChangedListener(this);
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}
		
		public void add(TabHost.TabSpec tabSpec,Fragment fragment, Bundle args) {
			tabSpec.setContent(new DummyTabFactory(mWeakContext.get()));
			
			mTabs.add(fragment);
			mTabHost.addTab(tabSpec);
			notifyDataSetChanged();
		}

		@Override
		public Fragment getItem(int position) {
			if(mTabs.get(position) != null) {
				return mTabs.get(position);
			}
			else return null;
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,int positionOffsetPixels) {}

		@Override
		public void onPageSelected(int position) {
			((FragmentActivity) mWeakContext.get()).invalidateOptionsMenu();
			mTabHost.setCurrentTab(position);
		}

		@Override
		public void onPageScrollStateChanged(int state) {}

		@Override
		public void onTabChanged(String tag) {
			((FragmentActivity) mWeakContext.get()).invalidateOptionsMenu();
			int position = mTabHost.getCurrentTab();
			mViewPager.setCurrentItem(position);
		}
	}
}