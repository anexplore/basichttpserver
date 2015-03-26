package com.fd.basichttpserver.impl;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
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

public class ReqListener extends BasicReqListener {
	private final static Logger logger = LogManager
			.getLogger(ReqListener.class);
	private final HttpConnectionFactory<DefaultBHttpServerConnection> connFactory;
	private final ServerSocket serversocket;
	private final HttpService httpService;
	private final ThreadPoolExecutor threadPool;

	public ReqListener(final int port, final HttpService httpService,
			final SSLServerSocketFactory sf, final ThreadPoolExecutor threadPool)
			throws IOException {
		this.connFactory = DefaultBHttpServerConnectionFactory.INSTANCE;
		this.serversocket = sf != null ? sf.createServerSocket(port)
				: new ServerSocket(port);
		this.httpService = httpService;
		this.threadPool = threadPool;
	}

	@Override
	public void doListen() {
		logger.info("Listening on port " + this.serversocket.getLocalPort());
		while (!Thread.interrupted()) {
			try {
				// Set up HTTP connection
				Socket socket = this.serversocket.accept();
				logger.info("Incoming connection from "
						+ socket.getInetAddress());
				HttpServerConnection conn = this.connFactory
						.createConnection(socket);
				// Start worker thread
				BasicWorker t = new ReqProcessor(this.httpService, conn);
				this.threadPool.submit(t);
			} catch (InterruptedIOException ex) {
				break;
			} catch (IOException e) {
				logger.warn("I/O error initialising connection thread: "
						+ e.getMessage());
				break;
			}
		}

	}

}
