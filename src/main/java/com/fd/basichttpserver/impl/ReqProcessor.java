package com.fd.basichttpserver.impl;

import java.io.IOException;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.fd.basichttpserver.BasicReqProcessor;
import com.fd.basichttpserver.HttpServerConnectionEntry;
import com.fd.basichttpserver.HttpServerConnectionPool;

public class ReqProcessor extends BasicReqProcessor {
	private final static Logger logger = LogManager
			.getLogger(ReqProcessor.class);

	private final HttpService httpservice;
	private final HttpServerConnectionEntry connEntry;
	private final HttpServerConnectionPool connPool;
	
	public ReqProcessor(final HttpService httpservice,
			final HttpServerConnectionEntry connEntry,
			final HttpServerConnectionPool connPool) {
		super();
		this.httpservice = httpservice;
		this.connEntry = connEntry;
		this.connPool = connPool;
	}

	@Override
	public void doProcess() {
		HttpContext context = new BasicHttpContext(null);
		try {
			while (!Thread.interrupted() && this.connEntry.connection.isOpen()) {
				this.httpservice.handleRequest(this.connEntry.connection, context);
			}
		} catch (ConnectionClosedException ex) {
			logger.info("Client closed connection", ex);
		} catch (IOException ex) {
			logger.warn("I/O error: ", ex);
		} catch (HttpException ex) {
			logger.warn("Unrecoverable HTTP protocol violation:", ex);
		} catch (Exception e) {
			logger.warn("request processor error:", e);
		} finally {
			connPool.release(connEntry);
		}

	}

}
