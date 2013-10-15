package com.rsv.utils;

import android.util.Log;

/**
 * Just a log wrapper
 * 
 */
public class LogUtils {
	public static void w(Object obj, String msg) {
		w(getTag(obj), msg);
	}

	public static void i(Object obj, String msg) {
		i(getTag(obj), msg);
	}

	public static void v(Object obj, String msg) {
		v(getTag(obj), msg);
	}

	public static void e(Object obj, String msg) {
		e(getTag(obj), msg);
	}

	public static void d(Object obj, String msg) {
		d(getTag(obj), msg);
	}

	public static void w(String tag, String msg) {
		Log.w(getTag(tag), msg);
	}

	public static void i(String tag, String msg) {
		Log.i(getTag(tag), msg);
	}

	public static void v(String tag, String msg) {
		Log.v(getTag(tag), msg);
	}

	public static void e(String tag, String msg) {
		Log.e(getTag(tag), msg);
	}

	public static void d(String tag, String msg) {
		Log.d(getTag(tag), msg);
	}

	public static void logException(Exception exp) {
		Log.e("Exception", exp.getMessage(), exp);
		exp.printStackTrace();
	}

	private static String getTag(Object obj) {
		return getTag(obj.getClass().getSimpleName());
	}

	private static String getTag(String tag) {
		return "Rsv_" + tag;
	}
}
