package com.gmail.at.faint545.activities;

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
import android.util.Log;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import com.gmail.at.faint545.QueueDownloadTask;
import com.gmail.at.faint545.QueueDownloadTask.DataDownloadTaskListener;
import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.fragments.RemoteHistoryFragment;
import com.gmail.at.faint545.fragments.RemoteQueueFragment;

public class RemoteDetailsActivity extends FragmentActivity implements DataDownloadTaskListener {
	private TabHost mTabHost;
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;
	private Remote mRemote;
	private QueueDownloadTask queueDownload;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mRemote = (Remote) getIntent().getParcelableExtra("selected_remote");
		downloadData(); // Begin downloading history & queue
		setContentView(R.layout.remote_details);		
		setupTabs();		
		super.onCreate(savedInstanceState);
	}

	private void downloadData() {
		queueDownload = new QueueDownloadTask(this, mRemote.buildURL(),mRemote.getApiKey());
		queueDownload.execute();
	}

	private void setupTabs() {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();
		
		mViewPager = (ViewPager) findViewById(R.id.remote_details_pager);
		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
		
		View queueIndicator = getLayoutInflater().inflate(R.layout.queue_tab_indicator, null);
		View historyIndicator = getLayoutInflater().inflate(R.layout.history_tab_indicator, null);
		
		mTabsAdapter.add(mTabHost.newTabSpec("queue").setIndicator(queueIndicator),new RemoteQueueFragment(),null);
		mTabsAdapter.add(mTabHost.newTabSpec("history").setIndicator(historyIndicator),new RemoteHistoryFragment(),null);
	}
	
	/**************************
	 * Tabs adapter subclass
	 **************************/	
	public static class TabsAdapter extends FragmentPagerAdapter implements OnTabChangeListener, OnPageChangeListener {
		private final Context mContext;
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
			mContext = activity;
			mTabHost = tabHost;
			mViewPager = pager;
			mTabHost.setOnTabChangedListener(this);
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}
		
		public void add(TabHost.TabSpec tabSpec,Fragment fragment, Bundle args) {
			tabSpec.setContent(new DummyTabFactory(mContext));
			
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
			mTabHost.setCurrentTab(position);
		}

		@Override
		public void onPageScrollStateChanged(int state) {}

		@Override
		public void onTabChanged(String tag) {
			int position = mTabHost.getCurrentTab();
			mViewPager.setCurrentItem(position);
		}
	}

	@Override
	public void onQueueDownloadFinished(String result) {
		queueDownload = null;
		if(result == null) {
			showAlertDialog();
		}
		else {
			Bundle arguments = new Bundle();
			arguments.putString("data", result);			
			mTabsAdapter.getItem(0).setArguments(arguments);
		}
	}

	private void showAlertDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("There was an error. Please check your connection and try again.");
		builder.setCancelable(false);
		builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {				
			@Override
			public void onClick(DialogInterface dialog, int which) {
				downloadData();
			}
		});
		builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});			
		builder.create().show();
	}
}