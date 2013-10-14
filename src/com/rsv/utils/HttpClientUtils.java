package com.rsv.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

import com.rsv.comp.IProgressListener;
import com.rsv.config.Constants;

public class HttpClientUtils {

	private static final int connectionTimeOut = 3000;
	private static final int socketTimeOut = 20000;
	private static final int maxTotalConnections = 5;

	private static HttpClient singleGzipClient = null;

	public static HttpClient getSingleInstance() {

		if (singleGzipClient == null) {
			singleGzipClient = getNewHttpClient(true);
		}

		return singleGzipClient;
	}

	public static HttpClient getNewHttpClient(boolean gzip) {

		BasicHttpParams params = new BasicHttpParams();

		ConnManagerParams.setMaxTotalConnections(params, maxTotalConnections);

		HttpConnectionParams.setConnectionTimeout(params, connectionTimeOut);
		HttpConnectionParams.setSoTimeout(params, socketTimeOut);

		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setUseExpectContinue(params, false);

		HttpProtocolParams.setUserAgent(params, Constants.DefaultUseragentForImageLoader);

		HttpClientParams.setCookiePolicy(params, CookiePolicy.BROWSER_COMPATIBILITY);

		SchemeRegistry schemeRegistry = new SchemeRegistry();

		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

		ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params,
				schemeRegistry);

		DefaultHttpClient client = new DefaultHttpClient(manager, params);

		if (gzip) {

			client.addRequestInterceptor(new HttpRequestInterceptor() {
				public void process(HttpRequest request, HttpContext context) throws HttpException,
						IOException {
					if (!request.containsHeader("Accept-Encoding")) {
						request.addHeader("Accept-Encoding", "gzip");
					}
				}
			});

			client.addResponseInterceptor(new HttpResponseInterceptor() {
				public void process(final HttpResponse response, final HttpContext context)
						throws HttpException, IOException {
					HttpEntity entity = response.getEntity();
					Header ceheader = entity.getContentEncoding();
					if (ceheader != null) {
						HeaderElement[] codecs = ceheader.getElements();
						for (int i = 0; i < codecs.length; i++) {
							if (codecs[i].getName().equalsIgnoreCase("gzip")) {
								response.setEntity(new GzipDecompressingEntity(response.getEntity()));
								return;
							}
						}
					}
				}
			});
		}

		client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(3, false) {

			@Override
			public boolean retryRequest(IOException exception, int executionCount,
					HttpContext context) {

				if (!super.retryRequest(exception, executionCount, context)) {
					LogUtils.d("HTTP retry-handler", "Won't retry");
					return false;
				}

				OSUtils.sleepMillSecs(3 * 1000);

				LogUtils.d("HTTP retry-handler", "Retrying request..." + executionCount);
				return true;
			}
		});

		return client;
	}

	private static class GzipDecompressingEntity extends HttpEntityWrapper {

		public GzipDecompressingEntity(final HttpEntity entity) {
			super(entity);
		}

		@Override
		public InputStream getContent() throws IOException, IllegalStateException {

			InputStream wrappedin = wrappedEntity.getContent();
			return new GZIPInputStream(wrappedin);
		}
	}

	public static boolean downloadToFile(String urlString, File savedFile, String userAgent)
			throws ClientProtocolException, IOException {
		return downloadToFile(urlString, savedFile, userAgent, null);
	}

	public static boolean downloadToFile(String urlString, File savedFile, String userAgent,
			final IProgressListener progressObs) throws ClientProtocolException, IOException {

		HttpClient client = getSingleInstance();

		if (userAgent != null)
			client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgent);

		HttpGet get = new HttpGet(urlString);

		HttpEntity entity = null;

		try {
			// the connect phase take some time
			final int pbase = 5;

			if (progressObs != null) {
				progressObs.reportProgress(pbase / 3);
			}

			LogUtils.d("http", "fetch url: " + urlString);

			HttpResponse resp = client.execute(get);

			int sCode = resp.getStatusLine().getStatusCode();

			if (sCode != HttpStatus.SC_OK) {
				LogUtils.w("downloadToFile", "Got code " + sCode + " from "
						+ get.getURI().toString());

				return false;
			}

			entity = resp.getEntity();

			final long totalBytes = entity.getContentLength();

			if (progressObs != null) {
				progressObs.reportProgress(pbase);
			}

			InputStream is = entity.getContent();

			FileOutputStream os = new FileOutputStream(savedFile);

			IProgressListener dwobs = null;

			if (progressObs != null) {
				dwobs = new IProgressListener() {

					long lstp = 0;

					@Override
					public void reportProgress(long progress) {

						long p = pbase + progress * (100 - pbase) / totalBytes;

						if (p - lstp > 0) {

							progressObs.reportProgress(p);
							lstp = p;
						}

						// OSUtils.sleepMillSecs(500);
					}
				};
			}

			IOUtils.copyStream(is, os, 1024, dwobs);

			// os.flush();
			os.getFD().sync();
			os.close();

			is.close();

			LogUtils.d("http", "fetch url done: " + urlString);

			// entity.consumeContent();

			return true;

		} finally {
			get.abort();
		}
	}
}
