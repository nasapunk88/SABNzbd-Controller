package com.gmail.at.faint545.fragments;

import java.util.ArrayList;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.SupportActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.gmail.at.faint545.ControllerListAdapter;
import com.gmail.at.faint545.R;
import com.gmail.at.faint545.Remote;

public class RemoteFragment extends ListFragment {
	private ControllerListAdapter mAdapter;
	private View mListViewFooter;

	public static RemoteFragment newInstance(ArrayList<Remote> remotes) {
		RemoteFragment self = new RemoteFragment();
		
		Bundle args = new Bundle();
		args.putParcelableArrayList("remotes", remotes);
		
		self.setArguments(args);
		return self;
	}	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		return inflater.inflate(R.layout.remote_layout, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setRetainInstance(true);
		setupListView();
		setupListAdapter();
		super.onActivityCreated(savedInstanceState);
	}

	private void setupListView() {
		getListView().setCacheColorHint(Color.TRANSPARENT);		
		mListViewFooter = getActivity().getLayoutInflater().inflate(R.layout.remote_fragment_footer, null);
		getListView().addFooterView(mListViewFooter);
	}

	private void setupListAdapter() {
		mAdapter = new ControllerListAdapter(getActivity(), R.layout.remote_layout_row, getRemotes());
		setListAdapter(mAdapter);
	}

	@SuppressWarnings("unchecked")
	private ArrayList<Remote> getRemotes() {
		Object result = getArguments().get("remotes");
		if(result instanceof ArrayList<?>) {
			return (ArrayList<Remote>) result;
		}
		else return null;
	}
	
	public void notifyDataSetChanged() {
		mAdapter.notifyDataSetChanged();
	}
}
