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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.SabnzbdConstants;
import com.gmail.at.faint545.tasks.QueueActionTask.QueueActionTaskListener;

public class SpeedLimitActivity extends Activity implements QueueActionTaskListener {
	private EditText valueEditText;
	private Button ok;
	private Remote target;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		target = getIntent().getParcelableExtra("remote");
		setContentView(R.layout.speed_limit);
		onCreateView();
		super.onCreate(savedInstanceState);
	}

	private void onCreateView() {
		setTitle("Set Speed Limit");
		valueEditText = (EditText) findViewById(R.id.speed_limit_value);
		ok = (Button) findViewById(R.id.speed_limit_ok);
		ok.setEnabled(false);
		
		initListeners();
	}
	
	private void initListeners() {
		valueEditText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if(s.length() < 1)
					ok.setEnabled(false);
				else
					ok.setEnabled(true);
			}
		});
		
		ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				String value = valueEditText.getText().toString();
				new SpeedLimitTask(SpeedLimitActivity.this, target.buildURL(), target.getApiKey()).execute(value);
			}
		});
	}

	@Override
	public void onQueueDeleteFinished(String result) {}

	@Override
	public void onQueuePauseFinished(String result) {}

	@Override
	public void onQueueResumeFinished(String result) {}

	@Override
	public void onSpeedLimitFinished(String result) {

	}
	
	private static class SpeedLimitTask extends AsyncTask<String, Void, String> {
		private ProgressDialog progressDialog;
		private String url;
		private String api;
		private WeakReference<Activity> mWeakContext;
	
		public SpeedLimitTask(Activity context, String url,String api) {
			this.url = url;
			this.api = api;
			mWeakContext = new WeakReference<Activity>(context);
		}
		
		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(mWeakContext.get(), null, "Setting speed limit");
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpRequest = new HttpPost(url);
			ArrayList<NameValuePair> arguments = new ArrayList<NameValuePair>();
			arguments.add(new BasicNameValuePair(SabnzbdConstants.APIKEY, api));
			arguments.add(new BasicNameValuePair(SabnzbdConstants.OUTPUT, SabnzbdConstants.OUTPUT_JSON));		
			prepareForSpeedLimit(arguments,params);
			
			try {
				httpRequest.setEntity(new UrlEncodedFormEntity(arguments));
				HttpResponse result = httpClient.execute(httpRequest);
				InputStream inStream = result.getEntity().getContent();
				BufferedReader br = new BufferedReader(new InputStreamReader(inStream),8);
				StringBuilder jsonStringBuilder = new StringBuilder();
				String line;
				while((line = br.readLine()) != null) {
					if(line.length() > 0)
						jsonStringBuilder.append(line);
				}
				br.close();
				inStream.close();
				return jsonStringBuilder.toString();
			} 
			catch (ClientProtocolException e) {
				return ClientProtocolException.class.getName();
			} 
			catch (IOException e) {
				return IOException.class.getName();
			}
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				String results = new JSONObject(result).getString(SabnzbdConstants.STATUS);
				if(Boolean.parseBoolean(results)) {
					Toast.makeText(mWeakContext.get(), "Speed limit set!", Toast.LENGTH_SHORT).show();
					mWeakContext.get().finish();
				}
				else {
					Toast.makeText(mWeakContext.get(), R.string.generic_error, Toast.LENGTH_SHORT).show();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}			
			cleanup();
			super.onPostExecute(result);
		}
		
		private void cleanup() {		
			progressDialog.dismiss();
			progressDialog = null;
			url = null;
			api = null;
		}	
		
		private void prepareForSpeedLimit(ArrayList<NameValuePair> arguments,String... params) {
			arguments.add(new BasicNameValuePair(SabnzbdConstants.MODE, SabnzbdConstants.MODE_CONFIG));
			arguments.add(new BasicNameValuePair(SabnzbdConstants.NAME, SabnzbdConstants.SPEEDLIMIT));
			arguments.add(new BasicNameValuePair(SabnzbdConstants.VALUE, params[0]));		
		}		
	}	
}
