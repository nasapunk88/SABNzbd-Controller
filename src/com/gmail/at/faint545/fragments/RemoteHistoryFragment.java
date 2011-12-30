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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.gmail.at.faint545.HistoryActionTask;
import com.gmail.at.faint545.HistoryActionTask.HistoryActionTaskListener;
import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.SabnzbdConstants;
import com.gmail.at.faint545.adapters.RemoteHistoryAdapter;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class RemoteHistoryFragment extends ListFragment implements HistoryActionTaskListener {
	private RemoteHistoryAdapter mAdapter;
	private ArrayList<JSONObject> mOldJobs = new ArrayList<JSONObject>();
	private ArrayList<Integer> mSelectedPositions = new ArrayList<Integer>();
	private RemoteHistoryListener mListener;
	private PullToRefreshListView mPtrView;
	
	public final static int DELETE_ALL = 0x123, DELETE_SELECTED = DELETE_ALL >> 1;
	
	public interface RemoteHistoryListener {
		public void onRefreshHistory(PullToRefreshListView view);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);			
		super.onCreate(savedInstanceState);
	}	
	
	@Override
	public void onAttach(SupportActivity activity) {
		mListener = (RemoteHistoryListener) activity;
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		ViewGroup parent = (ViewGroup) getActivity().findViewById(R.id.remote_details_pager);
		return inflater.inflate(R.layout.remote_history, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mPtrView = (PullToRefreshListView) getView().findViewById(R.id.remote_history_ptr);
		getListView().setCacheColorHint(Color.TRANSPARENT); // Optimization for ListView
		setupListAdapter();
		initListeners();
		super.onActivityCreated(savedInstanceState);
	}

	/*
	 * Default implementation for initializing listeners for views
	 */
	private void initListeners() {
		mPtrView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				mListener.onRefreshHistory(mPtrView);
			}			
		});
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		// Only show these two options if there are jobs to delete
		if(mSelectedPositions.size() > 0 && mOldJobs.size() > 0) {
			menu.add(Menu.NONE,DELETE_SELECTED,Menu.NONE,"Delete selected");
		}
		else if(mOldJobs.size() > 0){
			menu.add(Menu.NONE,DELETE_ALL,Menu.NONE,"Delete all");
		}
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case DELETE_ALL: // Conditional for deleting all history
				deleteHistory(null);
			break;
			case DELETE_SELECTED: // Conditional for deleting selected items
				StringBuilder selectedJobs = collectSelectedItems();
				deleteHistory(selectedJobs.substring(0, selectedJobs.lastIndexOf(","))); // Chop off the last comma
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/*
	 * A helper function to collect all the NZO IDs of all selected items/jobs
	 */
	private StringBuilder collectSelectedItems() {
		StringBuilder selectedJobs = new StringBuilder();
		// Create a string of jobs to delete, separated by commas i.e: job1,job2,job3
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
		return selectedJobs;
	}
	
	/*
	 * A function for when a user selects to delete a specific or all jobs. Refers to: RemoteHistoryFragment.java
	 */
	public void deleteHistory(String selectedJobs) {
		Remote currentRemote = getActivity().getIntent().getParcelableExtra("selected_remote");
		new HistoryActionTask(this, currentRemote.buildURL(), currentRemote.getApiKey(),SabnzbdConstants.DELETE).execute(selectedJobs);
	}	

	/*
	 * A helper function to setup the list adapter
	 */
	private void setupListAdapter() {
		mAdapter = new RemoteHistoryAdapter(getActivity(), R.layout.remote_history_row, mOldJobs);
		setListAdapter(mAdapter);
	}

	/*
	 * A handler to handle the event of a list item click
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		CheckBox checkbox = (CheckBox) v.findViewById(R.id.remote_history_checkbox);
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
	 * A function that handles arguments that have been passed through. 
	 * The is intended to be used immediately after constructing the fragment
	 * but we are re-purposing it for our own needs. In our case, this will 
	 * trigger when new history data has been downloaded and the fragment 
	 * needs to be updated.
	 */
	@Override
	public void setArguments(Bundle args) {
		String data = args.getString("data");
		mOldJobs.clear();
		if(data != null) {
			try {
				JSONObject object = new JSONObject(data);
				object = object.getJSONObject(SabnzbdConstants.MODE_HISTORY);
				JSONArray array = object.getJSONArray(SabnzbdConstants.SLOTS);
				
				for(int x = 0; x < array.length(); x++) {
					mOldJobs.add(array.getJSONObject(x));
				}
			} 
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if(mAdapter != null)
			mAdapter.notifyDataSetChanged();
	}
	
	/*
	 * A callback function for when the delete operation has completed. Refers to: DeleteHistoryTask.java
	 */
	@Override
	public void onHistoryDeleteFinished(String result) {
		try {
			String status = new JSONObject(result).getString(SabnzbdConstants.STATUS);
			if(Boolean.parseBoolean(status)) {
				updateRemovedJobs();
				Toast.makeText(getActivity(), R.string.delete_success, Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(getActivity(), R.string.generic_error, Toast.LENGTH_SHORT).show();
			}
		} 
		catch (JSONException e) {
			Toast.makeText(getActivity(), R.string.generic_error, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onHistoryRetryFinished(String result) {
		// TODO Auto-generated method stub		
	}
	
	/*
	 * A helper function to update the list of old jobs after a delete
	 * operation has completed.
	 */
	public void updateRemovedJobs() {
		ArrayList<JSONObject> removeList = new ArrayList<JSONObject>();
		if(mSelectedPositions.size() > 0) { // Only delete SELECTED jobs
			for(int position : mSelectedPositions) {
				removeList.add(mOldJobs.get(position));
			}		
			mOldJobs.removeAll(removeList);
		}
		else { // Delete ALL jobs
			mOldJobs.clear();
		}
		mAdapter.notifyDataSetChanged();
		mSelectedPositions.clear();
	}		
}
