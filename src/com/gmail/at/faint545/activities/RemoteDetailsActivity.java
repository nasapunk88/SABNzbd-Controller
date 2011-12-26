package com.gmail.at.faint545.activities;

import java.util.ArrayList;

import android.content.Context;
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

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.fragments.RemoteHistoryFragment;
import com.gmail.at.faint545.fragments.RemoteQueueFragment;

public class RemoteDetailsActivity extends FragmentActivity {
	private TabHost mTabHost;
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.remote_details);
		setupTabs();		
		super.onCreate(savedInstanceState);
	}

	private void setupTabs() {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();
		
		mViewPager = (ViewPager) findViewById(R.id.remote_details_pager);
		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
		
		View queueIndicator = getLayoutInflater().inflate(R.layout.queue_tab_indicator, null);
		View historyIndicator = getLayoutInflater().inflate(R.layout.history_tab_indicator, null);
		
		mTabsAdapter.add(mTabHost.newTabSpec("queue").setIndicator(queueIndicator),RemoteQueueFragment.class,null);
		mTabsAdapter.add(mTabHost.newTabSpec("history").setIndicator(historyIndicator),RemoteHistoryFragment.class,null);
	}
	
	/////////////////////////////////////
	// Tabs adapter subclass
	////////////////////////////////////	
	public static class TabsAdapter extends FragmentPagerAdapter implements OnTabChangeListener, OnPageChangeListener {
		private final Context mContext;
		private final TabHost mTabHost;
		private final ViewPager mViewPager;
		private final ArrayList<String> mTabs = new ArrayList<String>();
		
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
		
		public void add(TabHost.TabSpec tabSpec,Class<?> clss, Bundle args) {
			tabSpec.setContent(new DummyTabFactory(mContext));
			
			mTabs.add(clss.getName());
			mTabHost.addTab(tabSpec);
			notifyDataSetChanged();
		}

		@Override
		public Fragment getItem(int position) {
			Log.d("FOLLOW ME","getItem() " + mTabs.get(position));
			return Fragment.instantiate(mContext, mTabs.get(position),null);
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
		public void onTabChanged(String arg0) {}
	}
}