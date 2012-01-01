package com.gmail.at.faint545.fragments;

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
import android.widget.Toast;

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.databases.RemoteDatabase;

public class NewRemoteFragment extends Fragment {
	private EditText nickEditText;
	private EditText addressEditText;
	private EditText portEditText;
	private EditText apiKeyEditText;
	private Button saveRemote;
	private NewRemoteListener remoteListener;

	public interface NewRemoteListener {
		public void onRemoteSaved();
	}

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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_remote, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setupViews();
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

	private void initListeners() {
		nickEditText.addTextChangedListener(new TextWatcher() {			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}			
			@Override
			public void afterTextChanged(Editable s) {
				if(s.length() > 0 && addressEditText.getText().length() > 0 && portEditText.getText().length() > 0 && apiKeyEditText.getText().length() > 0) {
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
				if(s.length() > 0 && nickEditText.getText().length() > 0 && portEditText.getText().length() > 0 && apiKeyEditText.getText().length() > 0) {
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
				if(s.length() > 0 && nickEditText.getText().length() > 0 && addressEditText.getText().length() > 0 && apiKeyEditText.getText().length() > 0) {
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
				if(s.length() > 0 && nickEditText.getText().length() > 0 && addressEditText.getText().length() > 0 && portEditText.getText().length() > 0) {
					saveRemote.setEnabled(true);
				}
				else {
					saveRemote.setEnabled(false);
				}
			}
		});

		saveRemote.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				saveToDatabase(nickEditText.getText().toString(), addressEditText.getText().toString(), portEditText.getText().toString(), apiKeyEditText.getText().toString());
			}
		});
	}

	private void saveToDatabase(String nickname, String address, String port, String apiKey) {
		new AsyncTask<String, Void, Long>(){
			RemoteDatabase database;
			@Override
			protected void onPreExecute() {
				database = new RemoteDatabase(getActivity());
				super.onPreExecute();
			}

			@Override
			protected Long doInBackground(String... params) {
				String nickname = params[0], address = params[1],
						port = params[2], apiKey = params[3];
				database.open();
				long result = (getRemote() != null) ? database.update(Integer.parseInt(getRemote().getId()), nickname, address, port, apiKey) : database.insert(nickname, address, port, apiKey);
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

		}.execute(nickname,address,port,apiKey);
	}

	private void setupViews() {
		nickEditText = (EditText) getView().findViewById(R.id.new_remote_layout_name_edit_text);
		addressEditText = (EditText) getView().findViewById(R.id.new_remote_layout_address_edit_text);
		portEditText = (EditText) getView().findViewById(R.id.new_remote_layout_port_edit_text);
		apiKeyEditText = (EditText) getView().findViewById(R.id.new_remote_layout_apikey_edit_text);
		saveRemote = (Button) getView().findViewById(R.id.new_remote_save_button);

		saveRemote.setEnabled(false); // Disable button by default
	}

	public void populateApiKey(String apiKey) {
		apiKeyEditText.setText(apiKey);
	}

	private Remote getRemote() {
		return getArguments().getParcelable("remote");
	}
}
