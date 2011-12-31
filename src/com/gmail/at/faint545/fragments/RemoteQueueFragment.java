package com.gmail.at.faint545.fragments;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.SupportActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
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
		attachFooterView();
		setupListAdapter();
		initListeners();
		super.onActivityCreated(savedInstanceState);
	}

	private void attachFooterView() {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View footer = inflater.inflate(R.layout.remote_queue_footer, null);
		getListView().addFooterView(footer,null,false);
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
				new QueueActionTask(this, currentRemote.buildURL(), currentRemote.getApiKey(),SabnzbdConstants.MODE_PAUSE).execute(selectedJobs);
			break;
			case PAUSE_SELECTED:
				stringBuilder = collectSelectedItems();			
				selectedJobs = stringBuilder.substring(0, stringBuilder.lastIndexOf(","));
				new QueueActionTask(this, currentRemote.buildURL(), currentRemote.getApiKey(),SabnzbdConstants.MODE_PAUSE).execute(selectedJobs);
			break;
			case RESUME_ALL:
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
				JSONObject object = new JSONObject(data).getJSONObject(SabnzbdConstants.MODE_QUEUE);				
				populateFooterView(object);				
				JSONArray array = object.getJSONArray(SabnzbdConstants.SLOTS);
				
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

	private void populateFooterView(JSONObject object) throws JSONException {
		TextView timeleft = (TextView) getView().findViewById(R.id.remote_queue_timeleft);
		TextView speed = (TextView) getView().findViewById(R.id.remote_queue_speed);
		
		speed.setText(object.getString(SabnzbdConstants.SPEED));
		timeleft.setText(object.getString(SabnzbdConstants.TIMELEFT));
	}

	@Override
	public void onQueueDeleteFinished(String result) {
		try {
			String status = new JSONObject(result).getString(SabnzbdConstants.STATUS);
			if(Boolean.parseBoolean(status)){
				mListener.onRefreshQueue(null);
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
	public void onQueuePauseFinished(String result) {
		try {
			String status = new JSONObject(result).getString(SabnzbdConstants.STATUS);
			if(Boolean.parseBoolean(status)){
				mListener.onRefreshQueue(null);
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
				mListener.onRefreshQueue(null);
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
	public void onSpeedLimitFinished(String result) {
	}		
}
