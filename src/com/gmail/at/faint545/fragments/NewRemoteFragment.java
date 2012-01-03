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
package com.gmail.at.faint545.fragments;

import android.app.AlarmManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.SupportActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.databases.RemoteDatabase;
import com.gmail.at.faint545.zxing.IntentIntegrator;

public class NewRemoteFragment extends Fragment {
	private EditText nickEditText, addressEditText;
	private EditText portEditText, apiKeyEditText;
	private Button saveRemote, qrButton;
	private NewRemoteListener remoteListener;
	private RadioGroup refreshInterval;
	
	private static final int REFRESH_OFF = R.id.new_remote_refresh_off;
	private static final int REFRESH_FIFTEEN = R.id.new_remote_refresh_fifteen;
	private static final int REFRESH_HOUR = R.id.new_remote_refresh_hour;
	private static final int REFRESH_HALF_HOUR = R.id.new_remote_refresh_half_hour;

	public interface NewRemoteListener {
		public void onRemoteSaved();
	}

	/* Default constructor */
	public static NewRemoteFragment newInstance(Remote remote) {
		Bundle args = new Bundle();
		args.putParcelable("remote", remote);
		NewRemoteFragment self = new NewRemoteFragment();
		self.setArguments(args);
		return self;
	}

	@Override
	public void onAttach(SupportActivity activity) {
		remoteListener = (NewRemoteListener) activity;
		super.onAttach(activity);
	}

	/* Inflate custom view and initialize view objects */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.new_remote, null);
		nickEditText = (EditText) view.findViewById(R.id.new_remote_layout_name_edit_text);
		addressEditText = (EditText) view.findViewById(R.id.new_remote_layout_address_edit_text);
		portEditText = (EditText) view.findViewById(R.id.new_remote_layout_port_edit_text);
		apiKeyEditText = (EditText) view.findViewById(R.id.new_remote_layout_apikey_edit_text);
		saveRemote = (Button) view.findViewById(R.id.new_remote_save_button);
		refreshInterval = (RadioGroup) view.findViewById(R.id.new_remote_refresh_radiogroup);
		qrButton = (Button) view.findViewById(R.id.new_remote_layout_qr_button);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if(getRemote() != null) populateViews();
		initListeners();
		super.onActivityCreated(savedInstanceState);
	}

	private void populateViews() {
		nickEditText.setText(getRemote().getName());
		addressEditText.setText(getRemote().getAddress());
		portEditText.setText(getRemote().getPort());
		apiKeyEditText.setText(getRemote().getApiKey());
	}

	/* Initialize some listeners for view objects */
	private void initListeners() {
		nickEditText.addTextChangedListener(new TextWatcher() {			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}			
			@Override
			public void afterTextChanged(Editable s) {
				if(validateTextFields()) {
					saveRemote.setEnabled(true);
				}
				else {
					saveRemote.setEnabled(false);
				}
			}
		});

		addressEditText.addTextChangedListener(new TextWatcher() {			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}			
			@Override
			public void afterTextChanged(Editable s) {
				if(validateTextFields()) {
					saveRemote.setEnabled(true);
				}
				else {
					saveRemote.setEnabled(false);
				}
			}
		});

		portEditText.addTextChangedListener(new TextWatcher() {			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}

			@Override
			public void afterTextChanged(Editable s) {
				if(validateTextFields()) {
					saveRemote.setEnabled(true);
				}
				else {
					saveRemote.setEnabled(false);
				}
			}
		});

		apiKeyEditText.addTextChangedListener(new TextWatcher() {			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}

			@Override
			public void afterTextChanged(Editable s) {
				if(validateTextFields()) {
					saveRemote.setEnabled(true);
				}
				else {
					saveRemote.setEnabled(false);
				}
			}
		});
		
		refreshInterval.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(validateTextFields()) {
					saveRemote.setEnabled(true);
				}
				else {
					saveRemote.setEnabled(false);
				}
			}
		});
		
		qrButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				IntentIntegrator integrator = new IntentIntegrator(getActivity());
				integrator.initiateScan();
			}
		});

		saveRemote.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				long refresh = 0;
				switch(refreshInterval.getCheckedRadioButtonId()) {
					case REFRESH_FIFTEEN:
						refresh = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
					break;
					case REFRESH_HALF_HOUR:
						refresh = AlarmManager.INTERVAL_HALF_HOUR;
					break;
					case REFRESH_HOUR:
						refresh = AlarmManager.INTERVAL_HOUR;
					break;
					case REFRESH_OFF:
						refresh = -1;
					break;
					default: 
						refresh = -1;
					break;
				}
				saveToDatabase(nickEditText.getText().toString(), addressEditText.getText().toString(), portEditText.getText().toString(), apiKeyEditText.getText().toString(),refresh);
			}
		});
	}
	
	/* Return true if all text fields are filled, false otherwise */
	private boolean validateTextFields() {
		if(nickEditText.length() > 0 && addressEditText.length() > 0 && portEditText.length() > 0 && apiKeyEditText.length() > 0) {
			return true;
		}
		else return false;
	}

	/* Save this profile to the local database. Run as an AsyncTask */
	private void saveToDatabase(String nickname, String address, String port, String apiKey, long refreshInterval) {
		new AsyncTask<String, Void, Long>(){
			RemoteDatabase database;
			@Override
			protected void onPreExecute() {
				database = new RemoteDatabase(getActivity());
				super.onPreExecute();
			}

			@Override
			protected Long doInBackground(String... params) {
				String nickname = params[0], address = params[1],port = params[2], apiKey = params[3], refreshInterval = params[4];
				database.open();
				long result = (getRemote() != null) ? database.update(Integer.parseInt(getRemote().getId()), nickname, address, port, apiKey,refreshInterval) 
																						: database.insert(nickname,address,port,apiKey,refreshInterval);
				database.close();
				return result;
			}

			@Override
			protected void onPostExecute(Long result) {
				if(result != -1) {
					Toast.makeText(getActivity(), "Save successful!", Toast.LENGTH_SHORT).show();
					remoteListener.onRemoteSaved();
				}
				else {
					Toast.makeText(getActivity(), "Save failed!", Toast.LENGTH_SHORT).show();
				}
				super.onPostExecute(result);
			}

		}.execute(nickname,address,port,apiKey,String.valueOf(refreshInterval));
	}

	/* 
	 * This method is used to populate the API key field 
	 * when a QR scan completes 
	 */
	public void populateApiKey(String apiKey) {
		apiKeyEditText.setText(apiKey);
	}

	private Remote getRemote() {
		return getArguments().getParcelable("remote");
	}
}
