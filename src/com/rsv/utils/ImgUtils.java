package com.rsv.utils;

import java.io.InputStream;
import java.lang.reflect.Field;

import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class ImgUtils
{
	public static int[] getImageSizeScaleTo(final ImageView imageView)
	{
		int width = -1;
		int height = -1;

		// Check maxWidth and maxHeight parameters
		try
		{
			Field maxWidthField = ImageView.class.getDeclaredField("mMaxWidth");
			Field maxHeightField = ImageView.class.getDeclaredField("mMaxHeight");
			maxWidthField.setAccessible(true);
			maxHeightField.setAccessible(true);
			int maxWidth = (Integer) maxWidthField.get(imageView);
			int maxHeight = (Integer) maxHeightField.get(imageView);

			if (maxWidth >= 0 && maxWidth < Integer.MAX_VALUE)
			{
				width = maxWidth;
			}
			if (maxHeight >= 0 && maxHeight < Integer.MAX_VALUE)
			{
				height = maxHeight;
			}
		}
		catch (Exception e)
		{
			LogUtils.logException(e);
		}

		if (width < 0 && height < 0)
		{
			// Get layout width and height parameters
			LayoutParams params = imageView.getLayoutParams();
			width = params.width;
			height = params.height;
		}

		if (width < 0 && height < 0)
		{

		}

		return new int[] { width, height };
	}

	public static int computeImageScale(final InputStream imageStream, int width, int height,
			boolean fastway)
	{
		Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(imageStream, null, options);

		int scale = 1;

		if (fastway)
		{
			// Find the correct scale value. It should be the power of 2.
			int width_tmp = options.outWidth;
			int height_tmp = options.outHeight;

			while (true)
			{
				if (width_tmp / 2 < width || height_tmp / 2 < height)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}
		}
		else
		{
			int widthScale = (int) (Math.floor(((double) options.outWidth) / width));
			int heightScale = (int) (Math.floor(((double) options.outHeight) / height));
			int minScale = Math.min(widthScale, heightScale);
			if (minScale > 1)
			{
				scale = minScale;
			}
		}

		return scale;
	}
}
