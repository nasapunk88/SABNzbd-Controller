package com.gmail.at.faint545.fragments;

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
	private EditText nickEditText, addressEditText, portEditText, apiKeyEditText;
	private Button saveRemote;
	private NewRemoteListener remoteListener;
	private Remote mRemote;
	
	public interface NewRemoteListener {
		public void onRemoteSaved();
	}
	
	public NewRemoteFragment(Remote remote) {
		mRemote = remote;
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
		setRetainInstance(true);
		setupViews();
		if(mRemote != null) populateViews();
		initListeners();
		super.onActivityCreated(savedInstanceState);
	}

	private void populateViews() {
		nickEditText.setText(mRemote.getName());
		addressEditText.setText(mRemote.getAddress());
		portEditText.setText(mRemote.getPort());
		apiKeyEditText.setText(mRemote.getApiKey());	
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
		RemoteDatabase database = new RemoteDatabase(getActivity());
		database.open();
		long result;
		if(mRemote != null) {
			result = database.update(Integer.parseInt(mRemote.getId()), nickname, address, port, apiKey);
		}
		else {
			result = database.insert(nickname, address, port, apiKey);
		}
		if(result != -1) {
			database.close();
			Toast.makeText(getActivity(), "Save successful!", Toast.LENGTH_SHORT).show();
			remoteListener.onRemoteSaved();
		}
		else {
			database.close();
			Toast.makeText(getActivity(), "Save failed!", Toast.LENGTH_SHORT).show();
		}
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
}
