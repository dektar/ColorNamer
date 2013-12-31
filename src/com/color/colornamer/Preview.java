package com.color.colornamer;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

//TODO: fail gracefully when camera is null - go back to MainActivity

/* 
 * Copied nearly verbatum from this helpful blog post
 * http://www.41post.com/3470/programming/android-retrieving-the-camera-preview-as-a-pixel-array 
 */ 
class Preview extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback { 
	
	public interface PreviewListener {
		public void OnPreviewUpdated(int[] pixels, int width, int height);
	}
	
	PreviewListener listener;
    SurfaceHolder mHolder;  
    Camera mCamera = null;  
    byte[] buffer;
    int bufferSize;
    private boolean isFrontCamera = false;
    boolean lightOn = false;
    
    private boolean isPaused = false;
          
    //This variable is responsible for getting and setting the camera settings  
    private Parameters parameters;  
    //this variable stores the camera preview size   
    private Size previewSize;  
    //this array stores the pixels as hexadecimal pairs   
    private int[] pixels;  
      
    Preview(Context context) {  
        super(context);  
                  
        // Install a SurfaceHolder.Callback so we get notified when the  
        // underlying surface is created and destroyed.  
        mHolder = getHolder();  
        mHolder.addCallback(this);  
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  
        
        try {
            // Instantiate the EnterColorListener so we can send events to the host
            listener = (PreviewListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement PreviewListener");
        }
    }  
  
    public void surfaceCreated(SurfaceHolder holder) {  
        // The Surface has been created, acquire the camera and tell it where  
        // to draw.  
        try {
        	CameraInfo info = new CameraInfo();
        	Camera.getCameraInfo(0, info);
        	if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
        		this.isFrontCamera = true;
        	}
            mCamera = Camera.open(0); // attempt to get a Camera instance.  index b/c front cameras are ok too.
            
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        	Log.e("camera error", "could not open camera");
        	return;
        }
        try {  
           mCamera.setDisplayOrientation(90);
           mCamera.setPreviewDisplay(holder);  
        } catch (IOException exception) {  
            mCamera.release();  
            mCamera = null;  
        }  
    } 
    
    public void flash() {

        if(getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            Parameters p = mCamera.getParameters();

            if(!lightOn){
                lightOn = true;
                mCamera.stopPreview();
                mCamera.setPreviewCallbackWithBuffer(this);//setPreviewCallback(this);//setPreviewCallbackWithBuffer(this);

                p.setFlashMode(Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(p);
                mCamera.startPreview();
            }else{
                lightOn = false;
                mCamera.stopPreview();
		mCamera.setPreviewCallbackWithBuffer(this);//setPreviewCallback(this);//setPreviewCallbackWithBuffer(this);


                p.setFlashMode(Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(p);
                mCamera.startPreview();
            }

        }
    }
    
  
    public void surfaceDestroyed(SurfaceHolder holder) {  
        // Surface will be destroyed when we return, so stop the preview.  
        // Because the CameraDevice object is not a shared resource, it's very  
        // important to release it when the activity is paused.  
        mCamera.stopPreview();  
        mCamera.setPreviewCallback(null);//setPreviewCallbackWithBuffer(null);
        mCamera.release();  
        mCamera = null;  
    }  
    
    public boolean isFrontCamera() {
    	return isFrontCamera;
    }
  
    /* TODO: fix the bug with the null pointer exception */
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {  
        // Now that the size is known, set up the camera parameters and begin  
        // the preview.  
    	if (mCamera != null) {
    		parameters = mCamera.getParameters();
              
            //to do autofocus, need to set the parameters if available
 	        //http://stackoverflow.com/questions/11623266/camera-parameters-setfocusmode-is-not-working
 	        List<String> focusModes = parameters.getSupportedFocusModes();
 	        if (focusModes != null) {
	 	        if (focusModes.contains(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
	 	        	parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
	 	        else if (focusModes.contains(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
	 	        	parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
	 	        else if (focusModes.contains(Parameters.FOCUS_MODE_AUTO))
	 	        	parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
 	        }
    		
    		//have to get previewSizes because not all devices support arbitrary previews
    		//the following is from Stack Overflow
 	        int width = this.getWidth();
 	        int height = this.getHeight();
 	        Size best = null;
	        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
	        // You need to choose the most appropriate previewSize for your app
	        for (int i = 0; i < previewSizes.size(); i++) {
	        	Size size = previewSizes.get(i);
	        	if ((size.width <= width && size.height <= height) || (size.height <= width && size.width <= height))  {
	                if (best==null) {
	                    best=size;
	                } else {
	                    int resultArea=best.width*best.height;
	                    int newArea=size.width*size.height;

	                    if (newArea>resultArea) {
	                        best=size;
	                    }
	               }
	        	}
	        }
	        
	        // make sure something is picked.  previewSizes is guarenteed to have at least one thing.
	        if (best != null) {
	        	previewSize = best; 
	        } else {
	        	previewSize = previewSizes.get(0);
	        }
	           
	        parameters.setPreviewSize(previewSize.width, previewSize.height);
            pixels = new int[previewSize.width * previewSize.height];  
	        mCamera.setParameters(parameters);
	        
    	    //sets the camera callback to be the one defined in this class  
            mCamera.setPreviewCallbackWithBuffer(this);//setPreviewCallback(this);//setPreviewCallbackWithBuffer(this);  
	        bufferSize = previewSize.width*previewSize.height*ImageFormat.getBitsPerPixel(parameters.getPreviewFormat())/8;
	        buffer = new byte[bufferSize];
	    	resetBuffer();
	        
	        if (!isPaused) mCamera.startPreview();
	    	
    	}
    }
    
    public void pause(boolean isPaused) {
    	this.isPaused = isPaused;
    	if (isPaused) {
            parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
    		mCamera.stopPreview();
    	} else {
    		if (mCamera != null) {
	    		mCamera.setPreviewCallbackWithBuffer(this);//setPreviewCallback(this);//setPreviewCallbackWithBuffer(this);

                if(lightOn)
                    parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);

                mCamera.setParameters(parameters);
                mCamera.startPreview();
    		}
    	}
    }
    
    public void resetBuffer() {
    	if (mCamera != null) {
        	mCamera.addCallbackBuffer(buffer);
    	}
    }

      
    @Override  
    public void onPreviewFrame(byte[] data, Camera camera) {  
        //transforms NV21 pixel data into RGB pixels  
        decodeYUV420SP(pixels, data, previewSize.width,  previewSize.height);  
        listener.OnPreviewUpdated(pixels, previewSize.width, previewSize.height);
    }  
      
    //Method from Ketai project! Not mine! See below...  
    void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {  
              
            final int frameSize = width * height;  
  
            for (int j = 0, yp = 0; j < height; j++) {       int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;  
              for (int i = 0; i < width; i++, yp++) {  
                int y = (0xff & ((int) yuv420sp[yp])) - 16;  
                if (y < 0)  
                  y = 0;  
                if ((i & 1) == 0) {  
                  v = (0xff & yuv420sp[uvp++]) - 128;  
                  u = (0xff & yuv420sp[uvp++]) - 128;  
                }  
  
                int y1192 = 1192 * y;  
                int r = (y1192 + 1634 * v);  
                int g = (y1192 - 833 * v - 400 * u);  
                int b = (y1192 + 2066 * u);  
  
                if (r < 0)                  r = 0;               else if (r > 262143)  
                   r = 262143;  
                if (g < 0)                  g = 0;               else if (g > 262143)  
                   g = 262143;  
                if (b < 0)                  b = 0;               else if (b > 262143)  
                   b = 262143;  
  
                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);  
              }  
         }  
    }  
    
}  
