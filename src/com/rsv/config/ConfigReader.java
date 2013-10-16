package com.rsv.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.content.Context;
import android.content.res.AssetManager;

import com.rsv.utils.LogUtils;

public class ConfigReader {

	public static enum CacheStorage {
		EXTERNAL, INTERNAL
	};

	public static CacheStorage getImageCacheStorage(final Context context) {

		int type = getPropInt(context, Constants.PropWebImageCacheStorage);

		switch (type) {
			case 2:
				return CacheStorage.INTERNAL;

			default:
				return CacheStorage.EXTERNAL;
		}

	}

	public static double getImageCacheSpaceInMB(final Context context) {

		double spaceInMB = getPropDouble(context, Constants.PropWebImageCacheSpaceInMB);
		return spaceInMB > 0 ? spaceInMB : 50;
	}

	public static int getImageMemorySizeLimit(final Context context) {

		int size = getPropInt(context, Constants.PropWebImageMemorySizeLimit);
		return size >= 0 ? size : 20;
	}

	public static String getImageCacheDir(final Context context) {

		String dir = getPropString(context, Constants.PropWebImageCacheDir);

		return dir != null ? dir : Constants.DefaultWebImgCacheDir;
	}

	public static String getUseragentForImageLoader(final Context context) {

		String uagent = getPropString(context, Constants.PropUseragentForImageLoader);

		return uagent != null ? uagent : Constants.DefaultUseragentForImageLoader;
	}

	private static int getPropInt(final Context context, final String key) {

		String v = getPropString(context, key);
		return v != null ? Integer.parseInt(v) : 0;
	}

	private static double getPropDouble(final Context context, final String key) {

		String v = getPropString(context, key);
		return v != null ? Double.parseDouble(v) : 0;
	}

	private static String getPropString(final Context context, final String key) {

		AssetManager assetManager = context.getAssets();

		try {
			InputStream inputStream = assetManager.open(Constants.ConfigFileName);

			Properties properties = new Properties();
			properties.load(inputStream);

			return properties.getProperty(key);

		} catch (FileNotFoundException e) {

			return null;

		} catch (IOException e) {

			LogUtils.logException(e);

			return null;
		}
	}
}
