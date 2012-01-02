package com.gmail.at.faint545.services;

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

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.gmail.at.faint545.SabnzbdConstants;

public class DataQueueService extends IntentService {
  
	public DataQueueService() {
		super("Downloader");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String url = intent.getStringExtra("url");
		String api = intent.getStringExtra("api");
		StringBuilder results = new StringBuilder();
		
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(url);
		
		ArrayList<NameValuePair> arguments = new ArrayList<NameValuePair>();
		arguments.add(new BasicNameValuePair(SabnzbdConstants.APIKEY, api));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.OUTPUT, SabnzbdConstants.OUTPUT_JSON));
		arguments.add(new BasicNameValuePair(SabnzbdConstants.MODE, SabnzbdConstants.MODE_QUEUE));
		
		try {
			request.setEntity(new UrlEncodedFormEntity(arguments));
			HttpResponse result = client.execute(request);
			InputStream inStream = result.getEntity().getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(inStream),8);
			String line;
			while((line = br.readLine()) != null) {
				if(line.length() > 0) {
					results.append(line);
				}
			}
			br.close();
			inStream.close();
			
			Bundle extras = intent.getExtras();
			Messenger messenger = (Messenger) extras.get("messenger");
			Message message = Message.obtain();
			
			Bundle resultsBundle = new Bundle();
			resultsBundle.putString("results", results.toString());
			message.setData(resultsBundle);
			
			messenger.send(message);
		} 
		catch (ClientProtocolException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
