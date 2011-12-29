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
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import com.gmail.at.faint545.HistoryDownloadTask;
import com.gmail.at.faint545.HistoryDownloadTask.HistoryDownloadTaskListener;
import com.gmail.at.faint545.QueueDownloadTask;
import com.gmail.at.faint545.QueueDownloadTask.DataDownloadTaskListener;
import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.fragments.RemoteHistoryFragment;
import com.gmail.at.faint545.fragments.RemoteHistoryFragment.RemoteHistoryListener;
import com.gmail.at.faint545.fragments.RemoteQueueFragment.RemoteQueueListener;
import com.gmail.at.faint545.fragments.RemoteQueueFragment;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class RemoteDetailsActivity extends FragmentActivity implements DataDownloadTaskListener,
																	   HistoryDownloadTaskListener,
																	   RemoteHistoryListener,
																	   RemoteQueueListener{
	private TabHost mTabHost;
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;
	private Remote mRemote;
	private AlertDialog errorDialog;
	private PullToRefreshListView mHistoryPtrView,mQueuePtrView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mRemote = (Remote) getIntent().getParcelableExtra("selected_remote"); // Retrieved the selected remote
		downloadData(); // Begin downloading history & queue
		setContentView(R.layout.remote_details);		
		setupTabs();
		super.onCreate(savedInstanceState);
	}

	/*
	 * A helper function to begin executing the tasks needed to download
	 * the queue and history
	 */
	private void downloadData() {
		downloadQueue();
		downloadHistory();
		errorDialog = buildAlertDialog();
	}
	
	private void downloadQueue() {
		new QueueDownloadTask(this, mRemote.buildURL(),mRemote.getApiKey(),mQueuePtrView).execute();
	}
	
	private void downloadHistory() {
		new HistoryDownloadTask(this, mRemote.buildURL(), mRemote.getApiKey(),mHistoryPtrView).execute();
	}

	/*
	 * A helper function to create/add tabs to the tab host
	 */
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

	/*
	 * Tabs adapter subclass
	 */	
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
			((FragmentActivity) mContext).invalidateOptionsMenu();
			mTabHost.setCurrentTab(position);
		}

		@Override
		public void onPageScrollStateChanged(int state) {}

		@Override
		public void onTabChanged(String tag) {
			((FragmentActivity) mContext).invalidateOptionsMenu();
			int position = mTabHost.getCurrentTab();
			mViewPager.setCurrentItem(position);
		}
	}

	/*
	 * A callback function for when the queue has finished downloading. #ref: QueueDownloadTask.java
	 */
	@Override
	public void onQueueDownloadFinished(String result) {
		if(result == null) {
			if(!errorDialog.isShowing()) {
				errorDialog.show();
			}
		}
		else {
			Bundle arguments = new Bundle();
			arguments.putString("data", result);			
			mTabsAdapter.getItem(0).setArguments(arguments);
		}
	}
	
	/*
	 * A callback function for when the history has finished downloading. #ref: HistoryDownloadTask.java  
	 */
	@Override
	public void onHistoryDownloadFinished(String result) {
		if(result == null) {
			if(!errorDialog.isShowing()) {
				errorDialog.show();
			}
		}
		else {
			Bundle arguments = new Bundle();
			arguments.putString("data", result);
			mTabsAdapter.getItem(1).setArguments(arguments);
		}
	}

	/*
	 * A helper function to build an alert/error dialog
	 */
	private AlertDialog buildAlertDialog() {
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
		return builder.create();
	}
	
	/*
	 * A callback function for when a user "pulls-to-refresh" on the History tab. #ref: RemoteHistoryFragment.java
	 */
	@Override
	public void onRefreshHistory(PullToRefreshListView view) {
		mHistoryPtrView = view;
		downloadHistory();
	}

	@Override
	public void onRefreshQueue(PullToRefreshListView view) {
		mQueuePtrView = view;
		downloadQueue();
	}
}