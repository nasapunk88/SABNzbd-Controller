package com.gmail.at.faint545.adapters;

import java.util.ArrayList;

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.R.drawable;
import com.gmail.at.faint545.R.id;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RemoteFragmentAdapter extends ArrayAdapter<Remote> {
	private ArrayList<Remote> remotes;
	private Context mContext;
	private int resourceId;

	public RemoteFragmentAdapter(Context context, int resourceId, ArrayList<Remote> objects) {
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
		}
		
		if(position%2 == 0) {
			convertView.setBackgroundResource(R.drawable.remote_listview_selector_odd);
		}
		else {
			convertView.setBackgroundResource(R.drawable.remote_listview_selector_even);
		}
		
		TextView name = (TextView) convertView.findViewById(R.id.remote_layout_row_name);
		TextView host = (TextView) convertView.findViewById(R.id.remote_layout_row_host);
				
		
		if(remotes.size() > 0) {
			Remote remote = remotes.get(position);
			name.setText(remote.getName());
			
			host.setText(remote.getHost());
		}
		return convertView;
	}
}
