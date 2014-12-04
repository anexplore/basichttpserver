package com.fd.basichttpserver.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

import com.fd.basichttpserver.HttpReqHandler;
import com.fd.basichttpserver.entity.StringEntityWithGzipCompress;
import com.fd.basichttpserver.protocol.HttpConstant;
import com.fd.basichttpserver.utils.RequestParamterParser;

public class ReqHandler extends HttpReqHandler {

	@Override
	public void doGet(HttpRequest request, HttpResponse response,
			HttpContext context) {
		doPost(request,response,context);
	}

	@Override
	public void doPost(HttpRequest request, HttpResponse response,
			HttpContext context) {
		RequestParamterParser parser = new RequestParamterParser(request);
		Map<String,String> params = parser.getParams();
		for (Entry<String,String> entry : params.entrySet()) {
			System.out.println(entry.getKey() + "\t" + entry.getValue());
		}
		System.out.println(parser.getPath());
		System.out.println(parser.getRef());
		boolean useGzip = useGzip(request);
		response.setStatusCode(HttpStatus.SC_OK);
		if (useGzip) {
			response.setHeader(HttpConstant.CONTENT_ENCODING, HttpConstant.ENCODING_GZIP);
		}
		String result = "{\"code\":200}";
		StringEntity entity = null;
		if (!useGzip) {
			entity = new StringEntity(result,ContentType.create("text/plain", HttpConstant.UTF_8));
		} else {
			entity = new StringEntityWithGzipCompress(result,ContentType.create("text/plain", HttpConstant.UTF_8));
		}
		response.setEntity(entity);
	}
	
	/**
	 * head.getValue may be null
	 * @param request
	 * @return
	 */
	private boolean useGzip (HttpRequest request) {
		Header header = request.getFirstHeader(HttpConstant.ACCEPT_ENCODING);
		String value = header == null ? null : header.getValue().toLowerCase();
		return value == null ? false : value.contains(HttpConstant.ENCODING_GZIP);
	}
}
