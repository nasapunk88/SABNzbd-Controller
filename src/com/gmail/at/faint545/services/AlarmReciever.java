package com.gmail.at.faint545.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Messenger;

public class AlarmReciever extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String url = intent.getStringExtra("url");
		String api = intent.getStringExtra("api");
		Messenger messenger = (Messenger) intent.getExtras().get("messenger");
		
		Intent i = new Intent(context,DataQueueService.class);
		
		i.putExtra("url", url);
		i.putExtra("api", api);
		i.putExtra("messenger", messenger);
		
		context.startService(i);		
	}
}
