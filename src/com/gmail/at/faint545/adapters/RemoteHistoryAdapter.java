package com.gmail.at.faint545.adapters;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.SabnzbdConstants;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RemoteHistoryAdapter extends ArrayAdapter<JSONObject> {
	private TextView name, status;
	private View indicator;
	private Context mContext;
	private int resourceID;
	private ArrayList<JSONObject> oldJobs;

	public RemoteHistoryAdapter(Context context, int resourceID, ArrayList<JSONObject> objects) {
		super(context, resourceID, objects);
		mContext = context;
		this.resourceID = resourceID;
		oldJobs = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			convertView = ((LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE)).inflate(resourceID, null);
		}
		
		initViews(convertView);
		
		JSONObject oldJob = oldJobs.get(position);
		String jobStatus = null, jobName = null;
		try {
			jobStatus = oldJob.getString(SabnzbdConstants.STATUS);
			jobName = oldJob.getString(SabnzbdConstants.NAME);
			
			if(jobStatus.equals(SabnzbdConstants.STATUS_COMPLETED)) {
				indicator.setVisibility(View.VISIBLE);
				indicator.setBackgroundResource(R.color.lime_green);
			}
			else if(jobStatus.equals(SabnzbdConstants.STATUS_FAILED)) {
				indicator.setVisibility(View.VISIBLE);
				indicator.setBackgroundResource(R.color.light_red);
			}
		} 
		catch (JSONException e) {
			e.printStackTrace();
		}
		
		if(position%2 == 0) {
			convertView.setBackgroundResource(R.drawable.remote_listview_selector_odd);
		}
		else {
			convertView.setBackgroundResource(R.drawable.remote_listview_selector_even);
		}
		
		name.setText(jobName);
		status.setText(jobStatus);
		return convertView;
	}

	private void initViews(View convertView) {		
		name = (TextView) convertView.findViewById(R.id.remote_history_row_filename);
		status = (TextView) convertView.findViewById(R.id.remote_history_row_status);
		indicator = convertView.findViewById(R.id.remote_history_row_indicator);
	}
}
