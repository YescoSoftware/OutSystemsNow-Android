/*
 * OutSystems Project
 * 
 * Copyright (C) 2014 OutSystems.
 * 
 * This software is proprietary.
 */
package com.yesco.android.core;

import android.util.Log;

import com.yesco.android.BuildConfig;

/**
 * Class description.
 * 
 * @author <a href="mailto:vmfo@xpand-it.com">vmfo</a>
 * @version $Revision: 666 $
 * 
 */
public class EventLogger {
    /**
     * Log message.
     * 
     * @param className the class name
     * @param message the message
     */
    public static void logMessage(String className, String message) {
        if (BuildConfig.DEBUG) {
            Log.d("[ OutSystems - " + className + "]", message);
        }
    }

    /**
     * Log message.
     * 
     * @param classObject the class object
     * @param message the message
     */
    public static void logMessage(Class<?> classObject, String message) {
        if (BuildConfig.DEBUG) {
            Log.d("[ OutSystems - " + classObject.getName() + "]", message);
        }
    }

    /**
     * Log info message.
     * 
     * @param className the class name
     * @param message the message
     */
    public static void logInfoMessage(String className, String message) {
        if (BuildConfig.DEBUG) {
            Log.i("[ OutSystems - " + className + "]", message);
        }
    }

    /**
     * Log info message.
     * 
     * @param classObject the class object
     * @param message the message
     */
    public static void logInfoMessage(Class<?> classObject, String message) {
        if (BuildConfig.DEBUG) {
            Log.i("[ OutSystems - " + classObject.getName() + "]", message);
        }
    }

    /**
     * Log error.
     * 
     * @param className the class name
     * @param errorMessage the error message
     */
    public static void logError(String className, String errorMessage) {
        if (BuildConfig.DEBUG) {
            Log.e("[ OutSystems - " + className + "]", errorMessage);
        }
    }

    /**
     * Log error.
     * 
     * @param className the class name
     * @param ex the ex
     */
    public static void logError(String className, Exception ex) {
        if (BuildConfig.DEBUG) {
            Log.e("[ OutSystems - " + className + "]", ex.getMessage(), ex);
        }
    }

    /**
     * Log error.
     * 
     * @param classObject the class object
     * @param ex the ex
     */
    public static void logError(Class<?> classObject, Exception ex) {
        if (BuildConfig.DEBUG) {
            Log.e("[ OutSystems - " + classObject.getName() + "]", ex.getMessage(), ex);
        }
    }

    /**
     * Log error.
     * 
     * @param classObject the class object
     * @param errorMessage the error message
     */
    public static void logError(Class<?> classObject, String errorMessage) {
        if (BuildConfig.DEBUG) {
            Log.e("[ OutSystems - " + classObject.getName() + "]", errorMessage);
        }
    }

}
