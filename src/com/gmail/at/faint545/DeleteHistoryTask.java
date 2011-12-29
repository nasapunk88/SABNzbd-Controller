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

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

public class DeleteHistoryTask extends AsyncTask<String, Void, String> {
	private ProgressDialog progressDialog;
	private Activity mContext;
	private String url,auth_api;
	
	public interface HistoryDeleteTaskListener {
		public void onHistoryDeleteFinished(String result);
	}
	
	public DeleteHistoryTask(Activity context,String url,String api) {
		mContext = context;
		this.url = url;
		auth_api = api;
	}
	
	@Override
	protected void onPreExecute() {
		progressDialog = ProgressDialog.show(mContext, null, "Deleting history");
		super.onPreExecute();
	}

	@Override
	protected String doInBackground(String... params) {
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(url);
		String value = null;
		
		if(params.length < 1) {
			value = SabnzbdConstants.ALL;
		}
		else {
			value = params[0];
		}
		
		ArrayList<NameValuePair> arguments = new ArrayList<NameValuePair>();
		arguments.add(new BasicNameValuePair(SabnzbdConstants.APIKEY, auth_api));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.OUTPUT, SabnzbdConstants.OUTPUT_JSON));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.MODE, SabnzbdConstants.MODE_HISTORY));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.NAME, SabnzbdConstants.DELETE));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.VALUE, value));
		
		try {
			request.setEntity(new UrlEncodedFormEntity(arguments));
			HttpResponse result = client.execute(request);
			InputStream inStream = result.getEntity().getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(inStream),8);
			StringBuilder jsonStringBuilder = new StringBuilder();
			String line;
			while((line = br.readLine()) != null) {
				if(line.length() > 0) {
					jsonStringBuilder.append(line);
				}
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
		HistoryDeleteTaskListener listener = (HistoryDeleteTaskListener) mContext;
		listener.onHistoryDeleteFinished(result);
		cleanup();
		super.onPostExecute(result);
	}

	private void cleanup() {		
		progressDialog.dismiss();
		progressDialog = null;
		mContext = null;
		url = null;
		auth_api = null;
	}
}
