package com.rsv.utils;

import android.os.Looper;

public class OSUtils {
	public static boolean isInMainThread() {
		return Thread.currentThread() == Looper.getMainLooper().getThread();
	}

	public static void sleepMillSecs(long m) {
		try {
			Thread.sleep(m);
		} catch (InterruptedException e) {
			LogUtils.logException(e);
		}
	}
}
