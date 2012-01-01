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
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.name = (TextView) convertView.findViewById(R.id.remote_layout_row_name);
			viewHolder.host = (TextView) convertView.findViewById(R.id.remote_layout_row_host);
			convertView.setTag(viewHolder);
		}
		
		Remote remote = remotes.get(position);
		if(remote != null) {
			ViewHolder viewHolder = (ViewHolder) convertView.getTag();
			viewHolder.name.setText(remote.getName());
			viewHolder.host.setText(remote.getHost());
		}
		return convertView;
	}
	
	private static class ViewHolder {
		private TextView name;
		private TextView host;
	}
}
