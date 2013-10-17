package com.rsv.config;

public class Constants {

	/**
	 * Prop file(in the assets folder) name
	 */
	public static final String ConfigFileName = "com.rsv.webimageview.config.properties";

	/**
	 * Prop names
	 */

	public static final String PropWebImageCacheSpaceInMB = "webImageCacheSpaceInMB";

	public static final String PropUseragentForImageLoader = "useragentForImageLoader";

	public static final String PropWebImageCacheStorage = "webImageCacheStorage";

	public static final String PropWebImageCacheDir = "webImageCacheDir";
	
	public static final String PropWebImageMemorySizeLimit = "webImageMemorySizeLimit";

	/**
	 * Default prop values
	 */

	public static final String DefaultUseragentForImageLoader = "Mozilla/5.0 (compatible; WebImageView/1.0; +https://github.com/rooseve/WebImageView)";

	public static final String DefaultWebImgCacheDir = "rsv_webimg_cache";
}
