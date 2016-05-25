package com.color.colornamer;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

// draws a list of color results
// with a little help from http://www.vogella.com/articles/AndroidListView/article.html
public class ColorArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final List<String> values;
	private final List<String> colors;

	public ColorArrayAdapter(Context context, List<String> values, List<String> colors) {
		super(context, R.layout.search_result, values);
		this.context = context;
		this.values = values;
		this.colors = colors;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.search_result, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.search_result_text);
		textView.setText(values.get(position));
		View colorView = rowView.findViewById(R.id.search_result_color);
		colorView.setBackgroundColor(Color.parseColor(colors.get(position)));
		TextView hexView = (TextView) rowView.findViewById(R.id.search_result_hex_text);
		hexView.setText(colors.get(position));
		return rowView;
	}
	
	public String getColor(int position) {
		return colors.get(position);
	}
}
