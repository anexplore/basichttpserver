package com.fd.basichttpserver.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.util.EntityUtils;

/**
 * parser request
 * 
 * @author caoly
 * 
 */
public class RequestParamterParser {
	private Map<String, String> params = new HashMap<String, String>(10);
	private String path = "";
	private String ref = "";
	public static final char QUERY_SPLITER = '?';
	public static final char KV_SPLITER = '=';
	public static final char PARAM_SPLITER = '&';
	public static final char REF_SPLITER = '#';
	public static final char HEADER_SPLITER = ';';

	public RequestParamterParser(HttpRequest request) {
		if (request == null) {
			return;
		}
		parser(request);
	}

	private void parser(HttpRequest request) {
		String target = request.getRequestLine().getUri();
		try {
			// target = URLDecoder.decode(target, "UTF-8");
			parserUri(target.toCharArray());
			// entity body
			if (request instanceof HttpEntityEnclosingRequest) {
				HttpEntity entity = ((HttpEntityEnclosingRequest) request)
						.getEntity();
				if (entity == null) {
					return;
				}
				byte[] entityContent = EntityUtils.toByteArray(entity);
				String charset = "utf-8";
				String content = new String(entityContent, charset);
				String[] pairs = content.split(PARAM_SPLITER + "");
				for (int i = 0; i < pairs.length; i++) {
					String kv = pairs[i];
					String[] pair = kv.split("=");
					if (pair.length != 2 && !pair[0].isEmpty()) {
						continue;
					}
					params.put(pair[0], pair[1]);
				}
			}

		} catch (Exception ignore) {
			ignore.printStackTrace();
		}
		try {
			path = URLDecoder.decode(path, "UTF-8");
			ref = URLDecoder.decode(ref, "UTF-8");
			Map<String, String> tmp = new HashMap<String, String>();
			for (Entry<String, String> entry : params.entrySet()) {
				tmp.put(URLDecoder.decode(entry.getKey(), "UTF-8"),
						URLDecoder.decode(entry.getValue(), "UTF-8"));
			}
			params = tmp;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void parserUri(char[] uri) {
		if (uri == null) {
			return;
		}
		int len = uri.length;
		StringBuilder sb = new StringBuilder();
		int i = 0;
		String key = "";
		String value = "";
		char lastSpliter = ' ';
		for (; i < len; i++) {
			if (lastSpliter == REF_SPLITER) {
				sb.append(uri[i]);
				continue;
			}
			if (uri[i] == QUERY_SPLITER) {
				path = sb.toString();
				sb.delete(0, sb.length());
				lastSpliter = uri[i];
				continue;
			}
			if (uri[i] == KV_SPLITER) {
				key = sb.toString();
				sb.delete(0, sb.length());
				lastSpliter = uri[i];
				continue;
			}
			if (uri[i] == PARAM_SPLITER) {
				value = sb.toString();
				sb.delete(0, sb.length());
				if (!key.isEmpty()) {
					params.put(key, value);
				}
				lastSpliter = uri[i];
				continue;
			}
			if (uri[i] == REF_SPLITER) {
				value = "";
				sb.delete(0, sb.length());
				lastSpliter = uri[i];
				continue;
			}
			sb.append(uri[i]);
		}
		switch (lastSpliter) {
		case ' ':
			path = sb.toString();
			break;
		case QUERY_SPLITER:
			break;
		case KV_SPLITER:
			params.put(key, sb.toString());
			break;
		case PARAM_SPLITER:
			break;
		case REF_SPLITER:
			ref = sb.toString();
			break;
		}
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public Map<String, String> getParams() {
		return params;
	}

}
