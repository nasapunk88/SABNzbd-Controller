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
	private String url,api,request;
	private Fragment fragment;
	
	public interface QueueActionTaskListener {
		public void onQueueDeleteFinished(String result);
		public void onQueuePauseFinished(String result);
		public void onQueueResumeFinished(String result);
		public void onSpeedLimitFinished(String result);
	}
	
	public QueueActionTask(Fragment fragment,String url,String api,String request) {
		this.fragment = fragment;
		this.url = url;
		this.request = request;
		this.api = api;
	}
	
	@Override
	protected void onPreExecute() {
		if(request.equalsIgnoreCase(SabnzbdConstants.DELETE)) {
			progressDialog = ProgressDialog.show(fragment.getActivity(), null, "Deleting download(s)");
		}
		else if(request.equalsIgnoreCase(SabnzbdConstants.MODE_PAUSE)) {
			progressDialog = ProgressDialog.show(fragment.getActivity(), null, "Pausing download(s)");
		}
		else if(request.equalsIgnoreCase(SabnzbdConstants.MODE_RESUME)) {
			progressDialog = ProgressDialog.show(fragment.getActivity(), null, "Resuming download(s)");
		}
		else if(request.equalsIgnoreCase(SabnzbdConstants.SPEEDLIMIT)) {
			progressDialog = ProgressDialog.show(fragment.getActivity(), null, "Setting speed limit");
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
		else if(request.equalsIgnoreCase(SabnzbdConstants.MODE_PAUSE)) {
			arguments = prepareForPause(params);
		}
		else if(request.equalsIgnoreCase(SabnzbdConstants.MODE_RESUME)) {
			arguments = prepareForResume(params);
		}
		else if(request.equalsIgnoreCase(SabnzbdConstants.SPEEDLIMIT)) {
			arguments = prepareForSpeedLimit(params);
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
		if(request.equalsIgnoreCase(SabnzbdConstants.DELETE))
			listener.onQueueDeleteFinished(result);
		else if(request.equalsIgnoreCase(SabnzbdConstants.MODE_PAUSE))
			listener.onQueuePauseFinished(result);
		else if(request.equalsIgnoreCase(SabnzbdConstants.MODE_RESUME))
			listener.onQueueResumeFinished(result);
		else if(request.equalsIgnoreCase(SabnzbdConstants.SPEEDLIMIT))
			listener.onSpeedLimitFinished(result);
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
	
	private ArrayList<NameValuePair> prepareForDelete(String... params) {
		String value = (params[0] == null) ? SabnzbdConstants.ALL : params[0];
		
		ArrayList<NameValuePair> arguments = new ArrayList<NameValuePair>();
		arguments.add(new BasicNameValuePair(SabnzbdConstants.APIKEY, api));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.OUTPUT, SabnzbdConstants.OUTPUT_JSON));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.MODE, SabnzbdConstants.MODE_QUEUE));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.NAME, SabnzbdConstants.DELETE));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.VALUE, value));
		return arguments;	
	}
	
	private ArrayList<NameValuePair> prepareForResume(String... params) {
		ArrayList<NameValuePair> arguments = new ArrayList<NameValuePair>();
		arguments.add(new BasicNameValuePair(SabnzbdConstants.APIKEY, api));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.OUTPUT, SabnzbdConstants.OUTPUT_JSON));
		
		if(params[0] == null) {			
			arguments.add(new BasicNameValuePair(SabnzbdConstants.MODE, SabnzbdConstants.MODE_RESUME));
		}
		else {
			arguments.add(new BasicNameValuePair(SabnzbdConstants.MODE, SabnzbdConstants.MODE_QUEUE));
			arguments.add(new BasicNameValuePair(SabnzbdConstants.NAME, SabnzbdConstants.MODE_RESUME));
			arguments.add(new BasicNameValuePair(SabnzbdConstants.VALUE, params[0]));			
		}		
		return arguments;
	}

	private ArrayList<NameValuePair> prepareForPause(String... params) {
		ArrayList<NameValuePair> arguments = new ArrayList<NameValuePair>();
		arguments.add(new BasicNameValuePair(SabnzbdConstants.APIKEY, api));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.OUTPUT, SabnzbdConstants.OUTPUT_JSON));
		
		if(params[0] == null) { // A global pause
			arguments.add(new BasicNameValuePair(SabnzbdConstants.MODE, SabnzbdConstants.MODE_PAUSE));
		}
		else { // Individual pause
			arguments.add(new BasicNameValuePair(SabnzbdConstants.MODE, SabnzbdConstants.MODE_QUEUE));
			arguments.add(new BasicNameValuePair(SabnzbdConstants.NAME, SabnzbdConstants.MODE_PAUSE));
			arguments.add(new BasicNameValuePair(SabnzbdConstants.VALUE, params[0]));			
		}		
		return arguments;
	}
	
	private ArrayList<NameValuePair> prepareForSpeedLimit(String... params) {
		ArrayList<NameValuePair> arguments = new ArrayList<NameValuePair>();
		arguments.add(new BasicNameValuePair(SabnzbdConstants.APIKEY, api));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.OUTPUT, SabnzbdConstants.OUTPUT_JSON));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.MODE, SabnzbdConstants.MODE_CONFIG));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.NAME, SabnzbdConstants.SPEEDLIMIT));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.VALUE, params[0]));		
		return arguments;
	}	
}
