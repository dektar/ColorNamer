package com.color.colornamer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

public class ColorData {
	
	// contains full hex color -> name map
	static Map<String,String> nameMap;
		
	// colors as lists of int
	public ArrayList<int[]> colors;
	
	public ColorData(Context context) {
		loadColors(context);
	}
	
	//load the colors from a text file to the HashMap
    private void loadColors(Context context) {
    	nameMap = new HashMap<String,String>();
    	colors = new ArrayList<int[]>();
    	try {
        	BufferedReader reader = null;
        	//FileInputStream inputStream = openFileInput(COLOR_FILE);
        	InputStream inputStream = context.getResources().openRawResource(R.raw.rgb);
        	
			reader = new BufferedReader(new InputStreamReader(inputStream));
			String line = reader.readLine();
	    	while (line != null) {
	    		int split = line.indexOf('#');
	    		String color = line.substring(split,line.length()-1);
	    		String name = line.substring(0,split-1);
	    		nameMap.put(color,name);
	    		colors.add(StringToColor(color));
	    		line = reader.readLine();
	    	}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    // sanity check to make sure the color entered is valid
    public boolean isValidColor(String str) {
    	if (str.matches("#([a-f0-9])*") && str.length() == 7) {
    		return true;
    	}
    	return false;
    }
    
 // converts a hex string to a int[3] representing the color, for searches
    private int[] StringToColor(String str) { 
    	int[] result = new int[3];
    	for (int i = 1; i < str.length(); i+=2) {
    		String substring = str.substring(i,i+2);
    		int in = Integer.parseInt(substring, 16);
    		result[i/2] = in;
    	}
    	return result;
    }
    
    // converts a int[3] to a string hex color
    public String ColorToString(int[] color) {
    	String result = "#";
    	for (int i = 0; i < color.length; i++) {
    		String part = Integer.toHexString(color[i]);
    		if (part.length() == 1) {
    			part = "0" + part;
    		}
    		result = result.concat(part);
    	}
    	return result;
    }

    // returns true if the color is dark.  useful for picking a font color.
    public boolean isDarkColor(int[] color) {
    	if (color[0]*.3 + color[1]*.59 + color[2]*.11 > 150) return false;
    	return true;
    }
    
    public String getColorName(String color) {
    	return nameMap.get(color);
    }
    
    // takes a valid color string and finds the closest color from XKCD's survey results
    // e.g. "#000000"
    public String closestColor(String col) {
    	String result = null;
    	int[] input = StringToColor(col);
    	int[] bestMatch = new int[3];
    	double bestDist = 1000;
    	// calculate distances to each of the colors in colors
    	for (int i = 0; i < colors.size(); i++) {
    		double dist = calculateDist(input, colors.get(i));
    		if (dist < bestDist) {
    			bestMatch = colors.get(i);
    			bestDist = dist;
    		}
    	}
    	result = ColorToString(bestMatch);
    	return result;
    }
    
    public String closestColor(int[] col) {
    	String result = null;
    	int[] bestMatch = new int[3];
    	double bestDist = 1000;
    	// calculate distances to each of the colors in colors
    	for (int i = 0; i < colors.size(); i++) {
    		double dist = calculateDist(col, colors.get(i));
    		if (dist < bestDist) {
    			bestMatch = colors.get(i);
    			bestDist = dist;
    		}
    	}
    	result = ColorToString(bestMatch);
    	return result;
    }
    
    // calculates the distance between two points
    // Distance = SquareRoot(xd*xd + yd*yd + zd*zd)
    private double calculateDist(int[] p1,int[] p2) {
    	int sum = 0;
    	for (int i = 0; i < p1.length; i++) {
    		double diff = p1[i]-p2[i];
    		sum += diff*diff;
    	}
    	return Math.sqrt(sum);
    }
    

}
