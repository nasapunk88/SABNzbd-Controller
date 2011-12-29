package com.gmail.at.faint545.fragments;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.SabnzbdConstants;
import com.gmail.at.faint545.adapters.RemoteHistoryAdapter;

public class RemoteHistoryFragment extends ListFragment {
	private RemoteHistoryAdapter mAdapter;
	private ArrayList<JSONObject> mOldJobs = new ArrayList<JSONObject>();
	private ArrayList<Integer> mSelectedPositions = new ArrayList<Integer>();
	
	public final static int DELETE_ALL = 0x123, DELETE_SELECTED = DELETE_ALL >> 1;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return inflater.inflate(R.layout.remote_history, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		getListView().setCacheColorHint(Color.TRANSPARENT); // Optimization for ListView
		setupListAdapter();
		super.onActivityCreated(savedInstanceState);
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
		}
		else {
			mSelectedPositions.remove((Object) position);
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
}
