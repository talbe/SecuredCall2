/**
 * @file SecuredCallApplication.java
 * 
 * This file contains the implementation of the SecuredCallApplication class.
 * 
 * @author Kfir Gollan
 * @since 17/05/2014
 */
package com.example.securedcall;

import android.app.Application;
import android.content.Context;

/**
 * This class is an extension of the default application class provided
 * by the android framework. Its purpose is to expose needed features
 * for classes that don't have access to it on a regular basis.
 * 
 * @author Kfir Gollan
 * @since 17/05/2014
 */
public class SecuredCallApplication extends Application {
	/**
	 * The application context.
	 */
	private static Context s_cContext;
	
	public void onCreate() {
		super.onCreate();
		s_cContext = getApplicationContext();
	}
	
	public static Context getContext() {
		return s_cContext;
	}
}
