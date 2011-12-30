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
import android.widget.CheckBox;
import android.widget.TextView;

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.SabnzbdConstants;

public class RemoteHistoryAdapter extends ArrayAdapter<JSONObject> {
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
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.name = (TextView) convertView.findViewById(R.id.remote_history_row_filename);
			viewHolder.status = (TextView) convertView.findViewById(R.id.remote_history_row_status);
			viewHolder.isChecked = (CheckBox) convertView.findViewById(R.id.remote_history_checkbox);
			convertView.setTag(viewHolder);
		}
				
		JSONObject oldJob = oldJobs.get(position);
		if(oldJob != null) {
			ViewHolder viewHolder = (ViewHolder) convertView.getTag();
			try {
				String jobStatus = oldJob.getString(SabnzbdConstants.STATUS);
				String jobName = oldJob.getString(SabnzbdConstants.NAME);
				
				viewHolder.name.setText(jobName);
				viewHolder.status.setText(jobStatus);
				viewHolder.isChecked.setChecked(false);
			} 
			catch (JSONException e) {
				e.printStackTrace();
			}		
		}
		return convertView;
	}
	
	private static class ViewHolder {
		private TextView name,status;
		private CheckBox isChecked;
	}
}
