package com.gmail.at.faint545.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.databases.RemoteDatabase;
import com.gmail.at.faint545.zxing.IntentIntegrator;
import com.gmail.at.faint545.zxing.IntentResult;

public class UpdateControllerActivity extends Activity {
	private Remote currentRemote;
	private EditText nickEditText, addressEditText;
	private EditText portEditText, apiKeyEditText;
	private Button saveRemote;
	private RadioGroup refreshRadioGroup;
	
	private static final int REFRESH_OFF = R.id.new_remote_refresh_off;
	private static final int REFRESH_FIFTEEN = R.id.new_remote_refresh_fifteen;
	private static final int REFRESH_HOUR = R.id.new_remote_refresh_hour;
	private static final int REFRESH_HALF_HOUR = R.id.new_remote_refresh_half_hour;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.new_controller);
		onCreateView();
		currentRemote = getIntent().getParcelableExtra("remote");
		if(currentRemote != null) populateViews();
		super.onCreate(savedInstanceState);
	}

	private void onCreateView() {
		nickEditText = (EditText) findViewById(R.id.new_remote_layout_name_edit_text);
		addressEditText = (EditText) findViewById(R.id.new_remote_layout_address_edit_text);
		portEditText = (EditText) findViewById(R.id.new_remote_layout_port_edit_text);
		apiKeyEditText = (EditText) findViewById(R.id.new_remote_layout_apikey_edit_text);
		saveRemote = (Button) findViewById(R.id.new_remote_save_button);
		refreshRadioGroup = (RadioGroup) findViewById(R.id.new_remote_refresh_radiogroup);

		refreshRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
			}
		});

		saveRemote.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				long refreshInterval = -1;
				switch(refreshRadioGroup.getCheckedRadioButtonId()) {
				case REFRESH_FIFTEEN:
					refreshInterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
					break;
				case REFRESH_HALF_HOUR:
					refreshInterval = AlarmManager.INTERVAL_HALF_HOUR;
					break;
				case REFRESH_HOUR:
					refreshInterval = AlarmManager.INTERVAL_HOUR;
					break;
				case REFRESH_OFF:
					refreshInterval = -1;
					break;
				default: 
					refreshInterval = -1;
					break;
				}
				if(validateTextFields()) {
					saveToDatabase(nickEditText.getText().toString(), 
												 addressEditText.getText().toString(), 
												 portEditText.getText().toString(), 
												 apiKeyEditText.getText().toString(),refreshInterval);
				}
			}
		});
	}

	private void populateViews() {
		nickEditText.setText(currentRemote.getName());
		addressEditText.setText(currentRemote.getAddress());
		portEditText.setText(currentRemote.getPort());
		apiKeyEditText.setText(currentRemote.getApiKey());
		
		if(currentRemote.getRefreshInterval() == AlarmManager.INTERVAL_FIFTEEN_MINUTES) {
			refreshRadioGroup.check(REFRESH_FIFTEEN);
		}
		else if(currentRemote.getRefreshInterval() == AlarmManager.INTERVAL_HALF_HOUR) {
			refreshRadioGroup.check(REFRESH_HALF_HOUR);
		}
		else if(currentRemote.getRefreshInterval() == AlarmManager.INTERVAL_HOUR) {
			refreshRadioGroup.check(REFRESH_HOUR);
		}
		else 
			refreshRadioGroup.check(REFRESH_OFF);
	}
	
	/* Return true if all text fields are filled, false otherwise */
	private boolean validateTextFields() {
		int readyCount = 0;
		if(nickEditText.length() > 0) {
			readyCount++;
		}
		else {
			nickEditText.setError(getString(R.string.must_contain_char));			
		}

		if(portEditText.length() > 0) {
			readyCount++;
		}
		else {
			portEditText.setError(getString(R.string.must_contain_dig));
		}

		if(addressEditText.length() > 0 && !addressEditText.toString().contains(" ")) {
			readyCount++;
		}
		else {
			addressEditText.setError(getString(R.string.must_contain_char_no_space));
		}

		if(apiKeyEditText.length() > 0 && !apiKeyEditText.toString().contains(" ")) {
			readyCount++;
		}
		else {
			apiKeyEditText.setError(getString(R.string.must_contain_char_no_space));
		}

		return (readyCount == 4) ? true : false;
	}
	
	/* Save this profile to the local database. Run as an AsyncTask */
	private void saveToDatabase(String nickname, String address, String port, String apiKey, long refreshInterval) {
		new AsyncTask<String, Void, Long>(){
			RemoteDatabase database;
			@Override
			protected void onPreExecute() {
				database = new RemoteDatabase(UpdateControllerActivity.this);
				super.onPreExecute();
			}

			@Override
			protected Long doInBackground(String... params) {
				String nickname = params[0], address = params[1],port = params[2], apiKey = params[3], refreshInterval = params[4];
				database.open();
				long result = (currentRemote != null) ? database.update(Integer.parseInt(currentRemote.getId()), nickname, address, port, apiKey,refreshInterval) 
																							: database.insert(nickname,address,port,apiKey,refreshInterval);
				database.close();
				return result;
			}

			@Override
			protected void onPostExecute(Long result) {
				if(result != -1) {
					Toast.makeText(UpdateControllerActivity.this, "Save successful!", Toast.LENGTH_SHORT).show();
					setResult(Activity.RESULT_OK);
					finish();
				}
				else {
					Toast.makeText(UpdateControllerActivity.this, "Save failed!", Toast.LENGTH_SHORT).show();
				}
				super.onPostExecute(result);
			}

		}.execute(nickname,address,port,apiKey,String.valueOf(refreshInterval));
	}
	
	public void launchQRScanner(View v) {
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan();		
	}
	
	/* We are handling the results from the QR scan here */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if(result != null && result.getContents() != null) {
			apiKeyEditText.setText(result.getContents());
		}
		super.onActivityResult(requestCode, resultCode, data);
	}		
}
