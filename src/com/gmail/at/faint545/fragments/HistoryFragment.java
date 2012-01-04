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

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.SabnzbdConstants;
import com.gmail.at.faint545.adapters.RemoteHistoryAdapter;
import com.gmail.at.faint545.tasks.HistoryActionTask;
import com.gmail.at.faint545.tasks.HistoryActionTask.HistoryActionTaskListener;
import com.gmail.at.faint545.tasks.HistoryDownloadTask;
import com.gmail.at.faint545.tasks.HistoryDownloadTask.HistoryDownloadTaskListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class HistoryFragment extends ListFragment implements HistoryActionTaskListener,HistoryDownloadTaskListener {
	private RemoteHistoryAdapter mAdapter;
	private ArrayList<JSONObject> mOldJobs = new ArrayList<JSONObject>();
	private ArrayList<Integer> mSelectedPositions = new ArrayList<Integer>();
	private PullToRefreshListView mPtrView;
	private ViewStub loadingStub;
	private HistoryFragmentListener mFragmentListener;
	
	public final static int DELETE = 0x123;
	
	public interface HistoryFragmentListener {
		public void onConnectionError(String result);
	}
	
	/* Default constructor */
	public static HistoryFragment newInstance(Remote mRemote) {
		HistoryFragment self = new HistoryFragment();
		Bundle args = new Bundle();
		args.putParcelable("remote", mRemote);
		self.setArguments(args);
		return self;
	}
	
	@Override
	public void onAttach(Activity activity) {
		mFragmentListener = (HistoryFragmentListener) activity;
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);			
		super.onCreate(savedInstanceState);
	}	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.remote_history, null);
		mPtrView = (PullToRefreshListView) view.findViewById(R.id.remote_history_ptr);
		loadingStub = (ViewStub) view.findViewById(R.id.loading);
		
		mPtrView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				manualRefreshHistory(mPtrView);
			}			
		});		
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		refreshHistory(); // Begin downloading
		getListView().setCacheColorHint(Color.TRANSPARENT); // Optimization for ListView
		setupListAdapter();
		super.onActivityCreated(savedInstanceState);
	}
	
	/* A function to trigger an automatic history download. */
	private void refreshHistory() {
		displayLoadingAnim();
		mOldJobs.clear();
		new HistoryDownloadTask(this, getRemote().buildURL(), getRemote().getApiKey(),null).execute();
	}
	
	private void manualRefreshHistory(PullToRefreshListView view) {
		mOldJobs.clear();
		new HistoryDownloadTask(this, getRemote().buildURL(), getRemote().getApiKey(),view).execute();
	}

	/* A helper function to setup the list adapter */
	private void setupListAdapter() {
		mAdapter = new RemoteHistoryAdapter(getActivity(), R.layout.remote_history_row, mOldJobs);
		setListAdapter(mAdapter);
	}	
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		/* Only show these two options if there are jobs to delete */
		if(mSelectedPositions.size() > 0 && mOldJobs.size() > 0) {
			menu.add(Menu.NONE,DELETE,Menu.NONE,"Delete selected");
		}
		else if(mOldJobs.size() > 0){
			menu.add(Menu.NONE,DELETE,Menu.NONE,"Delete all");
		}
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Remote currentRemote = getActivity().getIntent().getParcelableExtra("selected_remote");
		String selectedJobs = null;
		if(mSelectedPositions.size() > 0) selectedJobs = collectSelectedItems();
		displayLoadingAnim();
		new HistoryActionTask(this, currentRemote.buildURL(), currentRemote.getApiKey(),item.getItemId()).execute(selectedJobs);		
		return super.onOptionsItemSelected(item);
	}
	
	/* A helper function to collect all the NZO IDs of all selected items/jobs */
	private String collectSelectedItems() {
		StringBuilder selectedJobs = new StringBuilder();
		/* Create a string of jobs to delete, separated by commas i.e: job1,job2,job3 */
		for(int position : mSelectedPositions) {				
			JSONObject job = mOldJobs.get(position);
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
	
	/* A handler to handle the event of a list item click */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		CheckBox checkbox = (CheckBox) v.findViewById(R.id.remote_history_checkbox);
		checkbox.toggle(); // Toggle the check box
		
		/* Add or remove the current position from our list of selected positions */
		try {
			if(checkbox.isChecked()) {
				mOldJobs.get(position).put("checked", true);
				mSelectedPositions.add(position);
				mSelectedPositions.trimToSize();
			}
			else {
				mOldJobs.get(position).put("checked", false);
				mSelectedPositions.remove((Object) position);
				mSelectedPositions.trimToSize();
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		super.onListItemClick(l, v, position, id);
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
	
	private Remote getRemote() {
		return getArguments().getParcelable("remote");
	}	

	private void displayLoadingAnim() {
		getListView().setVisibility(View.GONE);
		loadingStub.setVisibility(View.VISIBLE);
	}
	
	private void hideLoadingAnim() {
		getListView().setVisibility(View.VISIBLE);
		loadingStub.setVisibility(View.GONE);
	}		

	/* CALLBACK METHODS */
	
	/*
	 * A callback function for when the delete operation has completed. Refers to: DeleteHistoryTask.java
	 */
	@Override
	public void onHistoryDeleteFinished(String result) {
		try {
			String status = new JSONObject(result).getString(SabnzbdConstants.STATUS);
			if(Boolean.parseBoolean(status)) {
				refreshHistory();
				mSelectedPositions.clear();
			}
			else {
				Toast.makeText(getActivity(), R.string.generic_error, Toast.LENGTH_SHORT).show();
			}
		} 
		catch (JSONException e) {
			if(result.equals(ClientProtocolException.class.getName()) || result.equals(ClientProtocolException.class.getName())) {
				buildAlertDialog().show();
			}
		}
	}
	
	@Override
	public void onHistoryRetryFinished(String result) {}

	@Override
	public void onHistoryDownloadFinished(String result) {
		hideLoadingAnim();
		if(result.equals(ClientProtocolException.class.getName()) || result.equals(IOException.class.getName())) {
			mFragmentListener.onConnectionError(result);
		}
		else {
			try {
				JSONObject object = new JSONObject(result);
				object = object.getJSONObject(SabnzbdConstants.MODE_HISTORY);
				JSONArray array = object.getJSONArray(SabnzbdConstants.SLOTS);
				
				for(int x = 0; x < array.length(); x++) {
					mOldJobs.add(array.getJSONObject(x));
				}
				
				if(mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}						
			}
			catch(JSONException e) {
				e.printStackTrace();
			}
		}		
	}
}