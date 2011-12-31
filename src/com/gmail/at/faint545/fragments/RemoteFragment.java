package com.gmail.at.faint545.fragments;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
import android.widget.Toast;

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.activities.RemoteDetailsActivity;
import com.gmail.at.faint545.adapters.RemoteFragmentAdapter;
import com.gmail.at.faint545.databases.RemoteDatabase;

public class RemoteFragment extends ListFragment {
	private static RemoteFragmentAdapter mAdapter;
	private static ListView mListView;
	private static View mEmptyRemoteView;
	private static ArrayList<Remote> remotes = new ArrayList<Remote>(); // A list of remotes
	public 	static final int EDIT_REMOTE = 0x88, DELETE_REMOTE = EDIT_REMOTE >> 2, LOAD_REMOTE = DELETE_REMOTE >> 2,
							 SET_SPEED_LIMIT = LOAD_REMOTE >> 2;
	
	private RemoteFragmentListener mListener;	

	/*
	 * Callback functions for this class
	 */
	public interface RemoteFragmentListener {
		public void onEditRemote(Remote targetRemote);
		public void onAddRemoteClicked();
	}
	
	public static RemoteFragment newInstance() {
		RemoteFragment self = new RemoteFragment();
		return self;
	}
	
	@Override
	public void onAttach(SupportActivity activity) {
		mListener = (RemoteFragmentListener) activity;
		super.onAttach(activity);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		loadRemotes();
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.remote, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {		
		initViews();		
		registerForContextMenu(mListView); // Register this ListView to show a context menu		
		setupListAdapter();
		super.onActivityCreated(savedInstanceState);
	}
	
	private void initViews() {
		mListView = getListView();		
		mEmptyRemoteView = getView().findViewById(R.id.remote_empty);
		Button addRemote = (Button) getView().findViewById(R.id.remote_add_remote);
		
		mListView.setCacheColorHint(getActivity().getResources().getColor(R.color.main_background)); // Optimization for ListView
		
		addRemote.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mListener.onAddRemoteClicked();
			}
		});
	}	
	
	/*
     * Obtain all SABNzbd remote profiles from the local data store. Do this as a background task
     * to avoid ANR dialogs.
     */
	public void loadRemotes() {
		new DatabaseTask(getActivity()).execute(LOAD_REMOTE);
	}	
	
	/*
	 * A helper function to create the list adapter and set it
	 */
	private void setupListAdapter() {
		mAdapter = new RemoteFragmentAdapter(getActivity(), R.layout.remote_row, remotes);
		setListAdapter(mAdapter);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
		menu.add(Menu.NONE, EDIT_REMOTE, Menu.NONE, "Edit");
		menu.add(Menu.NONE, DELETE_REMOTE, Menu.NONE, "Delete");
		menu.add(Menu.NONE,SET_SPEED_LIMIT,Menu.NONE,"Limit Speed");
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId()) {
			case EDIT_REMOTE:
				mListener.onEditRemote(remotes.get(menuInfo.position));
			break;
			case DELETE_REMOTE:
				new DatabaseTask(getActivity()).execute(DELETE_REMOTE,menuInfo.position);
			break;
			case SET_SPEED_LIMIT:
				Remote thisRemote = remotes.get(menuInfo.position);
				DialogFragment setSpeedLimit = SpeedLimitFragment.newInstance(thisRemote);
				setSpeedLimit.show(getSupportFragmentManager(), "setSpeedLimitDialog");				
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent detailsIntent = new Intent(getActivity(), RemoteDetailsActivity.class);
		detailsIntent.putExtra("selected_remote", remotes.get(position));
		startActivity(detailsIntent);
		super.onListItemClick(l, v, position, id);
	}
	
	/*
	 * A background task for deleting/loading remotes
	 */
	static class DatabaseTask extends AsyncTask<Integer, Void, Long> {
		private WeakReference<Activity> mWeakContext;
		private int request,position;
				
		public DatabaseTask(Activity context) {
			mWeakContext = new WeakReference<Activity>(context);
		}

		@Override
		protected Long doInBackground(Integer... params) {
			request = params[0];
			RemoteDatabase db = new RemoteDatabase(mWeakContext.get());
			db.open();
			long result = 0;
			switch(request) {
				case DELETE_REMOTE:
					position = params[1];
					result = db.delete(Integer.parseInt(remotes.get(position).getId()));
					db.close();
				break;
				case LOAD_REMOTE:
					Cursor cur = db.getAllRows();					
					if(remotes.size() < cur.getCount()) // If a new remote has been added, move to that remote in the database
						cur.moveToPosition(remotes.size()-1);
					else // A remote has been updated
						remotes.clear();
					
					while(cur.moveToNext()) { // Collect all columns from each row and process it
						String id = cur.getString(RemoteDatabase.ID_INDEX);
						String name = cur.getString(RemoteDatabase.NAME_INDEX);
						String address = cur.getString(RemoteDatabase.ADDR_INDEX);
						String port = cur.getString(RemoteDatabase.PORT_INDEX);
						String apiKey = cur.getString(RemoteDatabase.API_KEY_INDEX);
						remotes.add(new Remote(name).setAddress(address).setPort(port).setApiKey(apiKey).setId(id));
					}					
					cur.close();
					db.close();
				break;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Long result) {
			switch(request) {
				case DELETE_REMOTE:
					if(result > 0) {
						remotes.remove(position);
						Toast.makeText(mWeakContext.get(), "Delete successful!", Toast.LENGTH_SHORT).show();
					}
				break;
			}		
			if(mAdapter != null) mAdapter.notifyDataSetChanged();
			
			if(remotes.size() < 1) {
				mListView.setVisibility(View.GONE);
				mEmptyRemoteView.setVisibility(View.VISIBLE);				
			}
			super.onPostExecute(result);
		}
	}
}