/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.realtek.bitmapfun.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

/**
 * A simple subclass of {@link ImageResizer} that fetches and resizes images
 * fetched from a URL.
 */
public class ImageFetcher extends ImageResizer {
    private static final String TAG = "ImageFetcher";
    private static final int HTTP_CACHE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final String HTTP_CACHE_DIR = "http";
     File cacheDir = null;//DiskLruCache.getDiskCacheDir(context, HTTP_CACHE_DIR);

     DiskLruCache cache =null;
            //DiskLruCache.openCache(context, cacheDir, HTTP_CACHE_SIZE);
    
    /**
     * Initialize providing a target image width and height for the processing
     * images.
     * 
     * @param context
     * @param loadingControl
     * @param imageWidth
     * @param imageHeight
     */
    public ImageFetcher(Context context, LoadingControl loadingControl,
            int imageWidth, int imageHeight)
    {
        super(context, loadingControl, imageWidth, imageHeight);
        init(context);
    }

    /**
     * Initialize providing a single target image size (used for both width and
     * height);
     * 
     * @param context
     * @param loadingControl
     * @param imageSize
     */
    public ImageFetcher(Context context, LoadingControl loadingControl,
            int imageSize)
    {
        super(context, loadingControl, imageSize);
        init(context);
    }

    private void init(Context context) {
        checkConnection(context);
        cacheDir = DiskLruCache.getDiskCacheDir(context, HTTP_CACHE_DIR);

        cache = DiskLruCache.openCache(context, cacheDir, HTTP_CACHE_SIZE);
    }

    /**
     * Simple network connection check.
     * 
     * @param context
     */
    private void checkConnection(Context context) {
    	/*
        final ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
            Toast.makeText(context, "No network connection found.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "checkConnection - no connection found");
        }*/
    }

    /**
     * The main process method, which will be called by the ImageWorker in the
     * AsyncTask background thread.
     * 
     * @param data The data to load the bitmap, in this case, a regular http URL
     * @return The downloaded and resized bitmap
     */
    private Bitmap processBitmap(String data) {
        return processBitmap(data, mImageWidth, mImageHeight);
    }

    /**
     * The main process method, which will be called by the ImageWorker in the
     * AsyncTask background thread.
     * 
     * @param data The data to load the bitmap, in this case, a regular http URL
     * @param imageWidth
     * @param imageHeight
     * @return The downloaded and resized bitmap
     */
    protected Bitmap processBitmap(String data, int imageWidth, int imageHeight) {
        /*if (BuildConfig.DEBUG) {
            Log.d(TAG, "processBitmap - " + data);
        }*/

    	//Log.d(TAG, "processBitmap - " + data);
    	
    	
    	//Log.d(TAG, "processBitmap - " + data);
        if (data == null)
        {
            return null;
        }
        
        
    	if(data !=null)
    	{
    		//android.resource:// 

    		String tmpStr=null,
    				httpTmpStr=null;
    		if(data.length()>19)
    			 tmpStr = data.substring(0, 19);
    		
    		if(data.length()>19)
    			 httpTmpStr = data.substring(0, 7);
    		
    		//Log.d(TAG, "httpTmpStr - " + httpTmpStr);
        	
    		
    		
    		if(tmpStr != null && tmpStr.equals("android.resource://"))
    		{
    			return processBitmap(Integer.parseInt( data.substring(tmpStr.length(), data.length())), imageWidth,imageHeight);
    		}
    		else if(httpTmpStr!=null && httpTmpStr.equals("http://"))
    		{
    			bCancel = false;
    	        // Download a bitmap, write it to a file
    			
    			//check httpcache
    	        final File f = downloadBitmap(mContext, data);
    	        if (f != null) {
    	            // Return a sampled down version
    	        	System.out.println("decodeSampledBitmapFromFile"+imageWidth+" "+imageHeight);
    	        	Bitmap bitmap = decodeSampledBitmapFromFile(f.toString(), imageWidth, imageHeight);  
    	        	
    	        	return bitmap;
    	        }
    		}
    		else
    		{
    			 // Download a bitmap, write it to a file
    	        final File f = new File(data);

    	        if (f == null || f.exists() == false)
    	        {	
    	        	return null;
    	        }
    			// Return a sampled down version
    			return decodeSampledBitmapFromFile(data, imageWidth, imageHeight);
    		}	
    			
    	}	
    	
        return null;
    }

    
    @Override
    protected Bitmap processBitmap(Object[] data) {
    	boolean isExif=(Boolean) data[1];
    	if(isExif!=true)
    	{
    		String path= String.valueOf(data[0]);
    		return processBitmap(path);
    	}
    	else if(isExif==true)
    	{
    		String exifpath=String.valueOf(data[0]);
    		return processBitmap(exifpath,isExif);
    	}
    	else 
    	{
    		//Log.d(TAG, "processBitmap get arg failed");
    		return null;
    	}
        
    }
    
   
    /**
     * Download a bitmap from a URL, write it to a disk and return the File
     * pointer. This implementation uses a simple disk cache.
     * 
     * @param context The context to use
     * @param urlString The URL to fetch
     * @return A File pointing to the fetched bitmap
     */

    public File downloadBitmap(Context context, String urlString)   
    {
    	//Log.d(TAG, "downloadBitmap -n http cache - " + urlString);
    	
    	if (bCancel== true) 
    		return null;
    	
        

        String path = cache.createFilePath(urlString);
        if (cache.containsFileKey(urlString)) {
            
        	Log.v(TAG,"Http download file Cache hit!");
        	/*
        	if (BuildConfig.DEBUG) {
                Log.dnloadBitmap - found in http cache - " + urlString);
            }*/
        	//Log.d(TAG, "downloadBitmap - found in http cache - " + urlString);
            return new File(path);
        }
        File cacheFile = new File(path);
        /*
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "downloadBitmap - downloading - " + urlString);
        }*/
        
        //Log.d(TAG, "downloadBitmap - downloading - " + urlString);

        Utils.disableConnectionReuseIfNecessary();
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        
        try {
        	URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(30000);
            urlConnection.setReadTimeout(30000);
            in = new BufferedInputStream(urlConnection.getInputStream(), Utils.IO_BUFFER_SIZE);
            out = new BufferedOutputStream(new FileOutputStream(cacheFile),Utils.IO_BUFFER_SIZE);
            
            byte[] buffer = new byte[Utils.IO_BUFFER_SIZE];
            int size = 0;
            while ((size = in.read(buffer)) > 0) 
            {
            	if(bCancel == true)
            	{
            		out.close();
            		in.close();
               		urlConnection.disconnect();

            		cacheFile.delete();
            		break;
            	}
                out.write(buffer, 0, size);
            }
            cache.putFileToCache(urlString, path);
            return cacheFile;

        } catch (final IOException e) {
            Log.e(TAG, "Error in downloadBitmap - " + e);
        } finally {
            if (out != null) {
                try {
                	out.flush();
                    out.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error in downloadBitmap - " + e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error in downloadBitmap - " + e);
                }
            } 
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;
    }
    
    public static boolean downloadURLtoStream(Context context, String urlString,OutputStream os) {
    
    	HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;

        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), Utils.IO_BUFFER_SIZE);
            out = new BufferedOutputStream(os, Utils.IO_BUFFER_SIZE);

            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;
        } catch (final IOException e) {
            Log.e(TAG, "Error in downloadBitmap - " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {}
        }
        return false;
    }
    
}
   