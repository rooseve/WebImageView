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

	public static int getImageCacheSpaceInMB(final Context context) {

		int size = getPropInt(context, Constants.PropWebImageCacheSpaceInMB);
		return size > 0 ? size : 50;
	}

	public static String getImageCacheDir(final Context context) {

		String dir = getPropString(context, Constants.PropWebImgCacheDir);

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
