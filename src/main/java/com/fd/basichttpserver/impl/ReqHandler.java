package com.fd.basichttpserver.impl;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

import com.fd.basichttpserver.HttpReqHandler;

public class ReqHandler extends HttpReqHandler {

	@Override
	public void doGet(HttpRequest request, HttpResponse response,
			HttpContext context) {
		doPost(request,response,context);
	}

	@Override
	public void doPost(HttpRequest request, HttpResponse response,
			HttpContext context) {
		response.setStatusCode(HttpStatus.SC_OK);
		String result = "{\"code\":200}";
		StringEntity entity = new StringEntity(result,ContentType.create("text/plain", "utf-8"));
		response.setEntity(entity);
	}

}
