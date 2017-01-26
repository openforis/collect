package org.openforis.collect.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public abstract class AbstractClient {

	public <T extends Object> T getOne(String url, final Class<T> type) {
		return get(url, createSingleItemResponseProcessor(type));
	}

	public <T extends Object> List<T> getList(String url, final Class<T> type) {
		return getList(url, null, type);
	}
	
	public <T extends Object> List<T> getList(String url, Map<String, Object> params, final Class<T> type) {
		if (params != null) {
			url = appendQueryParams(url, params);
		}
		return get(url, new ResponseProcessor<List<T>>() {
			public List<T> process(Reader r) {
				T[] result = new GsonBuilder().create().fromJson(r, TypeToken.getArray(type).getType());
				return Arrays.asList(result);
			}
		});
	}

	public <T extends Object> T post(String url, Object data, Class<T> resultType) {
		return httpConnect(url, "POST", toPostData(data), createSingleItemResponseProcessor(resultType));
	}
	
	public <T extends Object> T patch(String url, Object data, Class<T> resultType) {
		return httpConnect(url, "PATCH", toPostData(data), createSingleItemResponseProcessor(resultType));
	}
	
	public void delete(String url) {
		httpConnect(url, "DELETE", null, new ResponseProcessor<Void>() {
			public Void process(Reader r) {
				return null;
			}
		});
	}
	
	protected <R extends Object> R get(String url, ResponseProcessor<R> responseProcessor) {
		return httpConnect(url, "GET", null, responseProcessor);
	}
	
	private <T> ResponseProcessor<T> createSingleItemResponseProcessor(final Class<T> type) {
		return new ResponseProcessor<T>() {
			public T process(Reader r) {
				T result = new GsonBuilder().create().fromJson(r, type);
				return result;
			}
		};
	}
	
	private <R extends Object> R httpConnect(String url, String method, byte[] data, ResponseProcessor<R> responseProcessor) {
		HttpURLConnection c = null;
		try {
			c = (HttpURLConnection) new URL(url).openConnection();

			c.setDoOutput(true);
			c.setDoInput(true);
			c.setUseCaches(false);
			c.setRequestMethod(method);
			c.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			c.setRequestProperty("Accept", "application/json");

			if (data != null) {
				OutputStream os = c.getOutputStream();
				os.write(data);
				os.flush();
			}
			
			if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("Failed : HTTP error code : " + c.getResponseCode());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader((c.getInputStream())));
			return responseProcessor.process(br);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (c != null) {
				c.disconnect();
			}
		}
	}
	
	private byte[] toPostData(Object data) {
		Gson gson = new GsonBuilder().create();
		String postData;
		if (data instanceof Map) {
			postData = gson.toJson(data, new TypeToken<Map<String, Object>>() {}.getType());
		} else {
			postData = gson.toJson(data);
		}
		try {
			return postData.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String appendQueryParams(String url, Map<String, Object> params) {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		for (Entry<String, Object> entry : params.entrySet()) {
			Object entryVal = entry.getValue();
			pairs.add(new BasicNameValuePair(entry.getKey(), entryVal == null ? null : entryVal.toString()));
		}
		return url + "?" + URLEncodedUtils.format(pairs, "UTF-8");
	}
	
	private interface ResponseProcessor<R extends Object> {
		
		R process(Reader r);
		
	}
	
}
