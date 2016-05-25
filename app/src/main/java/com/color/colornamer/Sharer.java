package com.color.colornamer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

/**
 * Class to control sharing & file writing & cleanup.
 */
public class Sharer {
	
	private File dir;
	private Context context;
	private MediaScannerConnection scanner;

	// Need the activity in order to start a sharing intent
	public Sharer(Context context) {
		this.context = context;
    	String path = Environment.getExternalStorageDirectory().toString() + "/colornamer";
		dir = new File(path);
		scanner = new MediaScannerConnection(context, null);
		scanner.connect();
	}
	
	public void Share(Bitmap bitmap, String name, String hex) {
		// Save to external storage with the help of
		// http://stackoverflow.com/questions/649154/android-bitmap-save-to-location
    	FileOutputStream out;
    	// It seems that Google+ only accepts media from content providers (content:// uris),
        // not file:// uris, so this won't work for g+
    	File imageFile = new File(dir, "colornamer-"+name.replace(" ", "_")+".png");
		try {
			dir.mkdirs();
			out = new FileOutputStream(imageFile);
	        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
	        out.flush();
	        out.close();
	        // This puts the image into the gallery so sharing with some apps (like g+) works better
	        scanner.scanFile(imageFile.toString(), null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	Intent sendIntent = new Intent();
        Resources res = context.getResources();
    	sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
    	sendIntent.putExtra(Intent.EXTRA_TEXT, String.format(res.getString(R.string.share_body),
                name, hex));
    	sendIntent.putExtra(Intent.EXTRA_SUBJECT, res.getString(R.string.share_subject));
    	ArrayList<Uri> extrasList = new ArrayList<Uri>();
    	extrasList.add(Uri.fromFile(imageFile));
    	sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, extrasList);
    	sendIntent.setType("image/png");
    	context.startActivity(Intent.createChooser(sendIntent,
                res.getString(R.string.share_title)));
	}
	
	public void onDestroy() {
    	File[] files = dir.listFiles();
    	if (files != null) {
	    	for (int i = 0; i < files.length; i++) {
	    		files[i].delete();
	    	}
    	}
    	dir.delete();
    	scanner.disconnect();

    	// Tells the system to refresh the available files - this removes the colornamer
        // folder from the gallery (Thanks CodeCheater@github)
    	MediaScannerConnection.scanFile(context,
                new String[] { ""+Environment.getExternalStorageDirectory()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });

	}
	
	// Creates a bitmap of the current color.
    public Bitmap createBitmap(String colorString) {
    	int size = 512;
		int[] color = new int[size*size];
		int colorValue = Color.parseColor(colorString);
		for (int i = 0; i < size*size; i++) {
			color[i] = colorValue;
		}
		return Bitmap.createBitmap(color, size, size, Bitmap.Config.ARGB_8888);
    }
}
