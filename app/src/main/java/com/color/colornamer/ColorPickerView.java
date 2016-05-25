package com.color.colornamer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.TypedValue;
import android.view.View;

public class ColorPickerView extends View {

    // for the thumbnail
    private ShapeDrawable thumb;
    private ShapeDrawable thumb2;
    private int thumbRadius = 51; //value in dp, from 70 px on nexus 7
    private int thumbEdge = 1; //value in dp, from 1 px on nexus 7
    private boolean thumbIsVisible = false;

    // for the bitmap
    private Bitmap bitty;
    private static final int bwidth = 256;
    private static final int bheight = 256;
    private int[] pixels = new int[bwidth * bheight];
    private int[][] allPixels;
    private double factor = 3.1;
    public int paddingx = 0;
    public int paddingy = 0;

    public int size;

    // coordinates of the currently selected pixel (0-255)
    private int xp;
    private int yp;


    // value for the z part of the color graph
    private float kPrev = 150;

    private Rect r1 = new Rect(0, 0, bwidth, bheight);
    private Rect r2;

    public ColorPickerView(Context context, int blue, int density) {
        super(context);
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        bitty = Bitmap.createBitmap(bwidth, bheight, config);
        //initializeAllPixels(); // for computing all the possible colors ahead of time
        updateBitmap(blue);
        thumbRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, thumbRadius,
                getResources().getDisplayMetrics());
        thumbEdge = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, thumbEdge,
                getResources().getDisplayMetrics());
        createThumb(paddingx, paddingy, Color.BLACK);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = this.getWidth();
        int height = this.getHeight();
        if (height != 0 && changed) {
            if (width > height) {
                paddingx = (width - height + 1) / 2;
                size = height;
            } else if (width < height) {
                paddingy = (height - width + 1) / 2;
                size = width;
            }
            factor = (1.0 * size - 2.0 * Math.min(paddingx, paddingy)) / (1.0 * bwidth);
            r2 = new Rect(paddingx, paddingy, width - paddingx, height - paddingy);
        }
    }

    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitty, r1, r2, null);
        if (thumbIsVisible) {
            thumb.draw(canvas);
            thumb2.draw(canvas);
        }
    }

    // gets the color at a certain point
    public int getColor(float xf, float yf, boolean wasTouched) {
        int x = (int) xf;
        int y = (int) yf;

        if (!thumbIsVisible && wasTouched) {
            thumbIsVisible = true;
        }

        if (x >= r2.right) x = r2.right - 1;
        else if (x < r2.left) x = r2.left;
        if (y >= r2.bottom) y = r2.bottom - 1;
        else if (y < r2.top) y = r2.top;

        //xp and yp are in the scaled bitmap from 0-255
        xp = (int) ((1.0 * (x - paddingx)) / factor);
        yp = (int) ((1.0 * (y - paddingy)) / factor);

        moveThumb(x, y);

        int pixel = bitty.getPixel(xp, yp);
        thumb.getPaint().setColor(pixel);
        return pixel;
    }

    private void moveThumb(int x, int y) {
        thumb.setBounds(x - thumbRadius, y - thumbRadius, x + thumbRadius, y + thumbRadius);
        thumb2.setBounds(x - thumbRadius - thumbEdge, y - thumbRadius - thumbEdge, x + thumbRadius +
                thumbEdge, y + thumbRadius + thumbEdge);
    }

    public void noColor() {
        thumbIsVisible = false;
    }

    // called at multitouch events
    public int updateShade(double scale) {
        float k = ((float) scale - 1) * 100 + kPrev;
        if (k > 255) k = 255; // if hsv, set this to 360
        if (k < 0) k = 0; // can't let it go to 0 or we never scale it back again
        updateBitmap((int) k);
        if (thumbIsVisible) {
            thumbIsVisible = false;
        }
        int pixel = bitty.getPixel(xp, yp);
        thumb.getPaint().setColor(pixel);
        return pixel;
    }

    // called at seekbar events
    public int updateShade(int shade) {
        updateBitmap(shade);
        if (!thumbIsVisible) {
            thumbIsVisible = true;
        }
        //Log.d("color", "" + xp + " " + yp);
        int pixel = bitty.getPixel(xp, yp);
        thumb.getPaint().setColor(pixel);
        return pixel;
    }

    // sets the colorPicker's color to a certain r,g,b indicated by x,y,z
    public void setColor(int red, int green, int blue) {
        xp = red;
        yp = green;
        moveThumb((int) (xp * factor + paddingx), (int) (yp * factor + paddingy));
    }


    // creates the thumb nail viewer
    private void createThumb(int x, int y, int color) {
        thumb = new ShapeDrawable(new OvalShape());
        thumb.getPaint().setColor(color);

        thumb2 = new ShapeDrawable(new OvalShape());
        thumb2.getPaint().setColor(0x55FFFFFF);
        thumb2.getPaint().setStyle(Paint.Style.STROKE);
        thumb2.getPaint().setStrokeWidth(5);

        thumb.getPaint().setAntiAlias(true);
        thumb2.getPaint().setAntiAlias(true);
    }

    // updates the bitmap based on a blue factor
    private void updateBitmap(int k) {
        for (int i = 0; i < bwidth; i++) {
            for (int j = 0; j < bheight; j++) {
                pixels[i * bheight + j] = Color.rgb(j, i, k);
            }
        }
        kPrev = k;
        bitty.setPixels(pixels, 0, bwidth, 0, 0, bwidth, bheight);
    }

    // trying to pre-compute all pixels for HSV mode.
    private void initializeAllPixels() {
        allPixels = new int[360][bwidth * bheight];
        float[] hsv = new float[3];
        for (int k = 0; k < 360; k++) {
            hsv[0] = k;
            for (int i = 0; i < bwidth; i++) {
                hsv[1] = 1f * i / bwidth;
                for (int j = 0; j < bheight; j++) {
                    hsv[2] = 1f * j / bwidth;
                    allPixels[k][i * bheight + j] = Color.HSVToColor(hsv);
                }
            }
        }
    }
}