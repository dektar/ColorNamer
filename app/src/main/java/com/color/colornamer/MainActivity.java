package com.color.colornamer;


import java.io.IOException;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;


/*
 * TODO: saving colors to lists
 * TODO: share from colordialog
 * 2.2: set color as wallpaper (including from colordialog), share to google+ and google drive type things
 * 2.0: Camera input & camera sharing
 * 1.2: share colors, bug fix formatting for old devices
 * 1.1: instant search, full sized color, minor bug fixes with the keyboard, formatting for small devices
 */
public class MainActivity extends MenuActivity implements OnTouchListener, OnSeekBarChangeListener,
		EnterColorDialog.EnterColorListener, OnItemClickListener,
		ColorViewDialog.ColorViewDialogListener {
	public final static String EXTRA_MESSAGE = "com.example.colorpicker.MESSAGE";
	public final static String EXTRA_COLOR_ENTERED = "com.example.colorpicker.COLOR_ENTERED";
	public final static String EXTRA_MESSAGE_RESULT = "com.example.colorpicker.MESSAGE_RESULT";
	public final static String EXTRA_COLOR_RESULT = "com.example.colorpicker.COLOR_RESULT";
	public final static String COLOR_FILE = "rgb.txt";
	public ColorPickerView colorPicker;
	private TextView text1,text2;
	private boolean hasColor = false; //whether the text areas have a color in them
	private double spanPrev = 1;
	private static final int blueStart = 100;
	private String currentColor; //picked color hex
	private String currentNamedColor;
	static ColorData cdata;	
  
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        cdata = new ColorData(this);
        setContentView(R.layout.activity_main);
        LinearLayout layout = (LinearLayout) findViewById(R.id.color_picker_layout);
        final int width = layout.getWidth();
        //get the display density
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        colorPicker = new ColorPickerView(this,blueStart,metrics.densityDpi);
        layout.setMinimumHeight(width);
        layout.addView(colorPicker);
        layout.setOnTouchListener(this);
        
        text1 = (TextView) findViewById(R.id.result1_textview);
        text2 = (TextView) findViewById(R.id.result2_textview);
        text2.setGravity(Gravity.CENTER_HORIZONTAL);
        //for different screen sizes
		if (text2.getTag().equals("layout") || text2.getTag().equals("layout-small")) text2.setText(getText(R.string.tap));
		else text2.setText(getText(R.string.tap) + "\n");
        hasColor = false;
        text2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (hasColor) {
					ColorViewDialog dialog = new ColorViewDialog();
					dialog.setColor(currentNamedColor, cdata.getColorName(currentNamedColor) + " (" + currentNamedColor  + ")");
					dialog.show(getSupportFragmentManager(), "color_view");
				}
			}
        });
        text1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (hasColor) {
					ColorViewDialog dialog = new ColorViewDialog();
					dialog.setColor(currentColor, getText(R.string.you_picked) + " " + currentColor);
					dialog.show(getSupportFragmentManager(), "color_view");
				}
			}  	
        });
        
		SeekBar seek = (SeekBar) findViewById(R.id.seekBar1);
		seek.setProgress(blueStart);
		seek.setMax(255);
		seek.setOnSeekBarChangeListener(this);
		
    } 
	
	@Override
	public void onDestroy() {
		//clean up temp image files
		sharer.onDestroy();
		super.onDestroy();
	}
	
    
    // sets the text boxes' text and color background
	private void updateTextAreas(int col) {
		hasColor=true;
		int[] colBits = {Color.red(col),Color.green(col),Color.blue(col)};
		String colString = cdata.ColorToString(colBits);
		currentColor = colString;
		//set the text & color backgrounds
		text1.setText(getText(R.string.you_picked) + " " + colString);
		text1.setBackgroundColor(col);
		String colclose = cdata.closestColor(colString);
		currentNamedColor = colclose;
		// screen size matters
		if (text2.getTag().equals("layout") || text2.getTag().equals("layout-small")) text2.setText(("" + cdata.getColorName(colclose) + "    " + "(" + colclose + ")"));
		else text2.setText("" + cdata.getColorName(colclose) + '\n' + "(" + colclose + ")");
		text2.setBackgroundColor(Color.parseColor(colclose));
		
		if (cdata.isDarkColor(colBits)) {
			text1.setTextColor(Color.WHITE);
			text2.setTextColor(Color.WHITE);
		} else {
			text1.setTextColor(Color.BLACK);
			text2.setTextColor(Color.BLACK);
		}
	}
    
    @Override
	public boolean onTouch(View view, MotionEvent event) {
    	int numTouch = event.getPointerCount();
    	int col = 0;
    	if (numTouch == 1) {
			col = colorPicker.getColor(event.getX(),event.getY(),true);
			colorPicker.invalidate();
    	} else if (numTouch == 2) {
    		double x1 = event.getX(0);
    		double y1 = event.getY(0);
    		double x2 = event.getX(1);
    		double y2 = event.getY(1);
    		double span = Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
    		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {
    			// then the second touch has just joined us
    			spanPrev = span;
    		}
    		//pinching changes the slider
    		col = colorPicker.updateShade(span/spanPrev);
    		SeekBar seek = (SeekBar) findViewById(R.id.seekBar1);
    		seek.setProgress(Color.blue(col));
    		spanPrev = span;
    	} else return true;
		//re-draw the selected colors text
		updateTextAreas(col);
		return true;
	}
	
    @Override
	public void onProgressChanged(SeekBar seek, int progress, boolean fromUser) {
		int amt = seek.getProgress();
		int col = colorPicker.updateShade(amt);
		updateTextAreas(col);
		colorPicker.invalidate();
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		
	}
	
	// generate a random hex color & display it
	public void randomColor(View v) {
    	int z = (int) (Math.random()*255);
    	int x = (int) (Math.random()*255);
    	int y = (int) (Math.random()*255);
    	colorPicker.setColor(x,y,z);
		SeekBar seek = (SeekBar) findViewById(R.id.seekBar1);
		seek.setProgress(z);
	}
	
	// open an alert to enter a hex color
	public void enterHex(View v) {
		EnterColorDialog dialog = new EnterColorDialog();
		dialog.show(getSupportFragmentManager(), "enter_color");
	}
	
    // for type-in hex colors thru the alert
    public void getTypedColor(String colString) {
    	//Log.d("typed color", colString);
		//minimize the keyboard with help from http://stackoverflow.com/questions/2434532/android-set-hidden-the-keybord-on-press-enter-in-a-edittext
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    	if (colString.equals("")) return;
    	//make sure it starts with # even if user didn't enter it
    	if (colString.charAt(0) != '#')  colString = "#" + colString;
    	if (cdata.isValidColor(colString)) {
    		int col = Color.parseColor(colString);
    		colorPicker.setColor(Color.red(col), Color.green(col), Color.blue(col));
    		SeekBar seek = (SeekBar) findViewById(R.id.seekBar1);
    		seek.setProgress(Color.blue(col));
    	} else {
    		text1.setText(getText(R.string.not_a_color));
    		text2.setText(getText(R.string.dont_be_silly));
    		hasColor=false;
    		colorPicker.noColor();
    		colorPicker.invalidate();
    		text1.setBackgroundColor(Color.WHITE);
    		text1.setTextColor(Color.BLACK);
    		text2.setBackgroundColor(Color.WHITE);
    		text2.setTextColor(Color.BLACK);
    	}
    }

    // for the color enter dialog
	@Override
	public void onDialogPositiveClick(DialogFragment dialog, String text) {
		getTypedColor(text);		
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog, String text) {
	}

	@Override
	//Item click from the search list fragment returns to the main activity & displays nicely
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		ColorArrayAdapter adapter = (ColorArrayAdapter) parent.getAdapter();
		String colString = adapter.getColor(position);
		//Log.d("A click happened", "hex color " + colString);
		hideSearchFrag();
    	InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		int col = Color.parseColor(colString);
		colorPicker.setColor(Color.red(col), Color.green(col), Color.blue(col));
		SeekBar seek = (SeekBar) findViewById(R.id.seekBar1);
		seek.setProgress(Color.blue(col));
	}
	
	private void hideSearchFrag() {
		findViewById(R.id.fragLayout).setVisibility(View.GONE);
    	FragmentManager manager = getSupportFragmentManager();
    	FragmentTransaction transaction = manager.beginTransaction().remove(manager.findFragmentById(R.id.fragLayout));
    	//transaction.addToBackStack(null);
    	transaction.commit();
    	this.setMenuVisible();
	}
	
    @Override
    //Trying to move from SearchFragment back to color picker on back pressed
    public void onBackPressed() {
    	if (findViewById(R.id.fragLayout).getVisibility() != View.GONE) {
    		hideSearchFrag();
    	} else {
    		super.onBackPressed();
    	}    	
    	return;
    }
    
    //share the named color.
    @Override
    protected void share() {
    	if (this.hasColor) {
    		Bitmap bitmap = sharer.createBitmap(currentNamedColor);
    		String name = cdata.getColorName(currentNamedColor);
    		sharer.Share(bitmap, name, currentNamedColor);
    	} else {
    		Toast toast = Toast.makeText(this, getText(R.string.cant_share), Toast.LENGTH_SHORT);
    		toast.setGravity(Gravity.CENTER, 0, 0);
    		toast.show();
    	}
    }
    
    //set the wallpaper to be the named color
    @Override
    protected void wallpaper() {
    	if (this.hasColor) {
    		Bitmap bitmap = sharer.createBitmap(currentNamedColor);
    		WallpaperManager wm = WallpaperManager.getInstance(this.getApplicationContext());
    		try {
				wm.setBitmap(bitmap);
	    		Toast toast = Toast.makeText(this, getText(R.string.wallpaper_set), Toast.LENGTH_SHORT);
	    		toast.setGravity(Gravity.CENTER, 0, 0);
	    		toast.show();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	} else {
    		Toast toast = Toast.makeText(this, getText(R.string.cant_set), Toast.LENGTH_SHORT);
    		toast.setGravity(Gravity.CENTER, 0, 0);
    		toast.show();
    	}
    }

	@Override
	public void onColorDialogPositiveClick(DialogFragment dialog, String color) {
		Bitmap bitmap = sharer.createBitmap(color);
		WallpaperManager wm = WallpaperManager.getInstance(this.getApplicationContext());
		try {
			wm.setBitmap(bitmap);
    		Toast toast = Toast.makeText(this, getText(R.string.wallpaper_set), Toast.LENGTH_SHORT);
    		toast.setGravity(Gravity.CENTER, 0, 0);
    		toast.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    
 
}
