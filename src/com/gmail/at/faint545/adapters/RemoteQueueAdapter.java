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
import android.widget.ProgressBar;
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
			viewHolder.status = (TextView) convertView.findViewById(R.id.remote_queue_row_status);
			viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.remote_queue_checkbox);
			viewHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.remote_queue_progress_bar);
			convertView.setTag(viewHolder);
		}
					
		JSONObject job = jobs.get(position);
		if(job != null) {
			ViewHolder viewHolder = (ViewHolder) convertView.getTag();
			StringBuilder jobStatus = new StringBuilder();			
			String statusText = null, filename = null;
			double mbLeft = 0, mbTotal = 0;
			Boolean isChecked = false;
			
			try {
				mbLeft = job.getDouble(SabnzbdConstants.MBLEFT);
				mbTotal = job.getDouble(SabnzbdConstants.MB);
				statusText = job.getString(SabnzbdConstants.STATUS);
				filename = job.getString(SabnzbdConstants.FILENAME);
				isChecked = job.getBoolean("checked");				
			}
			catch (JSONException e) {
				// Do nothing
			}
			
			jobStatus.append(statusText).append(", ").append(StringUtils.normalizeSize(mbLeft, "m")).append(" left of ").append(StringUtils.normalizeSize(mbTotal, "m"));			
			
			viewHolder.checkBox.setChecked(isChecked);
			viewHolder.filename.setText(filename);
			viewHolder.status.setText(jobStatus.toString());
			viewHolder.progressBar.setMax((int)	mbTotal);
			viewHolder.progressBar.setProgress((int)(mbTotal-mbLeft));
		}
		return convertView;
	}
	
	private static class ViewHolder {
		TextView filename;
		TextView status;
		CheckBox checkBox;
		ProgressBar progressBar;
	}
}
