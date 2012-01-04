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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;

public class ControllerAdapter extends ArrayAdapter<Remote> {
	private ArrayList<Remote> remotes;
	private Context mContext;
	private int resourceId;

	public ControllerAdapter(Context context, int resourceId, ArrayList<Remote> objects) {
		super(context, resourceId, objects);
		this.resourceId = resourceId;
		mContext = context;
		remotes = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(resourceId, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.name = (TextView) convertView.findViewById(R.id.remote_layout_row_name);
			viewHolder.host = (TextView) convertView.findViewById(R.id.remote_layout_row_host);
			viewHolder.refreshInterval = (TextView) convertView.findViewById(R.id.remote_layout_row_refresh);
			convertView.setTag(viewHolder);
		}
		
		Remote remote = remotes.get(position);
		if(remote != null) {
			ViewHolder viewHolder = (ViewHolder) convertView.getTag();
			viewHolder.name.setText(remote.getName());
			viewHolder.host.setText(remote.getHost());
			
			if(remote.getRefreshInterval() != -1) {
				int intervalInMinutes = (int) ((remote.getRefreshInterval()/1000)/60);
				viewHolder.refreshInterval.setText("Auto-Refresh: " + intervalInMinutes + " minutes");
			}
			else {
				viewHolder.refreshInterval.setText("Auto-Refresh: Off");
			}
		}
		return convertView;
	}
	
	private static class ViewHolder {
		private TextView name;
		private TextView host;
		private TextView refreshInterval;
	}
}
