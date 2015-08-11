package com.fd.basichttpserver;

import java.io.IOException;

import org.apache.http.HttpServerConnection;

public class HttpServerConnectionEntry {
	public long createtime;
	public long timeoutInterval;
	public long timeout;
	public HttpServerConnection connection;
	
	public HttpServerConnectionEntry(HttpServerConnection conn,
			long createtime, long timeoutInterval) {
		this.connection = conn;
		this.createtime = createtime;
		this.timeoutInterval = timeoutInterval;
		this.timeout = this.createtime + this.timeoutInterval;
	}

	public boolean isTimeout() {
		return System.currentTimeMillis() > this.timeout;
	}
	
	public void shutdown() {
		try {
			connection.shutdown();
		} catch (IOException e) {
		}
	}
}
