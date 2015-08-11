package com.fd.basichttpserver.impl;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import javax.net.ssl.SSLServerSocketFactory;

import org.apache.http.HttpConnectionFactory;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.protocol.HttpService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.fd.basichttpserver.BasicReqListener;
import com.fd.basichttpserver.BasicWorker;
import com.fd.basichttpserver.HttpServerConnectionEntry;
import com.fd.basichttpserver.HttpServerConnectionPool;

public class ReqListener extends BasicReqListener {
	private final static Logger logger = LogManager
			.getLogger(ReqListener.class);
	private final HttpConnectionFactory<DefaultBHttpServerConnection> connFactory;
	private final ServerSocket serversocket;
	private final HttpService httpService;
	private final ThreadPoolExecutor threadPool;
	private final HttpServerConnectionPool connPool;
	
	public ReqListener(final int port, final HttpService httpService,
			final SSLServerSocketFactory sf, final ThreadPoolExecutor threadPool,
			final HttpServerConnectionPool connPool)
			throws IOException {
		this.connFactory = DefaultBHttpServerConnectionFactory.INSTANCE;
		this.serversocket = sf != null ? sf.createServerSocket(port)
				: new ServerSocket(port);
		this.httpService = httpService;
		this.threadPool = threadPool;
		this.connPool = connPool;
	}

	@Override
	public void doListen() {
		logger.info("Listening on port " + this.serversocket.getLocalPort());
		while (!Thread.interrupted()) {
			HttpServerConnection conn = null;
			HttpServerConnectionEntry  connEntry = null;
			try {
				Socket socket = this.serversocket.accept();
				logger.info("Incoming connection from "
						+ socket.getInetAddress());
				conn = connFactory
						.createConnection(socket);
				conn.setSocketTimeout(10000);
				connEntry = new HttpServerConnectionEntry(conn,
						System.currentTimeMillis(), 60000);
				connPool.addConnectionEntry(connEntry);
				BasicWorker t = new ReqProcessor(this.httpService, connEntry, connPool);
				threadPool.submit(t);
				logger.info("request has been submitted to request processor thread pool");
			} catch (RejectedExecutionException ex) {
				logger.warn("reject submit:", ex);
				connPool.release(connEntry);
			} catch (InterruptedIOException ex) {
				break;
			} catch (IOException e) {
				logger.warn("I/O error initialising connection thread: "
						+ e.getMessage());
				break;
			}
		}
		logger.info("request listener stops");
	}

}
