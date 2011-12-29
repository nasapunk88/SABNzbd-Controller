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

import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class QueueDownloadTask extends AsyncTask<Void, Void, String> {
	private ProgressDialog progressDialog;
	private Context mContext;
	private String url,auth_api;
	private PullToRefreshListView mPtrView;
	
	public interface DataDownloadTaskListener {
		public void onQueueDownloadFinished(String result);
	}
	
	public QueueDownloadTask(Context context,String url,String api,PullToRefreshListView ptrView) {
		mContext = context;
		this.url = url;		
		auth_api = api;
		mPtrView = ptrView;
	}
	
	@Override
	protected void onPreExecute() {
		if(mPtrView == null) progressDialog = ProgressDialog.show(mContext, null, "Loading data");
		super.onPreExecute();
	}

	@Override
	protected String doInBackground(Void... params) {
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(url);
		
		ArrayList<NameValuePair> arguments = new ArrayList<NameValuePair>();
		arguments.add(new BasicNameValuePair(SabnzbdConstants.APIKEY, auth_api));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.OUTPUT, SabnzbdConstants.OUTPUT_JSON));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.MODE, SabnzbdConstants.MODE_QUEUE));
		
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
		DataDownloadTaskListener listener = (DataDownloadTaskListener) mContext;
		listener.onQueueDownloadFinished(result);
		cleanup();
		super.onPostExecute(result);
	}

	private void cleanup() {
		if(mPtrView != null) mPtrView.onRefreshComplete();
		if(progressDialog != null) progressDialog.dismiss();
		
		mPtrView = null;
		progressDialog = null;		
		mContext = null;
		url = null;
		auth_api = null;
	}
}
