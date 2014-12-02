package com.fd.basichttpserver.impl;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

import com.fd.basichttpserver.HttpReqHandler;
import com.fd.basichttpserver.entity.StringEntityWithGzipCompress;

public class ReqHandler extends HttpReqHandler {

	@Override
	public void doGet(HttpRequest request, HttpResponse response,
			HttpContext context) {
		doPost(request,response,context);
	}

	@Override
	public void doPost(HttpRequest request, HttpResponse response,
			HttpContext context) {
		boolean useGzip = useGzip(request);
		response.setStatusCode(HttpStatus.SC_OK);
		if (useGzip) {
			response.setHeader("Content-Encoding", "gzip");
		}
		String result = "{\"code\":200}";
		StringEntity entity = null;
		if (!useGzip) {
			entity = new StringEntity(result,ContentType.create("text/plain", "utf-8"));
		} else {
			entity = new StringEntityWithGzipCompress(result,ContentType.create("text/plain", "utf-8"));
		}
		response.setEntity(entity);
	}
	
	private boolean useGzip (HttpRequest request) {
		Header[] headers = request.getHeaders("Accept-Encoding");
		if (headers != null) {
			String v = headers[0].getValue().toLowerCase();
			return v.contains("gzip");
		}
		return false;
	}
}
