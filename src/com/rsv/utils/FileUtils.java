package com.rsv.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.channels.FileChannel;

public final class FileUtils {

	public static final Object[] DATA_LOCK = new Object[0];

	/**
	 * Copy file, return true on success, false on failure.
	 * 
	 * @param src
	 * @param dst
	 * @return
	 */
	public static boolean copyFile(final File src, final File dst) {
		boolean result = false;
		FileChannel inChannel = null;
		FileChannel outChannel = null;
		synchronized (DATA_LOCK) {
			try {
				inChannel = new FileInputStream(src).getChannel();
				outChannel = new FileOutputStream(dst).getChannel();
				inChannel.transferTo(0, inChannel.size(), outChannel);
				result = true;
			} catch (IOException e) {

			} finally {
				if (inChannel != null && inChannel.isOpen()) {
					try {
						inChannel.close();
					} catch (IOException e) {
						// ignore
					}
				}
				if (outChannel != null && outChannel.isOpen()) {
					try {
						outChannel.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}
		return result;
	}

	/**
	 * Replace entire File with contents of String, return true on success,
	 * false on failure.
	 * 
	 * @param fileContents
	 * @param file
	 * @return
	 */
	public static boolean writeStringAsFile(final String fileContents,
			final File file) {
		boolean result = false;
		try {
			synchronized (DATA_LOCK) {
				if (file != null) {
					file.createNewFile(); // ok if returns false, overwrite
					Writer out = new BufferedWriter(new FileWriter(file), 1024);
					out.write(fileContents);
					out.close();
					result = true;
				}
			}
		} catch (IOException e) {
			LogUtils.logException(e);
		}
		return result;
	}

	/**
	 * Append String to end of File, return true on success, false on failure.
	 * 
	 * @param appendContents
	 * @param file
	 * @return
	 */
	public static boolean appendStringToFile(final String appendContents,
			final File file) {
		boolean result = false;
		try {
			synchronized (DATA_LOCK) {
				if ((file != null) && file.canWrite()) {
					file.createNewFile(); // ok if returns false, overwrite
					Writer out = new BufferedWriter(new FileWriter(file, true),
							1024);
					out.write(appendContents);
					out.close();
					result = true;
				}
			}
		} catch (IOException e) {
			LogUtils.logException(e);
		}
		return result;
	}

	/**
	 * Read file as String, return null if file is not present or not readable.
	 * 
	 * @param file
	 * @return
	 */
	public static String readFileAsString(final File file) {
		StringBuilder sb = null;
		try {
			synchronized (DATA_LOCK) {
				if ((file != null) && file.canRead()) {
					sb = new StringBuilder();
					String line = null;
					BufferedReader in = new BufferedReader(
							new FileReader(file), 1024);
					while ((line = in.readLine()) != null) {
						sb.append(line + System.getProperty("line.separator"));
					}
					in.close();
				}
			}
		} catch (IOException e) {
			LogUtils.logException(e);
		}
		if (sb != null) {
			return sb.toString();
		}
		return null;
	}

	/**
	 * Call sync on a FileOutputStream to ensure it is written to disk
	 * immediately (write, flush, close, etc, don't guarantee physical disk
	 * write on buffered file systems).
	 * 
	 * @param stream
	 * @return
	 */
	public static boolean syncStream(FileOutputStream fos) {
		try {
			if (fos != null) {
				fos.getFD().sync();
			}
			return true;
		} catch (IOException e) {
			LogUtils.logException(e);
		}
		return false;
	}
}
