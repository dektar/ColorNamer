package com.color.colornamer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

public class ColorViewDialog extends DialogFragment {
	
	private int color;
	private String title;
	private CameraActivity context;

	public ColorViewDialog() {
	}
	
	public void setColor(int color, String title) {
		this.color = color;
		this.title = title;
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.color_alert_content, null);
        view.findViewById(R.id.color_alert_layout).setBackgroundColor(color);
        builder.setView(view);
        builder.setNegativeButton(R.string.cancel_color, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       dialog.cancel();
                   }
               }).setTitle(title);
        // Create the AlertDialog object and return it
        return builder.create();
    }
	
	public void setDestroyCallback(CameraActivity context) {
		this.context = context;
	}
	
	@Override
	public void onDestroy() {
		if (context != null) {
			context.pause();
		}
		super.onDestroy();
	}
}
