package com.gmail.at.faint545.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.SupportActivity;
import android.support.v4.view.MenuItem;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.activities.RemoteDetailsActivity;
import com.gmail.at.faint545.adapters.RemoteFragmentAdapter;

public class RemoteFragment extends ListFragment {
	private RemoteFragmentAdapter mAdapter;
	private RemoteFragmentListener mListener;
	
	private Button addRemote;
	
	public static final int EDIT_REMOTE = 0x88, DELETE_REMOTE = EDIT_REMOTE >> 2;

	/*
	 * Callback functions for this class
	 */
	public interface RemoteFragmentListener {
		public void onEditRemote(int position);
		public void onDeleteRemote(int position);
		public void onAddRemoteClicked();
	}
	
	public static RemoteFragment newInstance(ArrayList<Remote> remotes) {
		RemoteFragment self = new RemoteFragment();
		
		Bundle args = new Bundle();
		args.putParcelableArrayList("remotes", remotes);
		
		self.setArguments(args);
		return self;
	}
	
	@Override
	public void onAttach(SupportActivity activity) {
		mListener = (RemoteFragmentListener) activity;
		super.onAttach(activity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		return inflater.inflate(R.layout.remote, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		initViews();
		registerForContextMenu(getListView()); // Register this ListView to show a context menu		
		getListView().setCacheColorHint(getActivity().getResources().getColor(R.color.main_background)); // Optimization for ListView
		setupListAdapter();
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
		menu.add(Menu.NONE, EDIT_REMOTE, Menu.NONE, "Edit");
		menu.add(Menu.NONE, DELETE_REMOTE, Menu.NONE, "Delete");
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId()) {
			case EDIT_REMOTE:
				mListener.onEditRemote(menuInfo.position);
			break;
			case DELETE_REMOTE:
				mListener.onDeleteRemote(menuInfo.position);				
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent detailsIntent = new Intent(getActivity(), RemoteDetailsActivity.class);
		detailsIntent.putExtra("selected_remote", getRemotes().get(position));
		startActivity(detailsIntent);
		super.onListItemClick(l, v, position, id);
	}

	private void setupListAdapter() {
		mAdapter = new RemoteFragmentAdapter(getActivity(), R.layout.remote_row, getRemotes());
		setListAdapter(mAdapter);
	}
	
	private void initViews() {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.remote_footer, null);
		addRemote = (Button) view.findViewById(R.id.remote_add_remote);
		getListView().addFooterView(view);
		
		addRemote.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mListener.onAddRemoteClicked();
			}
		});
	}

	@SuppressWarnings("unchecked")
	private ArrayList<Remote> getRemotes() {
		Object result = getArguments().get("remotes");
		return (ArrayList<Remote>) result;
	}
	
	public void notifyDataSetChanged() {
		mAdapter.notifyDataSetChanged();
	}
}
