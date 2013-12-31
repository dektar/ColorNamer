package com.color.colornamer;

import java.io.IOException;

import com.color.colornamer.Preview.PreviewListener;

import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


//class to hold the camera preview & manage the layouts & callbacks associated with the camera fragment
public class CameraActivity extends MenuActivity implements PreviewListener, OnTouchListener, ColorViewDialog.ColorViewDialogListener {
	
	private Menu menu;
	private Preview mPreview;
	private boolean isPaused = false; // whether or not the preview is paused
	private TextView colorView1;
	private TextView colorView2;
	private Button button;
	private ColorData cdata;
	private int radius = 5;
	private OutlineDrawableView centerView;
	private boolean sharing = false;
	
	private int[] pausedPixels = null;
	private int pausedWidth;
	private int pausedHeight;
	private boolean pausedDarkColor;
	
	//x and y where we take the reading
	private int cX;
	private int cY;
		
	String currentColor;
	String currentNamedColor; // closest named color (hex)

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// The camera activity is the main activity.  So, we need to default to the chooser activity if the camera doesn't exist.
		if (!this.hasCamera || this.sdk < 9) {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			this.finish();
			return;
		}
				
		cdata = new ColorData(this);
		
		setContentView(R.layout.activity_camera);
				
		//in the dialogs, clicking on named colors pauses & closing that resumes!
		colorView1 = (TextView) findViewById(R.id.camera_result1_textview);
		colorView2 = (TextView) findViewById(R.id.camera_result2_textview);

		colorView1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ColorViewDialog dialog = new ColorViewDialog();
				if (!isPaused) {
					pause();
					dialog.setDestroyCallback(CameraActivity.this);
				}
				dialog.setColor(currentColor, "You chose " + currentColor);
				dialog.show(getSupportFragmentManager(), "color_view");
			}  	
        });
		colorView2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ColorViewDialog dialog = new ColorViewDialog();
				if (!isPaused) {
					pause();
					dialog.setDestroyCallback(CameraActivity.this);
				}
				dialog.setColor(currentNamedColor, cdata.getColorName(currentNamedColor) + " (" + currentNamedColor  + ")");
				dialog.show(getSupportFragmentManager(), "color_view");
			}
        });
		
		//convert to dp
		radius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radius, getResources().getDisplayMetrics());
		
		mPreview = new Preview(this);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);
		
		LinearLayout centerLayout = (LinearLayout) findViewById(R.id.camera_activity_center);
		centerView = new OutlineDrawableView(this, radius);
		centerLayout.addView(centerView);
		centerLayout.setOnTouchListener(this);	
		//later we initialize the center in a more robust way
		cX = 2*radius;
		cY = 2*radius;
		
		button = (Button) findViewById(R.id.play_pause_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				pause();
			}
		});		
	}
	
	@Override
	// this is the first time i can get the width/height of my elements, allowing me to find the center point
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		cX = centerView.getWidth()/2;
		cY = centerView.getHeight()/2;
		centerView.move(cX, cY);
		centerView.invalidate();
	}
	
	
	@Override
    	protected void flash(){
        	if(getBaseContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){

            		button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.av_pause_over_video, 0, 0, 0);
            		mPreview.resetBuffer();
            		mPreview.flash();

		}
        //Toast massage when no Flash support on Handy
    	}
	
	
	
	@Override
	public void onResume() {
		super.onResume();
		if (isPaused) {
			//or just automatically have it play...
			pause();
		}
	}
	
	@Override
	public void onDestroy() {
		//clean up temp image files
		if (sharer != null) sharer.onDestroy();
		super.onDestroy();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_camera, menu);

        if (getBaseContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            menu.add(Menu.NONE, R.string.menu_flash, 0,getString(R.string.menu_flash));
        }

        this.menu = menu;
        return true;
    }

    
    private void updateColors() {
    	int red = 0;
        int green = 0;
        int blue = 0;
        int cWidth = centerView.getWidth();
        int cHeight = centerView.getHeight();
        int scaleX = (int) ((1.0*(cWidth- cX))/cWidth*pausedHeight);
        int scaleY;
        if (mPreview.isFrontCamera()) {
        	scaleY = (int) ((1.0*(cHeight - cY))/cHeight*pausedWidth);
        } else {
        	scaleY = (int) ((1.0*cY)/cHeight*pausedWidth);
        }
        for (int i = scaleX-radius; i < scaleX+radius;i++) {
        	for (int j = scaleY-radius; j < scaleY+radius;j++) {
        		int index = i*pausedWidth + j;
        		int pixel = pausedPixels[index];
        		red += Color.red(pixel);
        		green += Color.green(pixel);
        		blue += Color.blue(pixel);
        	}
        }
        int color = Color.rgb((int) (red/(1.0*(2*radius)*(2*radius))), (int) (green/(1.0*(2*radius)*(2*radius))), (int) (blue/(1.0*(2*radius)*(2*radius))));
        int[] col = {Color.red(color), Color.green(color), Color.blue(color)};
		currentColor = cdata.ColorToString(col);
		currentNamedColor = cdata.closestColor(col);
		boolean isDarkColor = cdata.isDarkColor(col);

		this.pausedDarkColor = isDarkColor;

		colorView1.setBackgroundColor(color);
		colorView1.setText("you chose " + currentColor);
		
		colorView2.setBackgroundColor(Color.parseColor(currentNamedColor));
		if (colorView2.getTag().equals("layout") || colorView2.getTag().equals("layout-small")) colorView2.setText(("" + cdata.getColorName(currentNamedColor) + "    " + "(" + currentNamedColor + ")"));
		else colorView2.setText("" + cdata.getColorName(currentNamedColor) + '\n' + "(" + currentNamedColor + ")");
		
		if (isDarkColor) {
			colorView1.setTextColor(Color.WHITE);
			colorView2.setTextColor(Color.WHITE);
		} else {
			colorView1.setTextColor(Color.BLACK);
			colorView2.setTextColor(Color.BLACK);
		}
    }
    
	@Override
	//TODO: it seems this is getting called once after pausing the preview 
	public void OnPreviewUpdated(int[] pixels, int width, int height) {
		if (pixels != null) {
			this.pausedPixels = pixels;
			this.pausedWidth = width;
			this.pausedHeight = height;
			
			//TODO: put this in another thread or something for performance
	        updateColors();
	
			/*if (sharing) {
				Log.e("BAD THINGS", "sharing, but in the camera preview anyway!");
			}*/
			if (isPaused) {
				//Log.e("BAD THINGS", "paused, but in the camera preview anyway!");
			} else {
				//once we are done with this info, let's reset the preview buffer & get another previewUpdate
				mPreview.resetBuffer();
			}
		}
	}
	
	@Override
	// mPreview flips a boolean for the previewCallback
	protected void share() {
		sharing = true;
		if (!isPaused) {
			mPreview.pause(true);
			//play the camera shutter sound
			//http://developer.android.com/reference/android/media/MediaActionSound.html also
			/*if (sdk >= 16) {
				android.media.MediaActionSound soundPlayer = new android.media.MediaActionSound();
				soundPlayer.play(android.media.MediaActionSound.SHUTTER_CLICK);
			}*/
		}
		saveScreenshot(pausedPixels, pausedWidth, pausedHeight, pausedDarkColor);
    }
	
	public void pause() {
		isPaused = !isPaused;
		mPreview.pause(isPaused);
		if (isPaused) {
			button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.av_play_over_video, 0, 0, 0);
			button.setText(" resume");
		} else {
			button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.av_pause_over_video, 0, 0, 0);
			button.setText("  pause");
			mPreview.resetBuffer();
		}
	}
	
	private Bitmap makeBitmapfromPixels(int[] pixels, int width, int height) {
		//rotated 90degrees
		Bitmap bTemp = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
		Matrix matrix = new Matrix();
		//http://stackoverflow.com/questions/8608734/android-rotate-bitmap-90-degrees-results-in-squashed-image-need-a-true-rotate-b
		//if it is a front camera, the rotation needs to be -90 & it needs to be mirrored
		if (mPreview.isFrontCamera()) {
			matrix.setScale(1, -1);
			matrix.postRotate(-90); //setRotate wipes out the earlier scale matrix.  need to use postRotate
		}
		else matrix.setRotate(90, 0, 0);
		matrix.postTranslate(bTemp.getHeight(), 0);
		return Bitmap.createBitmap(bTemp, 0, 0, bTemp.getWidth(), bTemp.getHeight(), matrix, true);
	}
	
	// verbatum from http://stackoverflow.com/questions/3674441/combining-2-images-overlayed
	public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, 0, 0, null);
        return bmOverlay;
    }

	
	//generate the bitmap & use the sharer to share it
	private void saveScreenshot(int[] pixels, int width, int height, boolean isDarkColor) {
		Bitmap bmp1 = makeBitmapfromPixels(pixels, width, height);
		
		//bmp2 has transparent colors
		View view = findViewById(R.id.camera_activity_layout);
		//hide the button
		button.setVisibility(View.INVISIBLE);
		view.setDrawingCacheEnabled(true);
		
		Bitmap bmp2 = view.getDrawingCache();
		Bitmap scaledbmp1 = Bitmap.createScaledBitmap(bmp1, bmp2.getWidth(), bmp2.getHeight(), false);		
		Bitmap bitmap = overlay(scaledbmp1,bmp2);
		
		view.setDrawingCacheEnabled(false);		
		button.setVisibility(View.VISIBLE);
		
        String name = cdata.getColorName(currentNamedColor);
        
        sharer.Share(bitmap, name, currentNamedColor);
        
    	sharing = false;
    	if (!isPaused) mPreview.pause(isPaused);
	}

	@Override
	//pick colors across the screen
	public boolean onTouch(View view, MotionEvent event) {
		int numTouch = event.getPointerCount();
    	if (numTouch == 1) {
    		int x = (int) event.getX();
    		int y = (int) event.getY();
    		if (x < 2*radius) x = 2*radius;
    		else if (x > view.getWidth()-2*radius) x = view.getWidth() - 2*radius;
    		if (y < 2*radius) y = radius;
    		else if (y > view.getHeight() - 2*radius) y = view.getHeight() - 2*radius;
    		cX = x;
    		cY = y;
			centerView.move(cX, cY);
			centerView.invalidate();
			if (isPaused) {
				updateColors();
			}
			return true;
    	}
		return false;
	}

	//set the wallpaper to be the named color
    @Override
    protected void wallpaper() {
   		Bitmap bitmap = sharer.createBitmap(currentNamedColor);
   		WallpaperManager wm = WallpaperManager.getInstance(this.getApplicationContext());
   		try {
			wm.setBitmap(bitmap);
    		Toast toast = Toast.makeText(this, "Wallpaper set", Toast.LENGTH_SHORT);
    		toast.setGravity(Gravity.CENTER, 0, 0);
    		toast.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    @Override
	public void onColorDialogPositiveClick(DialogFragment dialog, String color) {
		Bitmap bitmap = sharer.createBitmap(color);
		WallpaperManager wm = WallpaperManager.getInstance(this.getApplicationContext());
		try {
			wm.setBitmap(bitmap);
    		Toast toast = Toast.makeText(this, "Wallpaper set", Toast.LENGTH_SHORT);
    		toast.setGravity(Gravity.CENTER, 0, 0);
    		toast.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    

}
