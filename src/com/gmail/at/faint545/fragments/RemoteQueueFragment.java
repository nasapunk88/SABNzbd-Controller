package com.gmail.at.faint545.fragments;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.SabnzbdConstants;
import com.gmail.at.faint545.adapters.RemoteQueueAdapter;

public class RemoteQueueFragment extends ListFragment {
	private RemoteQueueAdapter mAdapter;
	private ArrayList<JSONObject> jobs = new ArrayList<JSONObject>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return inflater.inflate(R.layout.remote_queue, null);
	}
		
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		getListView().setCacheColorHint(Color.TRANSPARENT); // Optimization for ListView
		setupListAdapter();
		super.onActivityCreated(savedInstanceState);
	}

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
