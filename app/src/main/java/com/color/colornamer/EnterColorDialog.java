package com.color.colornamer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class EnterColorDialog extends DialogFragment implements OnEditorActionListener {
	
	View view;
	
	public interface EnterColorListener {
        void onDialogPositiveClick(DialogFragment dialog, String text);
        void onDialogNegativeClick(DialogFragment dialog, String text);
	}
	
	EnterColorListener listener;
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the EnterColorListener so we can send events to the host
            listener = (EnterColorListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement EnterColorListener");
        }
    }
	
	public EnterColorDialog() {
		// empty constructor
	}

	public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.hex_alert_content, null);
        EditText edit = (EditText) view.findViewById(R.id.edit_message);
        edit.setOnEditorActionListener(this);
        builder.setView(view);
        builder.setPositiveButton(R.string.enter_hex, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   String text = ((EditText) view.findViewById(R.id.edit_message))
                               .getText().toString();
                	   listener.onDialogPositiveClick(EnterColorDialog.this, text);
                   }
               })
               .setNegativeButton(R.string.cancel_hex, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   // Minimize the keyboard too
                       dialog.cancel();
                   }
               }).setTitle(R.string.title_hex);
        // Create the AlertDialog object and return it
        return builder.create();
    }

	// For the editText - trying to get the enter button to submit the dialog
	@Override
	public boolean onEditorAction(TextView view, int action, KeyEvent event) {
		if (EditorInfo.IME_ACTION_DONE == action) {
            listener.onDialogPositiveClick(null, view.getText().toString());
            this.dismiss();
            return true;
        }
		return false;
	}
}
