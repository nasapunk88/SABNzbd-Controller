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
package com.gmail.at.faint545.activities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.adapters.ControllerAdapter;
import com.gmail.at.faint545.databases.RemoteDatabase;

public class ControllerActivity extends ListActivity {
	private static ControllerAdapter mAdapter;
	private static ListView mListView;
	private static ArrayList<Remote> mRemotes = new ArrayList<Remote>();	

	public 	static final int NEW_REMOTE = 0x88;
	public 	static final int EDIT_REMOTE = NEW_REMOTE >> 2;
	public 	static final int DELETE_REMOTE = EDIT_REMOTE >> 2;
	public 	static final int LOAD_REMOTE = DELETE_REMOTE >> 2;
	public 	static final int SET_SPEED_LIMIT = LOAD_REMOTE >> 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {    	
		super.onCreate(savedInstanceState);
		loadProfiles();
		BugSenseHandler.setup(this, "42fc347a"); // Used for BugSense
		setContentView(R.layout.main);
		setupListView();
		setupListAdapter();
	}

	/*
	 * Obtain all SABNzbd remote profiles from the local data store. Do this as a background task
	 * to avoid ANR dialogs.
	 */
	public void loadProfiles() {
		new DatabaseTask(this).execute(LOAD_REMOTE);
	}		

	private void setupListView() {
		mListView = getListView();		
		mListView.setCacheColorHint(getResources().getColor(R.color.main_background)); // Optimization for ListView
		registerForContextMenu(mListView); // Register this ListView to show a context menu
	}

	/* A helper function to initialize the list adapter and set it */
	private void setupListAdapter() {
		mAdapter = new ControllerAdapter(this, R.layout.remote_row, mRemotes);
		setListAdapter(mAdapter);
	}	

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
		menu.add(Menu.NONE, EDIT_REMOTE, Menu.NONE, R.string.edit);
		menu.add(Menu.NONE, DELETE_REMOTE, Menu.NONE, R.string.delete);
		menu.add(Menu.NONE,SET_SPEED_LIMIT,Menu.NONE,"Set Speed Limit");
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		Remote selectedRemote = mRemotes.get(menuInfo.position);
		switch(item.getItemId()) {
			case EDIT_REMOTE:
				Intent editIntent = new Intent(this,UpdateControllerActivity.class);
				editIntent.putExtra("remote", mRemotes.get(menuInfo.position));
				startActivityForResult(editIntent, EDIT_REMOTE);
			break;
			case DELETE_REMOTE:
				new DatabaseTask(this).execute(DELETE_REMOTE,menuInfo.position);
			break;
			case SET_SPEED_LIMIT:
				Intent speedLimitIntent = new Intent(this,SpeedLimitActivity.class);
				speedLimitIntent.putExtra("remote", selectedRemote);
				startActivity(speedLimitIntent);
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent detailsIntent = new Intent(this, DetailsActivity.class);
		detailsIntent.putExtra("selected_remote", mRemotes.get(position));
		startActivity(detailsIntent);
		super.onListItemClick(l, v, position, id);
	}
	
	public void launchUpdateController(View v) {
		startActivityForResult(new Intent(this,UpdateControllerActivity.class), NEW_REMOTE);
	}

	/*
	 * A background task for deleting/loading remotes. This accepts integers
	 * as parameters.
	 */
	private static class DatabaseTask extends AsyncTask<Integer, Void, Long> {
		private WeakReference<Activity> mWeakContext;
		private int request;
		private int position;

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
					result = db.delete(Integer.parseInt(mRemotes.get(position).getId()));
					db.close();
				break;
				case LOAD_REMOTE:
					Cursor cur = db.getAllRows();
	
					/* Move to the last profile position in the database if
					 * a new profile has been added.
					 */
					if(mRemotes.size() < cur.getCount())
						cur.moveToPosition(mRemotes.size()-1);
					else
						mRemotes.clear();
	
					while(cur.moveToNext()) { // Collect all columns from each row and process it
						String id = cur.getString(RemoteDatabase.ID_INDEX);
						String name = cur.getString(RemoteDatabase.NAME_INDEX);
						String address = cur.getString(RemoteDatabase.ADDR_INDEX);
						String port = cur.getString(RemoteDatabase.PORT_INDEX);
						String apiKey = cur.getString(RemoteDatabase.API_KEY_INDEX);
						long interval = cur.getLong(RemoteDatabase.REFRESH_INDEX);
						mRemotes.add(new Remote(name).setAddress(address).setPort(port).setApiKey(apiKey).setId(id).setRefreshInterval(interval));
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
					mRemotes.remove(position);
					Toast.makeText(mWeakContext.get(), "Delete successful!", Toast.LENGTH_SHORT).show();
				}
				break;
			}

			if(mAdapter != null) mAdapter.notifyDataSetChanged();
			super.onPostExecute(result);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode != Activity.RESULT_CANCELED) new DatabaseTask(this).execute(LOAD_REMOTE);
		super.onActivityResult(requestCode, resultCode, data);
	}
}