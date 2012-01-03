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
package com.gmail.at.faint545.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.gmail.at.faint545.SabnzbdConstants;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

public class HistoryActionTask extends AsyncTask<String, Void, String> {
	private ProgressDialog progressDialog;
	private String url;
	private String api;
	private int request;
	private Fragment fragment;
	
	public static final int DELETE = 0x345;
	public static final int RETRY = DELETE >> 1;
	
	public interface HistoryActionTaskListener {
		public void onHistoryDeleteFinished(String result);
		public void onHistoryRetryFinished(String result);
	}
	
	public HistoryActionTask(Fragment fragment,String url,String api,int request) {
		this.fragment = fragment;
		this.url = url;
		this.request = request;
		this.api = api;
	}
	
	@Override
	protected void onPreExecute() {
		switch(request) {
			case DELETE:
				progressDialog = ProgressDialog.show(fragment.getActivity(), null, "Deleting history");
			break;
			case RETRY:
				progressDialog = ProgressDialog.show(fragment.getActivity(), null, "Attempting to retry");
			break;
		}
		super.onPreExecute();
	}

	@Override
	protected String doInBackground(String... params) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpRequest = new HttpPost(url);
		ArrayList<NameValuePair> arguments = new ArrayList<NameValuePair>();
		arguments.add(new BasicNameValuePair(SabnzbdConstants.APIKEY, api));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.OUTPUT, SabnzbdConstants.OUTPUT_JSON));		
		
		switch(request) {
			case DELETE:
				prepareForDelete(arguments,params);
			break;
			case RETRY:
				prepareForRetry(arguments,params);
			break;
		}
		
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
		HistoryActionTaskListener listener = (HistoryActionTaskListener) fragment;
		switch(request) {
			case DELETE:
				listener.onHistoryDeleteFinished(result);
			break;
			case RETRY:
				listener.onHistoryRetryFinished(result);
			break;
		}		
		cleanup();
		super.onPostExecute(result);
	}

	private void cleanup() {
		progressDialog.dismiss();
		progressDialog = null;
		fragment = null;
		url = null;
		api = null;
	}
	
	private void prepareForDelete(ArrayList<NameValuePair> arguments, String... params) {
		String value = (params[0] == null) ? SabnzbdConstants.ALL : params[0];
		
		arguments.add(new BasicNameValuePair(SabnzbdConstants.MODE, SabnzbdConstants.MODE_HISTORY));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.NAME, SabnzbdConstants.DELETE));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.VALUE, value));
	}
	
	private void prepareForRetry(ArrayList<NameValuePair> arguments, String... params) {		
		arguments.add(new BasicNameValuePair(SabnzbdConstants.MODE, SabnzbdConstants.MODE_RETRY));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.VALUE, params[0]));
	}
}
