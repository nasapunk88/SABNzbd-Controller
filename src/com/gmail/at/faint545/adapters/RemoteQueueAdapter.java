package com.gmail.at.faint545.adapters;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
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
		JSONObject job = jobs.get(position);
		if(convertView == null) {
			convertView = ((LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE)).inflate(resourceID, null);
		}
		
		if(position%2 == 0) {
			convertView.setBackgroundResource(R.drawable.remote_listview_selector_odd);
		}
		else {
			convertView.setBackgroundResource(R.drawable.remote_listview_selector_even);
		}		
		
		TextView filename = (TextView) convertView.findViewById(R.id.remote_queue_row_filename);
		TextView sizeLeft = (TextView) convertView.findViewById(R.id.remote_queue_row_sizeleft);
		TextView totalSize = (TextView) convertView.findViewById(R.id.remote_queue_row_totalsize);
		
		try {
			filename.setText(job.getString(SabnzbdConstants.FILENAME));
			sizeLeft.setText(job.getString(SabnzbdConstants.MBLEFT));
			totalSize.setText(job.getString(SabnzbdConstants.MB));
		} 
		catch (JSONException e) {
			e.printStackTrace();
		}
		return convertView;
	}
}
