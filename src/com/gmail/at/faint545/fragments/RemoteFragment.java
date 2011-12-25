package com.gmail.at.faint545.fragments;

import java.util.ArrayList;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gmail.at.faint545.ControllerListAdapter;
import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;

public class RemoteFragment extends ListFragment {
	private ControllerListAdapter mAdapter;
	private ArrayList<Remote> mData = new ArrayList<Remote>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		return inflater.inflate(R.layout.remote_layout, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setupTestData();
		getListView().setCacheColorHint(Color.TRANSPARENT);
		mAdapter = new ControllerListAdapter(getActivity(), R.layout.remote_layout_row, mData);
		setListAdapter(mAdapter);
		super.onActivityCreated(savedInstanceState);
	}

	private void setupTestData() {		
		mData.add(new Remote("Remote1").setAddress("aptaccess.dyndns.org").setPort("8081"));
		mData.add(new Remote("Remote2").setAddress("faint.kicks-ass.net").setPort("8081"));
	}
}
