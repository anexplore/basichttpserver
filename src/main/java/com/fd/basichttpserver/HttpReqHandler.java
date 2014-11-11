package com.fd.basichttpserver;

import java.util.Locale;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * http请求的实际处理类
 * @author caoly
 *
 */

public abstract class HttpReqHandler implements HttpRequestHandler{
	private final static Logger logger = LogManager.getLogger();
	public void handle(final HttpRequest request,
            final HttpResponse response,
            final HttpContext context) {
		String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
        if (!method.equals("GET") && !method.equals("POST")) {
            logger.warn("Unsupport http method :" + method);
            return;
        }
        switch (method) {
        case "GET":
        	doGet(request,response,context);
        	break;
        case "POST":
        	doPost(request,response,context);
        	break;
        }
	}
	public abstract void doGet(final HttpRequest request,
            final HttpResponse response,
            final HttpContext context);
	public abstract void doPost(final HttpRequest request,
            final HttpResponse response,
            final HttpContext context);
}
