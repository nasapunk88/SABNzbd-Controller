package com.gmail.at.faint545;

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

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

public class HistoryActionTask extends AsyncTask<String, Void, String> {
	private ProgressDialog progressDialog;
	private String url,auth_api,request;
	private Fragment fragment;
	
	public interface HistoryActionTaskListener {
		public void onHistoryDeleteFinished(String result);
		public void onHistoryRetryFinished(String result);
	}
	
	public HistoryActionTask(Fragment fragment,String url,String api,String request) {
		this.fragment = fragment;
		this.url = url;
		this.request = request;
		auth_api = api;
	}
	
	@Override
	protected void onPreExecute() {
		if(request.equalsIgnoreCase(SabnzbdConstants.DELETE)) {
			progressDialog = ProgressDialog.show(fragment.getActivity(), null, "Deleting history");
		}
		else if(request.equalsIgnoreCase(SabnzbdConstants.MODE_RETRY)) {
			progressDialog = ProgressDialog.show(fragment.getActivity(), null, "Attempting to retry");
		}
		super.onPreExecute();
	}

	@Override
	protected String doInBackground(String... params) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpRequest = new HttpPost(url);
		ArrayList<NameValuePair> arguments = null;
		
		if(request.equalsIgnoreCase(SabnzbdConstants.DELETE)) {
			arguments = prepareForDelete(params);
		}
		else if(request.equalsIgnoreCase(SabnzbdConstants.MODE_RETRY)) {
			arguments = prepareForRetry(params);
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
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		HistoryActionTaskListener listener = (HistoryActionTaskListener) fragment;
		if(request.equalsIgnoreCase(SabnzbdConstants.DELETE))
			listener.onHistoryDeleteFinished(result);
		else if(request.equalsIgnoreCase(SabnzbdConstants.MODE_RETRY))
			listener.onHistoryRetryFinished(result);
		cleanup();
		super.onPostExecute(result);
	}

	private void cleanup() {		
		progressDialog.dismiss();
		progressDialog = null;
		fragment = null;
		url = null;
		auth_api = null;
	}
	
	private ArrayList<NameValuePair> prepareForDelete(String... params) {
		String value = (params[0] == null) ? SabnzbdConstants.ALL : params[0];
		
		ArrayList<NameValuePair> arguments = new ArrayList<NameValuePair>();
		arguments.add(new BasicNameValuePair(SabnzbdConstants.APIKEY, auth_api));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.OUTPUT, SabnzbdConstants.OUTPUT_JSON));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.MODE, SabnzbdConstants.MODE_HISTORY));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.NAME, SabnzbdConstants.DELETE));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.VALUE, value));
		return arguments;
	}
	
	private ArrayList<NameValuePair> prepareForRetry(String... params) {
		ArrayList<NameValuePair> arguments = new ArrayList<NameValuePair>();
		arguments.add(new BasicNameValuePair(SabnzbdConstants.APIKEY, auth_api));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.OUTPUT, SabnzbdConstants.OUTPUT_JSON));		
		arguments.add(new BasicNameValuePair(SabnzbdConstants.MODE, SabnzbdConstants.MODE_RETRY));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.VALUE, params[0]));
		return arguments;
	}
}
