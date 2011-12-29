package com.gmail.at.faint545.fragments;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.SupportActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.SabnzbdConstants;
import com.gmail.at.faint545.adapters.RemoteQueueAdapter;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class RemoteQueueFragment extends ListFragment {
	private RemoteQueueAdapter mAdapter;
	private ArrayList<JSONObject> jobs = new ArrayList<JSONObject>();
	private PullToRefreshListView mPtrView;
	private RemoteQueueListener mListener;
	
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
				mListener.onRefreshQueue(mPtrView);
			}
		});
	}

	/*
	 * A helper function to setup the list adapter
	 */	
	private void setupListAdapter() {
		mAdapter = new RemoteQueueAdapter(getActivity(), R.layout.remote_queue_row, jobs);
		setListAdapter(mAdapter);
	}

	@Override
	public void setArguments(Bundle args) {
		String data = args.getString("data");
		jobs.clear();
		if(data != null) {
			try {
				JSONObject object = new JSONObject(data);
				JSONArray array = object.getJSONArray(SabnzbdConstants.JOBS);
				
				for(int x = 0; x < array.length(); x++) {
					jobs.add(array.getJSONObject(x));
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
}
