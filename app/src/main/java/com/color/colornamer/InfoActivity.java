package com.color.colornamer;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.widget.TextView;

public class InfoActivity extends MenuActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		TextView t = (TextView) findViewById(R.id.info_text);
		t.setMovementMethod(LinkMovementMethod.getInstance());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_info, menu);
		return true;
	}

}
