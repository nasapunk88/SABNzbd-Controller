package com.gmail.at.faint545.fragments;

import java.util.ArrayList;

import android.graphics.Color;
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

import com.gmail.at.faint545.ControllerListAdapter;
import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.databases.RemoteDatabase;

public class RemoteFragment extends ListFragment {
	private ControllerListAdapter mAdapter;
	private RemoteFragmentListener mListener;
	
	private Button addRemote;
	
	public static final int EDIT_REMOTE = 0x88, DELETE_REMOTE = EDIT_REMOTE >> 2;

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
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true); // Retain instance across activity recreation
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		return inflater.inflate(R.layout.remote_layout, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		initViews();
		registerForContextMenu(getListView()); // Register this ListView to show a context menu		
		getListView().setCacheColorHint(Color.TRANSPARENT); // Optimization for ListView
		setupListAdapter();
		super.onActivityCreated(savedInstanceState);
	}

	private void setupListAdapter() {
		mAdapter = new ControllerListAdapter(getActivity(), R.layout.remote_layout_row, getRemotes());
		setListAdapter(mAdapter);
	}
	
	private void initViews() {
		addRemote = (Button) getView().findViewById(R.id.remote_layout_add_remote);
		
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
		if(result instanceof ArrayList<?>) {
			return (ArrayList<Remote>) result;
		}
		else return null;
	}
	
	public void notifyDataSetChanged() {
		mAdapter.notifyDataSetChanged();
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
}
