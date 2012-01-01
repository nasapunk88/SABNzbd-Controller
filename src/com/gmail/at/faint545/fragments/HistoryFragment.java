package com.gmail.at.faint545.fragments;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
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
import android.widget.Toast;

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.SabnzbdConstants;
import com.gmail.at.faint545.adapters.RemoteHistoryAdapter;
import com.gmail.at.faint545.tasks.HistoryActionTask;
import com.gmail.at.faint545.tasks.HistoryDownloadTask;
import com.gmail.at.faint545.tasks.HistoryActionTask.HistoryActionTaskListener;
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
		public void onConnectionError();
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
	public void onAttach(SupportActivity activity) {
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
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		loadingStub.setVisibility(View.VISIBLE); // Make progressbar visible
		downloadHistory(null); // Begin downloading
		getListView().setCacheColorHint(Color.TRANSPARENT); // Optimization for ListView
		setupListAdapter();
		initListeners();
		super.onActivityCreated(savedInstanceState);
	}
	
	/*
	 * A function to trigger a history download. If a PullToRefreshListView
	 * is passed through, this indicates that we are going to use the
	 * PullToRefreshListView to show a loading message.
	 */
	private void downloadHistory(Object target) {
		mOldJobs.clear();
		new HistoryDownloadTask(this, getRemote().buildURL(), getRemote().getApiKey(),target).execute();
	}

	/* A helper function to setup the list adapter */
	private void setupListAdapter() {
		mAdapter = new RemoteHistoryAdapter(getActivity(), R.layout.remote_history_row, mOldJobs);
		setListAdapter(mAdapter);
	}	
	
	/* Default implementation for initializing listeners for views */
	private void initListeners() {
		mPtrView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				downloadHistory(mPtrView);
			}			
		});
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
		String selectedJobs = null;
		if(mSelectedPositions.size() > 0) {
			selectedJobs = collectSelectedItems();
		}
		switch(item.getItemId()) {
			case DELETE:
				deleteHistory(selectedJobs);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/* A helper function to collect all the NZO IDs of all selected items/jobs */
	private String collectSelectedItems() {
		StringBuilder selectedJobs;
		selectedJobs = new StringBuilder();
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
		return selectedJobs.substring(0, selectedJobs.lastIndexOf(","));
	}
	
	/* A handler to handle the event of a list item click */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		CheckBox checkbox = (CheckBox) v.findViewById(R.id.remote_history_checkbox);
		checkbox.toggle(); // Toggle the check box
		
		/* Add or remove the current position from our list of selected positions */
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
	
	/* A function for when a user selects to delete a specific or all jobs. Refers to: RemoteHistoryFragment.java */
	public void deleteHistory(String selectedJobs) {
		Remote currentRemote = getActivity().getIntent().getParcelableExtra("selected_remote");
		new HistoryActionTask(this, currentRemote.buildURL(), currentRemote.getApiKey(),HistoryActionTask.DELETE).execute(selectedJobs);
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
				downloadHistory(ProgressDialog.show(getActivity(), null, "Loading data"));
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
	public void onHistoryRetryFinished(String result) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void onHistoryDownloadFinished(String result) {
		if(result.equals(ClientProtocolException.class.getName()) || result.equals(IOException.class.getName())) {
			mFragmentListener.onConnectionError();
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
				
				loadingStub.setVisibility(View.GONE); // Hide progressbar				
			}
			catch(JSONException e) {
				e.printStackTrace();
			}
		}		
	}
}