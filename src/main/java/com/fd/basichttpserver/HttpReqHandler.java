package com.fd.basichttpserver;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

/**
 * http请求的实际处理类
 * @author caoly
 *
 */

public interface HttpReqHandler {
	public void handleReq(final HttpRequest request,
            final HttpResponse response,
            final HttpContext context);
	public void doGet(final HttpRequest request,
            final HttpResponse response,
            final HttpContext context);
	public void doPost(final HttpRequest request,
            final HttpResponse response,
            final HttpContext context);
}
