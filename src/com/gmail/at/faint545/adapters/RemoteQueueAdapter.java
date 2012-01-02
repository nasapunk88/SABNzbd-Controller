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
import com.gmail.at.faint545.StringUtils;

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
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.filename = (TextView) convertView.findViewById(R.id.remote_queue_row_filename);
			viewHolder.progress = (TextView) convertView.findViewById(R.id.remote_queue_row_progress);
			viewHolder.status = (TextView) convertView.findViewById(R.id.remote_queue_row_status);
			viewHolder.statusIndicator = convertView.findViewById(R.id.remote_queue_status_indicator);
			viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.remote_queue_checkbox);
			convertView.setTag(viewHolder);
		}
					
		JSONObject job = jobs.get(position);
		if(job != null) {
			ViewHolder viewHolder = (ViewHolder) convertView.getTag();
			StringBuilder jobProgress = new StringBuilder();			
			String statusText = null, mbLeft = null, filename = null;
			Boolean isChecked = false;
			
			try {
				mbLeft = StringUtils.normalizeSize(job.getString(SabnzbdConstants.MBLEFT), "m");
				statusText = job.getString(SabnzbdConstants.STATUS);
				isChecked = job.getBoolean("checked");
				filename = job.getString(SabnzbdConstants.FILENAME);
			}
			catch (JSONException e) {
				// Do nothing.
			}
			
			jobProgress.append(mbLeft).append(" left");
			jobProgress.toString();
			
			viewHolder.checkBox.setChecked(isChecked);
			viewHolder.filename.setText(filename);
			viewHolder.progress.setText(mbLeft);
			viewHolder.status.setText(statusText);
			
			if(statusText.equalsIgnoreCase("downloading")) {
				viewHolder.statusIndicator.setBackgroundResource(R.color.lime_green);
			}
			else if(statusText.equalsIgnoreCase("paused")) {
				viewHolder.statusIndicator.setBackgroundResource(R.color.light_red);
			}
			else if(statusText.equalsIgnoreCase("queued")) {
				viewHolder.statusIndicator.setBackgroundResource(R.color.banana_yellow);
			}
		}
		return convertView;
	}
	
	private static class ViewHolder {
		TextView filename;
		TextView progress;
		TextView status;
		View statusIndicator;
		CheckBox checkBox;
	}
}
