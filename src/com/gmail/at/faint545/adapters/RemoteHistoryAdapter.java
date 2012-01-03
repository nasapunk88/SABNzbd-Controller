/* 
 * Copyright 2011 Alex Fu
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 		
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
			String jobStatus = null, jobName = null;
			Boolean isChecked = false;
			
			try {
				jobStatus = oldJob.getString(SabnzbdConstants.STATUS);
				jobName = oldJob.getString(SabnzbdConstants.NAME);
				isChecked = oldJob.getBoolean("checked");
			}
			catch (JSONException e) {
				// Do nothing
			}	
			
			viewHolder.name.setText(jobName);
			viewHolder.status.setText(jobStatus);
			viewHolder.isChecked.setChecked(isChecked);
		}
		return convertView;
	}
	
	private static class ViewHolder {
		private TextView name;
		private TextView status;
		private CheckBox isChecked;
	}
}
