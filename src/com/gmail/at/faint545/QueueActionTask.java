/*
 * This is an AsynTask which will control and handle actions for the queue such as pause, delete, and resume. This MUST
 * be called from RemoteQueueFragment and ONLY RemoteQueueFragment.
 */

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

public class QueueActionTask extends AsyncTask<String, Void, String> {
	private ProgressDialog progressDialog;
	private String url,api;
	private int request;
	private Fragment fragment;
	
	public final static int DELETE = 0x11;
	public final static int PAUSE = DELETE >> 1;
	public final static int RESUME = PAUSE >> 1;
	public final static int SPEEDLIMIT = RESUME >> 1;
	
	public interface QueueActionTaskListener {
		public void onQueueDeleteFinished(String result);
		public void onQueuePauseFinished(String result);
		public void onQueueResumeFinished(String result);
		public void onSpeedLimitFinished(String result);
	}
	
	public QueueActionTask(Fragment fragment,String url,String api,int request) {
		this.fragment = fragment;
		this.url = url;
		this.request = request;
		this.api = api;
	}
	
	@Override
	protected void onPreExecute() {
		switch(request) {
			case DELETE:
				progressDialog = ProgressDialog.show(fragment.getActivity(), null, "Deleting download(s)");
			break;
			case PAUSE:
				progressDialog = ProgressDialog.show(fragment.getActivity(), null, "Pausing download(s)");
			break;
			case RESUME:
				progressDialog = ProgressDialog.show(fragment.getActivity(), null, "Resuming download(s)");
			break;
			case SPEEDLIMIT:
				progressDialog = ProgressDialog.show(fragment.getActivity(), null, "Setting speed limit");
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
			case PAUSE:
				prepareForPause(arguments,params);
			break;
			case RESUME:
				prepareForResume(arguments,params);
			break;
			case SPEEDLIMIT:
				prepareForSpeedLimit(arguments,params);
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
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}		
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		QueueActionTaskListener listener = (QueueActionTaskListener) fragment;
		switch(request) {
			case DELETE:
				listener.onQueueDeleteFinished(result);
			break;
			case PAUSE:
				listener.onQueuePauseFinished(result);
			break;
			case RESUME:
				listener.onQueueResumeFinished(result);
			break;
			case SPEEDLIMIT:
				listener.onSpeedLimitFinished(result);
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
	
	private void prepareForDelete(ArrayList<NameValuePair> arguments,String... params) {
		String value = (params[0] == null) ? SabnzbdConstants.ALL : params[0];
		
		arguments.add(new BasicNameValuePair(SabnzbdConstants.MODE, SabnzbdConstants.MODE_QUEUE));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.NAME, SabnzbdConstants.DELETE));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.VALUE, value));	
	}
	
	private void prepareForResume(ArrayList<NameValuePair> arguments,String... params) {	
		if(params[0] == null) { // A global resume
			arguments.add(new BasicNameValuePair(SabnzbdConstants.MODE, SabnzbdConstants.MODE_RESUME));
		}
		else { // Individual resume
			arguments.add(new BasicNameValuePair(SabnzbdConstants.MODE, SabnzbdConstants.MODE_QUEUE));
			arguments.add(new BasicNameValuePair(SabnzbdConstants.NAME, SabnzbdConstants.MODE_RESUME));
			arguments.add(new BasicNameValuePair(SabnzbdConstants.VALUE, params[0]));			
		}		
	}

	private void prepareForPause(ArrayList<NameValuePair> arguments,String... params) {		
		if(params[0] == null) { // A global pause
			arguments.add(new BasicNameValuePair(SabnzbdConstants.MODE, SabnzbdConstants.MODE_PAUSE));
		}
		else { // Individual pause
			arguments.add(new BasicNameValuePair(SabnzbdConstants.MODE, SabnzbdConstants.MODE_QUEUE));
			arguments.add(new BasicNameValuePair(SabnzbdConstants.NAME, SabnzbdConstants.MODE_PAUSE));
			arguments.add(new BasicNameValuePair(SabnzbdConstants.VALUE, params[0]));			
		}		
	}
	
	private void prepareForSpeedLimit(ArrayList<NameValuePair> arguments,String... params) {
		arguments.add(new BasicNameValuePair(SabnzbdConstants.MODE, SabnzbdConstants.MODE_CONFIG));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.NAME, SabnzbdConstants.SPEEDLIMIT));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.VALUE, params[0]));		
	}	
}
