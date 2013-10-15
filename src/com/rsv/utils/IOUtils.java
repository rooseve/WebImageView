package com.rsv.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import com.rsv.comp.IProgressListener;

public class IOUtils {
	private static final int DEFAULT_BUFFER_SIZE = 1024;

	public static void copyStream(InputStream is, OutputStream os) throws IOException {
		copyStream(is, os, 1024, null);
	}

	public static void copyStream(InputStream is, OutputStream os, int bufferSize,
			IProgressListener obs) throws IOException {
		final int buffer_size = bufferSize;

		byte[] bytes = new byte[buffer_size];

		long p = 0;

		while (true) {
			int count = is.read(bytes, 0, buffer_size);

			if (count == -1) {
				break;
			}

			os.write(bytes, 0, count);

			if (obs != null) {
				p += count;
				obs.reportProgress(p);
			}
		}
	}

	/**
	 * http://www.iana.org/assignments/character-sets
	 * 
	 * @param input
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static String toString(InputStream input, String encoding) throws IOException {
		StringWriter sw = new StringWriter();
		copy(input, sw, encoding);
		return sw.toString();
	}

	public static void copy(InputStream input, Writer output, String encoding) throws IOException {
		if (encoding == null) {
			copy(input, output);
		} else {
			InputStreamReader in = new InputStreamReader(input, encoding);
			copy(in, output);
		}
	}

	public static void copy(InputStream input, Writer output) throws IOException {
		InputStreamReader in = new InputStreamReader(input);
		copy(in, output);
	}

	public static int copy(Reader input, Writer output) throws IOException {
		long count = copyLarge(input, output);
		if (count > Integer.MAX_VALUE) {
			return -1;
		}
		return (int) count;
	}

	public static long copyLarge(Reader input, Writer output) throws IOException {
		char[] buffer = new char[DEFAULT_BUFFER_SIZE];
		long count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
}
