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
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import com.gmail.at.faint545.SabnzbdConstants;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class HistoryDownloadTask extends AsyncTask<Void, Void, String> {
	private Fragment mContext;
	private String url;
	private String api;
	private PullToRefreshListView pullToRefresh;

	public interface HistoryDownloadTaskListener {
		public void onHistoryDownloadFinished(String result);
	}

	public HistoryDownloadTask(Fragment context,String url,String api,PullToRefreshListView usePTR) {
		mContext = context;
		this.url = url;
		this.api = api;
		pullToRefresh = usePTR;
	}

	@Override
	protected String doInBackground(Void... params) {
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(url);

		ArrayList<NameValuePair> arguments = new ArrayList<NameValuePair>();
		arguments.add(new BasicNameValuePair(SabnzbdConstants.APIKEY, api));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.OUTPUT, SabnzbdConstants.OUTPUT_JSON));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.MODE, SabnzbdConstants.MODE_HISTORY));

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
			return IOException.class.getName();
		}
	}

	@Override
	protected void onPostExecute(String result) {
		HistoryDownloadTaskListener listener = (HistoryDownloadTaskListener) mContext;
		listener.onHistoryDownloadFinished(result);
		cleanup();
		super.onPostExecute(result);
	}

	private void cleanup() {
		if(pullToRefresh instanceof PullToRefreshListView) {
			((PullToRefreshListView) pullToRefresh).onRefreshComplete();
		}

		pullToRefresh = null;		
		mContext = null;
		url = null;
		api = null;
	}
}
