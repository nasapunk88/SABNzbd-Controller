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
package com.gmail.at.faint545.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.ListFragment;
import android.support.v4.app.SupportActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.SabnzbdConstants;
import com.gmail.at.faint545.adapters.RemoteQueueAdapter;
import com.gmail.at.faint545.services.AlarmReciever;
import com.gmail.at.faint545.tasks.QueueActionTask;
import com.gmail.at.faint545.tasks.QueueActionTask.QueueActionTaskListener;
import com.gmail.at.faint545.tasks.QueueDownloadTask;
import com.gmail.at.faint545.tasks.QueueDownloadTask.QueueDownloadTaskListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class QueueFragment extends ListFragment implements QueueActionTaskListener,QueueDownloadTaskListener {
	private RemoteQueueAdapter mAdapter;
	private ArrayList<JSONObject> mJobs = new ArrayList<JSONObject>();
	private ArrayList<Integer> mSelectedPositions = new ArrayList<Integer>();
	private PullToRefreshListView mPtrView;
	private ViewStub loadingStub;
	private QueueFragmentListener mFragmentListener;
	private Calendar updateTime;
	private TextView timeLeft, globalSpeed;
	
	public final static int DELETE = 0x321;
	public final static int PAUSE = DELETE >> 1;
	public final static int RESUME = PAUSE >> 1;
	
	/*
	 * A handler to handle an incoming message. More specifically,
	 * this will trigger when the queue download service completed
	 * and the message will be the new queue data.
	 */
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			String results = msg.getData().getString("results");
			try {
				JSONObject object = new JSONObject(results).getJSONObject(SabnzbdConstants.MODE_QUEUE);				
				updateFooterView(object);
				JSONArray array = object.getJSONArray(SabnzbdConstants.SLOTS);
				
				mJobs.clear();
				for(int x = 0; x < array.length(); x++) {
					mJobs.add(array.getJSONObject(x));
				}
				
				if(mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
				
				if(loadingStub.getVisibility() == View.VISIBLE) {
					loadingStub.setVisibility(View.GONE);
				}
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			super.handleMessage(msg);
		}		
	};
	
	public interface QueueFragmentListener {
		public void onConnectionError(String result);
	}
	
	/*
	 * Default constructor
	 */
	public static QueueFragment newInstance(Remote mRemote) {
		QueueFragment self = new QueueFragment();
		Bundle args = new Bundle();
		args.putParcelable("remote", mRemote);
		self.setArguments(args);
		return self;
	}	
	
	@Override
	public void onAttach(SupportActivity activity) {
		mFragmentListener = (QueueFragmentListener) activity;
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.remote_queue, null);
		mPtrView = (PullToRefreshListView) view.findViewById(R.id.remote_queue_ptr);
		loadingStub = (ViewStub) view.findViewById(R.id.loading);
		return view;
	}
		
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {		
		loadingStub.setVisibility(View.VISIBLE); // Make progress bar visible
		setRecurringAlarm();
		setupListView();
		setupListAdapter();
		initListeners();
		super.onActivityCreated(savedInstanceState);
	}

	private void setupListView() {
		getListView().setCacheColorHint(Color.TRANSPARENT); // Optimization for ListView
		
		// Attach a footer view
		View footer = getActivity().getLayoutInflater().inflate(R.layout.remote_queue_footer, null);
		getListView().addFooterView(footer);
	}
	
	private void downloadQueue(Object viewToUse) {
		new QueueDownloadTask(this, getRemote().buildURL(),getRemote().getApiKey(),viewToUse).execute();		
	}	
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		// Only show these options if there are jobs to delete
		if(mSelectedPositions.size() > 0 && mJobs.size() > 0) {
			menu.add(Menu.NONE,DELETE,Menu.NONE,"Delete selected");
			menu.add(Menu.NONE,PAUSE,Menu.NONE,"Pause selected");
			menu.add(Menu.NONE,RESUME,Menu.NONE,"Resume selected");
		}
		else if(mJobs.size() > 0){
			menu.add(Menu.NONE,DELETE,Menu.NONE,"Delete all");
			menu.add(Menu.NONE,PAUSE,Menu.NONE,"Pause all");
			menu.add(Menu.NONE,RESUME,Menu.NONE,"Resume all");
		}
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Remote currentRemote = getActivity().getIntent().getParcelableExtra("selected_remote");
		String selectedJobs = null;
		if(mSelectedPositions.size() > 0) {
			selectedJobs = collectSelectedItems();
		}
		switch(item.getItemId()) {
			case DELETE:				
				new QueueActionTask(this, currentRemote.buildURL(), currentRemote.getApiKey(),QueueActionTask.DELETE).execute(selectedJobs);			
			break;
			case PAUSE:
				new QueueActionTask(this, currentRemote.buildURL(), currentRemote.getApiKey(),QueueActionTask.PAUSE).execute(selectedJobs);
			break;
			case RESUME:
				new QueueActionTask(this, currentRemote.buildURL(), currentRemote.getApiKey(),QueueActionTask.RESUME).execute(selectedJobs);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private String collectSelectedItems() {
		StringBuilder selectedJobs;
		selectedJobs = new StringBuilder();
		
		// Create a string of jobs to delete, separated by commas i.e: job1,job2,job3
		for(int position : mSelectedPositions) {				
			JSONObject job = mJobs.get(position);
			try {
				String id = job.getString(SabnzbdConstants.NZOID);
				selectedJobs.append(id).append(",");
			} 
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return selectedJobs.substring(0, selectedJobs.lastIndexOf(","));
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if(position < getListView().getCount()-1) {
			CheckBox checkbox = (CheckBox) v.findViewById(R.id.remote_queue_checkbox);
			checkbox.toggle(); // Toggle the check box
			
			// Add or remove the current position from our list of selected positions
			try {
				if(checkbox.isChecked()) {
					mJobs.get(position).put("checked", true);
					mSelectedPositions.add(position);
					mSelectedPositions.trimToSize();
				}
				else {
					mJobs.get(position).put("checked", false);
					mSelectedPositions.remove((Object) position);
					mSelectedPositions.trimToSize();
				}
			}
			catch(JSONException e) {
				e.printStackTrace();
			}
		}
		super.onListItemClick(l, v, position, id);
	}

	/*
	 * Default implementation for initializing listeners for views
	 */
	private void initListeners() {
		mPtrView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				downloadQueue(mPtrView);
			}
		});
	}

	/*
	 * A helper function to setup the list adapter
	 */	
	private void setupListAdapter() {
		mAdapter = new RemoteQueueAdapter(getActivity(), R.layout.remote_queue_row, mJobs);
		setListAdapter(mAdapter);
	}

	private void updateFooterView(JSONObject object) throws JSONException {
		if(timeLeft == null) {
			timeLeft = (TextView) getView().findViewById(R.id.remote_queue_timeleft);
		}
		
		if(globalSpeed == null) {
			globalSpeed = (TextView) getView().findViewById(R.id.remote_queue_speed);
		}
		
		globalSpeed.setText(object.getString(SabnzbdConstants.SPEED));
		timeLeft.setText(object.getString(SabnzbdConstants.TIMELEFT));
	}

	@Override
	public void onQueueDeleteFinished(String result) {
		validateResults(result);		
	}

	@Override
	public void onQueuePauseFinished(String result) {
		validateResults(result);
	}

	@Override
	public void onQueueResumeFinished(String result) {
		validateResults(result);	
	}
	
	private void validateResults(String result) {
		try {
			String status = new JSONObject(result).getString(SabnzbdConstants.STATUS);
			if(Boolean.parseBoolean(status)){
				downloadQueue(ProgressDialog.show(getActivity(), null, "Loading data"));
				mSelectedPositions.clear();
			}
			else {
				Toast.makeText(getActivity(), R.string.generic_error, Toast.LENGTH_SHORT).show();
			}
		} 
		catch (JSONException e) {
			if(result.equals(ClientProtocolException.class.getName()) || result.equals(IOException.class.getName())) {
				buildAlertDialog().show();
			}
		}
	}
	
	private AlertDialog buildAlertDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.connect_error);
		builder.setCancelable(false);
		builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {}
		});			
		return builder.create();
	}

	@Override
	public void onSpeedLimitFinished(String result) {}
	
	private Remote getRemote() {
		return getArguments().getParcelable("remote");
	}

	@Override
	public void onQueueDownloadFinished(String result) {		
		if(result.equals(ClientProtocolException.class.getName()) || result.equals(IOException.class.getName())) {
			mFragmentListener.onConnectionError(result);
		}
		else {
			try {
				JSONObject object = new JSONObject(result).getJSONObject(SabnzbdConstants.MODE_QUEUE);				
				updateFooterView(object);
				JSONArray array = object.getJSONArray(SabnzbdConstants.SLOTS);
				mJobs.clear();
				for(int x = 0; x < array.length(); x++) {
					mJobs.add(array.getJSONObject(x));
				}
				
				if(mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
				
				if(loadingStub.getVisibility() == View.VISIBLE) {
					loadingStub.setVisibility(View.GONE); // Hide progressbar
				}
			} 
			catch (JSONException e) {
				mFragmentListener.onConnectionError(result);
			}
		}
	}
	
	/*
	 * Set a recurring alarm to trigger a service to download the latest 
	 * queue data. If user turned off auto refresh, just download the data
	 * and don't set an alarm.
	 */
	private void setRecurringAlarm() {
		if(getRemote().getRefreshInterval() != -1) {
			updateTime = Calendar.getInstance();
	    updateTime.setTimeZone(TimeZone.getTimeZone("GMT"));
	    updateTime.set(Calendar.MINUTE, 1);
	    
			Intent downloader = new Intent(getActivity(), AlarmReciever.class);
			downloader.putExtra("url", getRemote().buildURL());
			downloader.putExtra("api", getRemote().getApiKey());
			downloader.putExtra("messenger", new Messenger(handler));
			
			PendingIntent recurringDownload = PendingIntent.getBroadcast(getActivity(),0, downloader, PendingIntent.FLAG_CANCEL_CURRENT);
			
			AlarmManager alarms = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE); 
			alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis(),getRemote().getRefreshInterval(), recurringDownload);
		}
		else {
			downloadQueue(null);
		}
	}
}