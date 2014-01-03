package com.color.colornamer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;

// this class controls sharing & file writing & cleanup.
public class Sharer {
	
	private File dir;
	private Context context;
	private MediaScannerConnection scanner;

	//need the activity in order to start a sharing intent
	public Sharer(Context context) {
		this.context = context;
    	String path = Environment.getExternalStorageDirectory().toString() + "/colornamer";
		dir = new File(path);
		scanner = new MediaScannerConnection(context, null);
		scanner.connect();
	}
	
	public void Share(Bitmap bitmap, String name, String hex) {
		//save to external storage with the help of http://stackoverflow.com/questions/649154/android-bitmap-save-to-location
    	FileOutputStream out;
    	//it seems that Google+ only accepts media from content providers (content:// uris), not file:// uris, so this won't work for g+
    	File imageFile = new File(dir, "colornamer-"+name.replace(" ", "_")+".png");
		try {
			dir.mkdirs();
			out = new FileOutputStream(imageFile);
	        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
	        out.flush();
	        out.close();
	        //This puts the image into the gallary so sharing with some apps (like g+) works better
	        scanner.scanFile(imageFile.toString(), null);
		} catch (FileNotFoundException e) {
			//Log.d("file not found", "png file not found");
			e.printStackTrace();
		} catch (IOException e) {
			//Log.d("error writing to file", "io exception");
			e.printStackTrace();
		}
    	Intent sendIntent = new Intent();
    	sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
    	sendIntent.putExtra(Intent.EXTRA_TEXT, "I found this color named \"" + name + "\" using Color Namer.  The hex value is " + hex + "." +
    			"\n\nColor Namer is available at play.google.com/store/apps/details?id=com.color.colornamer");
    	sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Look at this cool color from Color Namer!");
    	ArrayList<Uri> extrasList = new ArrayList<Uri>();
    	extrasList.add(Uri.fromFile(imageFile));
    	sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, extrasList);
    	sendIntent.setType("image/png");
    	context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.share_title)));
	}
	
	public void onDestroy() {
    	File[] files = dir.listFiles();
    	if (files != null) {
	    	for (int i = 0; i < files.length; i++) {
	    		files[i].delete();
	    	}
    	}
    	dir.delete();
    	//Tells the system to refresh the available files - this removes the colornamer folder from the gallery
    	
    	//http://stackoverflow.com/questions/18624235/android-refreshing-the-gallery-after-saving-new-images
    	
    	MediaScannerConnection.scanFile(context,
                new String[] { ""+Environment.getExternalStorageDirectory()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    	
    	
    	//context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
    	//context.unbindService(scanner);

	}
	
	// creates a bitmap of the current color
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
