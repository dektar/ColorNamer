package com.color.colornamer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

public class ColorViewDialog extends DialogFragment {
	
	private String color;
	private String title;
	private CameraActivity context;
	
	public interface ColorViewDialogListener {
		public void onColorDialogPositiveClick(DialogFragment dialog, String color);
	}
	
	ColorViewDialogListener listener;
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the EnterColorListener so we can send events to the host
            listener = (ColorViewDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement EnterColorListener");
        }
    }
	
	public ColorViewDialog() {
	}
	
	public void setColor(String color, String title) {
		this.color = color;
		this.title = title;
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.color_alert_content, null);
        view.findViewById(R.id.color_alert_layout).setBackgroundColor(Color.parseColor(color));
        builder.setView(view);
        builder.setPositiveButton(R.string.wallpaper_color, new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int id) {
        			listener.onColorDialogPositiveClick(ColorViewDialog.this, color);
        			dialog.dismiss();
        		}
        	});
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
