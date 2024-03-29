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

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.gmail.at.faint545.SabnzbdConstants;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class QueueDownloadTask extends AsyncTask<Void, Void, String> {
	private Fragment mContext;
	private String url;
	private String auth_api;
	private Object mTargetView;
	
	public interface QueueDownloadTaskListener {
		public void onQueueDownloadFinished(String result);
	}
	
	public QueueDownloadTask(Fragment context,String url,String api,Object target) {
		mContext = context;
		this.url = url;		
		auth_api = api;
		mTargetView = target;
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
			return ClientProtocolException.class.getName();
		} 
		catch (IOException e) {
			return ClientProtocolException.class.getName();
		}
	}

	@Override
	protected void onPostExecute(String result) {		
		QueueDownloadTaskListener listener = (QueueDownloadTaskListener) mContext;
		cleanup();
		listener.onQueueDownloadFinished(result);
		super.onPostExecute(result);
	}
	
	private void cleanup() {
		if(mTargetView instanceof PullToRefreshListView) {			
			((PullToRefreshListView) mTargetView).onRefreshComplete();
		}
		else if(mTargetView instanceof ProgressDialog) {
			((ProgressDialog) mTargetView).dismiss();
		}
				
		mTargetView = null;
		mContext = null;
		url = null;
		auth_api = null;
	}
}