package com.color.colornamer;


import android.os.Bundle;
import android.support.v4.app.FragmentTransaction; //TODO: how to do this?
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MenuActivity extends FragmentActivity {
	
	private Menu menu;
	protected boolean hasCamera = false;
	protected int sdk;
	protected Sharer sharer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		sdk = android.os.Build.VERSION.SDK_INT;
		
		//for hiding the camera option for devices w/ no camera
		//feature_camera_any is api level 17
		PackageManager manager = getPackageManager();
		if (manager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) || manager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) hasCamera = true;
		
		sharer = new Sharer(this);

	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

    	//maybe someday support API level 8
    	if (hasCamera && sdk >= 9) {
            getMenuInflater().inflate(R.menu.activity_main, menu);
    	} else {
    		getMenuInflater().inflate(R.menu.activity_main_nocamera, menu);
    	}
    	
        this.menu = menu;
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
    	Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_info:
            	// launch the info menu
            	intent = new Intent(this, InfoActivity.class);
            	startActivity(intent);
                return true;
            case R.id.menu_help:
            	intent = new Intent(this, HelpActivity.class);
            	startActivity(intent);
                return true;
            case R.id.menu_settings:
                return true;
            case R.id.menu_camera:
            	intent = new Intent(this, CameraActivity.class);
            	startActivity(intent);
            	return true;
            case R.id.menu_search:
            	View fragLayout = findViewById(R.id.fragLayout);
            	if (fragLayout.getVisibility() != View.VISIBLE) {
            		menu.setGroupVisible(R.id.menu_group, false);
	            	fragLayout.setVisibility(View.VISIBLE);
	            	FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().add(R.id.fragLayout, new SearchFragment());
	            	//transaction.addToBackStack(null);
	            	transaction.commit();
            	}
            	return true;
            case R.id.menu_share:
            	share();
            	return true;
            case R.id.menu_wallpaper:
            	wallpaper();
            	return true;
            case R.id.menu_chooser:
            	intent = new Intent(this, MainActivity.class);
            	startActivity(intent);
            	return true;
        	case android.R.id.home:
        		intent = new Intent(this, CameraActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        		startActivity(intent);
        		return true;
	    case R.string.menu_flash:
                 flash();
                 return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    protected void wallpaper() {
    	//empty
    }
    
    protected void share() {
    	//empty
    }
    
    protected void flash() {
        //empty
    }
    
    protected void setMenuVisible() {
    	menu.setGroupVisible(R.id.menu_group, true);
    }
    

}
