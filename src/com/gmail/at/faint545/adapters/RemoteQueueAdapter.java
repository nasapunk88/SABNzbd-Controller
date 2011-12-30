package com.gmail.at.faint545.adapters;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.SabnzbdConstants;

public class RemoteQueueAdapter extends ArrayAdapter<JSONObject> {
	private Context mContext;
	private int resourceID;
	private ArrayList<JSONObject> jobs;

	public RemoteQueueAdapter(Context context, int resourceID, ArrayList<JSONObject> objects) {
		super(context, resourceID, objects);
		mContext = context;
		this.resourceID = resourceID;
		jobs = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			convertView = ((LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE)).inflate(resourceID, null);
		}
		
		JSONObject job = jobs.get(position);
		Log.d("TAG",job.toString());
		StringBuilder jobProgress = new StringBuilder();	
		
		TextView filename = (TextView) convertView.findViewById(R.id.remote_queue_row_filename);
		TextView progress = (TextView) convertView.findViewById(R.id.remote_queue_row_progress);
		TextView status = (TextView) convertView.findViewById(R.id.remote_queue_row_status);
		View status_indicator = convertView.findViewById(R.id.remote_queue_status_indicator);
		
		try {
			String statusText = job.getString(SabnzbdConstants.STATUS);
			jobProgress.append(job.getString(SabnzbdConstants.MBLEFT)).append(" / ").append(job.getString(SabnzbdConstants.MB));
			filename.setText(job.getString(SabnzbdConstants.FILENAME));
			progress.setText(jobProgress.toString());
			status.setText(statusText);
			
			if(statusText.equalsIgnoreCase("downloading")) {
				status_indicator.setBackgroundResource(R.color.lime_green);
			}
			else if(statusText.equalsIgnoreCase("paused")) {
				status_indicator.setBackgroundResource(R.color.light_red);
			}
			else if(statusText.equalsIgnoreCase("queued")) {
				status_indicator.setBackgroundResource(R.color.banana_yellow);
			}
			
		} 
		catch (JSONException e) {
			e.printStackTrace();
		}
		return convertView;
	}
}
