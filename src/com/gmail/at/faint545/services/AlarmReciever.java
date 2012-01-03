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
