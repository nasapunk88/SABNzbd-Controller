package com.gmail.at.faint545.fragments;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.SupportActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.gmail.at.faint545.QueueActionTask;
import com.gmail.at.faint545.QueueActionTask.QueueActionTaskListener;
import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.SabnzbdConstants;
import com.gmail.at.faint545.adapters.RemoteQueueAdapter;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class RemoteQueueFragment extends ListFragment implements QueueActionTaskListener {
	private RemoteQueueAdapter mAdapter;
	private ArrayList<JSONObject> mJobs = new ArrayList<JSONObject>();
	private ArrayList<Integer> mSelectedPositions = new ArrayList<Integer>();
	private PullToRefreshListView mPtrView;
	private RemoteQueueListener mListener;	
	
	public final static int DELETE_ALL = 0x321, DELETE_SELECTED = DELETE_ALL >> 1,
							PAUSE_ALL = DELETE_SELECTED >> 1, PAUSE_SELECTED = PAUSE_ALL >> 1,
							RESUME_ALL = PAUSE_SELECTED >> 1, RESUME_SELECTED = RESUME_ALL >> 1;
	
	public interface RemoteQueueListener {
		public void onRefreshQueue(PullToRefreshListView view);
		public void onQueueResumeFinished();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onAttach(SupportActivity activity) {
		mListener = (RemoteQueueListener) activity;
		super.onAttach(activity);
	}	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return inflater.inflate(R.layout.remote_queue, null);
	}
		
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mPtrView = (PullToRefreshListView) getView().findViewById(R.id.remote_queue_ptr);
		getListView().setCacheColorHint(Color.TRANSPARENT); // Optimization for ListView
		setupListAdapter();
		initListeners();
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		// Only show these two options if there are jobs to delete
		if(mSelectedPositions.size() > 0 && mJobs.size() > 0) {
			menu.add(Menu.NONE,DELETE_SELECTED,Menu.NONE,"Delete selected");
			menu.add(Menu.NONE,PAUSE_SELECTED,Menu.NONE,"Pause selected");
			menu.add(Menu.NONE,RESUME_SELECTED,Menu.NONE,"Resume selected");
		}
		else if(mJobs.size() > 0){
			menu.add(Menu.NONE,DELETE_ALL,Menu.NONE,"Delete all");
			menu.add(Menu.NONE,PAUSE_ALL,Menu.NONE,"Pause all");
			menu.add(Menu.NONE,RESUME_ALL,Menu.NONE,"Resume all");
		}
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Remote currentRemote = getActivity().getIntent().getParcelableExtra("selected_remote");
		StringBuilder stringBuilder = null;
		String selectedJobs = null;
		switch(item.getItemId()) {
			case DELETE_ALL:
				new QueueActionTask(this, currentRemote.buildURL(), currentRemote.getApiKey(),SabnzbdConstants.DELETE).execute(selectedJobs);
			break;
			case DELETE_SELECTED:
				stringBuilder = collectSelectedItems();
				selectedJobs = stringBuilder.substring(0, stringBuilder.lastIndexOf(",")); // Chop off the last comma
				new QueueActionTask(this, currentRemote.buildURL(), currentRemote.getApiKey(),SabnzbdConstants.DELETE).execute(selectedJobs);
			break;
			case PAUSE_ALL:
				addAllToSelectedPositions();
				stringBuilder = collectSelectedItems();			
				selectedJobs = stringBuilder.substring(0, stringBuilder.lastIndexOf(","));
				new QueueActionTask(this, currentRemote.buildURL(), currentRemote.getApiKey(),SabnzbdConstants.MODE_PAUSE).execute(selectedJobs);
			break;
			case PAUSE_SELECTED:
				stringBuilder = collectSelectedItems();			
				selectedJobs = stringBuilder.substring(0, stringBuilder.lastIndexOf(","));
				new QueueActionTask(this, currentRemote.buildURL(), currentRemote.getApiKey(),SabnzbdConstants.MODE_PAUSE).execute(selectedJobs);
			break;
			case RESUME_ALL:
				addAllToSelectedPositions();
				stringBuilder = collectSelectedItems();
				selectedJobs = stringBuilder.substring(0, stringBuilder.lastIndexOf(","));
				new QueueActionTask(this, currentRemote.buildURL(), currentRemote.getApiKey(),SabnzbdConstants.MODE_RESUME).execute(selectedJobs);
			break;
			case RESUME_SELECTED:
				stringBuilder = collectSelectedItems();
				selectedJobs = stringBuilder.substring(0, stringBuilder.lastIndexOf(","));
				new QueueActionTask(this, currentRemote.buildURL(), currentRemote.getApiKey(),SabnzbdConstants.MODE_RESUME).execute(selectedJobs);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * A helper function to add all of the indices of available jobs in the list
	 * to the list of selected positions. Useful for when we want to do a pause all,
	 * or resume all
	 */
	private void addAllToSelectedPositions() {
		for(int x = 0; x < getListView().getCount(); x++) {
			mSelectedPositions.add(x);
		}
	}

	private StringBuilder collectSelectedItems() {
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
		return selectedJobs;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		CheckBox checkbox = (CheckBox) v.findViewById(R.id.remote_queue_checkbox);
		checkbox.toggle(); // Toggle the checkbox
		
		// Add or remove the current position from our list of selected positions
		if(checkbox.isChecked()) {
			mSelectedPositions.add(position);
			mSelectedPositions.trimToSize();
		}
		else {
			mSelectedPositions.remove((Object) position);
			mSelectedPositions.trimToSize();
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
				mListener.onRefreshQueue(mPtrView);
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

	@Override
	public void setArguments(Bundle args) {
		String data = args.getString("data");
		mJobs.clear();
		if(data != null) {
			try {
				JSONObject object = new JSONObject(data);
				Log.d("TAG",data.toString());
				JSONArray array = object.getJSONObject(SabnzbdConstants.MODE_QUEUE).getJSONArray(SabnzbdConstants.SLOTS);
				
				for(int x = 0; x < array.length(); x++) {
					mJobs.add(array.getJSONObject(x));
				}
			} 
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if(mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}	
	}

	@Override
	public void onQueueDeleteFinished(String result) {
		try {
			String status = new JSONObject(result).getString(SabnzbdConstants.STATUS);
			if(Boolean.parseBoolean(status)){
				updateRemovedJobs();
				mSelectedPositions.clear();
				Toast.makeText(getActivity(), R.string.delete_success, Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(getActivity(), R.string.generic_error, Toast.LENGTH_SHORT).show();
			}
		} 
		catch (JSONException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void onQueuePauseFinished(String result) {
		try {
			String status = new JSONObject(result).getString(SabnzbdConstants.STATUS);
			if(Boolean.parseBoolean(status)){
				for(int position : mSelectedPositions) {
					mJobs.get(position).put(SabnzbdConstants.STATUS, SabnzbdConstants.PAUSED);
				}
				Toast.makeText(getActivity(), R.string.pause_successful, Toast.LENGTH_SHORT).show();
				mAdapter.notifyDataSetChanged();
				mSelectedPositions.clear();
			}
			else {
				Toast.makeText(getActivity(), R.string.generic_error, Toast.LENGTH_SHORT).show();
			}
		} 
		catch (JSONException e) {
			e.printStackTrace();
		}	
	}

	@Override
	public void onQueueResumeFinished(String result) {
		try {
			String status = new JSONObject(result).getString(SabnzbdConstants.STATUS);
			if(Boolean.parseBoolean(status)){
				mListener.onQueueResumeFinished();
				mSelectedPositions.clear();
			}
			else {
				Toast.makeText(getActivity(), R.string.generic_error, Toast.LENGTH_SHORT).show();
			}
		} 
		catch (JSONException e) {
			e.printStackTrace();
		}	
	}
	
	/*
	 * A helper function to update the list of old jobs after a delete
	 * operation has completed.
	 */
	public void updateRemovedJobs() {
		ArrayList<JSONObject> removeList = new ArrayList<JSONObject>();
		if(mSelectedPositions.size() > 0) { // Only delete SELECTED jobs
			for(int position : mSelectedPositions) {
				removeList.add(mJobs.get(position));
			}		
			mJobs.removeAll(removeList);
		}
		else { // Delete ALL jobs
			mJobs.clear();
		}
		mAdapter.notifyDataSetChanged();		
	}		
}
