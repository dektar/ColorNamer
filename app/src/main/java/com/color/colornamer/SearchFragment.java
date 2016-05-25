package com.color.colornamer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchFragment extends Fragment {
	
	private EditText editText;
	private View view;
	ListView listView;
	List<String> resultStringList;
	List<String> resultHexList;
	
	public SearchFragment() {
		//empty constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		view = inflater.inflate(R.layout.activity_search, container, false);
		
		listView = (ListView) view.findViewById(R.id.search_results_view);
		// What to display if there are no results
		listView.setEmptyView(view.findViewById(R.id.empty_result_set));		
		// When an item is clicked, the main activity will take care of it
		listView.setOnItemClickListener((OnItemClickListener) getActivity());
		
		Button button = (Button) view.findViewById(R.id.search_color_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                searchColors(editText.getText().toString(), true);
				// Minimize the keyboard with help from
                // http://stackoverflow.com/questions/2434532/android-set-hidden-the-keybord-on-press-enter-in-a-edittext
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
		        imm.hideSoftInputFromWindow(editText.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
			}
		});
		
		editText = (EditText) view.findViewById(R.id.edit_search);
		editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {	
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					searchColors(editText.getText().toString(), true);
					// Minimize the keyboard with help from
                    // http://stackoverflow.com/questions/2434532/android-set-hidden-the-keybord-on-press-enter-in-a-edittext
					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
			        imm.hideSoftInputFromWindow(editText.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
					return true;
				}
                return false;
			}
		});
		editText.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				searchColors(editText.getText().toString(), false);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
			
		});

		editText.requestFocus();
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);
		
		return view;
	}
	
	// Called when the search button is clicked
	public void searchColors(String query, boolean isDone) {

		query = query.toLowerCase();
		
		// Create a List<String> for the adapter
		resultStringList = new ArrayList<String>();
		resultHexList = new ArrayList<String>();
		for(Map.Entry<String, String> entry : MainActivity.cdata.nameMap.entrySet()) {
			if (entry.getValue().contains(query)) {
				// then this entry contains the search query
				resultStringList.add(entry.getValue());
				resultHexList.add(entry.getKey());
			}
		}
		ColorArrayAdapter adapter = new ColorArrayAdapter(getActivity(), resultStringList,
                resultHexList);
		listView.setAdapter(adapter);
		if (isDone) Toast.makeText(getActivity().getApplicationContext(), adapter.getCount() +
                " colors found", Toast.LENGTH_LONG).show();
	}

}
