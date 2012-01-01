package com.gmail.at.faint545.fragments;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gmail.at.faint545.tasks.QueueActionTask;
import com.gmail.at.faint545.tasks.QueueActionTask.QueueActionTaskListener;
import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;
import com.gmail.at.faint545.SabnzbdConstants;

public class LimitSpeedDialog extends DialogFragment implements QueueActionTaskListener {
	private EditText valueEditText;
	private Button ok;
	
	public static LimitSpeedDialog newInstance(Remote remote) {
		LimitSpeedDialog self = new LimitSpeedDialog();
		Bundle arguments = new Bundle();
		arguments.putParcelable("remote", remote);
		self.setArguments(arguments);
		return self;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.speed_limit, container,false);
		valueEditText = (EditText) view.findViewById(R.id.speed_limit_value);
		ok = (Button) view.findViewById(R.id.speed_limit_ok);
		ok.setEnabled(false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		getDialog().setTitle("Set Speed Limit");
		initListeners();
		super.onActivityCreated(savedInstanceState);
	}

	private void initListeners() {
		valueEditText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if(s.length() < 1)
					ok.setEnabled(false);
				else
					ok.setEnabled(true);
			}
		});
		
		ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				String value = valueEditText.getText().toString();
				Remote targetRemote = getArguments().getParcelable("remote");
				new QueueActionTask(LimitSpeedDialog.this, targetRemote.buildURL(), targetRemote.getApiKey(), QueueActionTask.SPEEDLIMIT).execute(value);
			}
		});
	}

	@Override
	public void onQueueDeleteFinished(String result) {}

	@Override
	public void onQueuePauseFinished(String result) {}

	@Override
	public void onQueueResumeFinished(String result) {}

	@Override
	public void onSpeedLimitFinished(String result) {
		try {
			String results = new JSONObject(result).getString(SabnzbdConstants.STATUS);
			if(Boolean.parseBoolean(results)) {
				Toast.makeText(getActivity(), "Speed limit set!", Toast.LENGTH_SHORT).show();
				dismiss();
			}
			else {
				Toast.makeText(getActivity(), R.string.generic_error, Toast.LENGTH_SHORT).show();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
