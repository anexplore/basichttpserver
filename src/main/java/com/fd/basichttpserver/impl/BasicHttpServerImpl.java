package com.fd.basichttpserver.impl;

import java.net.URL;
import java.security.KeyStore;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.fd.basichttpserver.BasicHttpServer;
import com.fd.basichttpserver.HttpReqHandler;

public class BasicHttpServerImpl extends BasicHttpServer {
	private final static Logger logger = LogManager
			.getLogger(BasicHttpServerImpl.class);
	private int port = 8080;
	private int maxWorkThread = 100;
	private final ThreadPoolExecutor threadPool;
	private volatile boolean inited = false;
	private HttpService httpService = null;
	private SSLServerSocketFactory sf = null;
	private Thread listenThread;
	private UriHttpRequestHandlerMapper reqistry;

	public BasicHttpServerImpl(int port, int maxWorkThread, int coreSize,
			long keepAliveTime, int maxBlockingThreadNum) {
		this.port = port;
		this.maxWorkThread = maxWorkThread;
		threadPool = new ThreadPoolExecutor(coreSize, this.maxWorkThread,
				keepAliveTime, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(maxBlockingThreadNum));
		threadPool
				.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
	}

	@Override
	public boolean init() throws Exception {
		// Set up the HTTP protocol processor
		HttpProcessor httpproc = HttpProcessorBuilder.create()
				.add(new ResponseDate())
				.add(new ResponseServer("FDSERVER/1.1"))
				.add(new ResponseContent()).add(new ResponseConnControl())
				.build();

		// Set up request handlers
		reqistry = new UriHttpRequestHandlerMapper();
		reqistry.register("*", new ReqHandler());

		// Set up the HTTP service
		httpService = new HttpService(httpproc, reqistry);
		if (port == 443) {
			// Initialize SSL context
			ClassLoader cl = BasicHttpServerImpl.class.getClassLoader();
			URL url = cl.getResource("my.keystore");
			if (url == null) {
				logger.fatal("Keystore not found");
				System.exit(1);
			}
			KeyStore keystore = KeyStore.getInstance("jks");
			keystore.load(url.openStream(), "secret".toCharArray());
			KeyManagerFactory kmfactory = KeyManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmfactory.init(keystore, "secret".toCharArray());
			KeyManager[] keymanagers = kmfactory.getKeyManagers();
			SSLContext sslcontext = SSLContext.getInstance("TLS");
			sslcontext.init(keymanagers, null, null);
			sf = sslcontext.getServerSocketFactory();
		}
		inited = true;
		return inited;
	}

	@Override
	public boolean start() throws Exception {
		if (!inited) {
			logger.fatal("httpserver has not been inited");
			return false;
		}
		listenThread = new Thread(new ReqListener(port, httpService, sf,
				threadPool));
		listenThread.setDaemon(false);
		listenThread.start();
		return true;
	}

	public void register(String path, Class<? extends HttpReqHandler> c) {
		if (path != null && c != null)
			try {
				this.reqistry.register(path, c.newInstance());
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
	}

	@Override
	public void stop() {
		listenThread.interrupt();
		threadPool.shutdown();
	}

	public static void main(String[] args) throws Exception {
		String usage = "BasicHttpServerImpl listenPort maxWorkThread";
		CommandLineParser cmdParser = new PosixParser();
		Options options = new Options();
		options.addOption("h", "help", false, "usage");
		options.addOption("threadPoolCoreSize", true,
				"threadPoolCoreSize,default 50");
		options.addOption("threadKeepAliveTime", true,
				"thread keep alive time,default 1000ms");
		options.addOption("maxBlockingThreadNum", true,
				"max blocking thread number");
		CommandLine cmd = cmdParser.parse(options, args);
		String[] rArgs = cmd.getArgs();
		if (rArgs.length != 2) {
			HelpFormatter helpFormat = new HelpFormatter();
			helpFormat.printHelp(usage, options);
			return;
		}
		int port = Integer.valueOf(rArgs[0]);
		int maxWorkThread = Integer.valueOf(rArgs[1]);
		int threadPoolCoreSize = 50;
		long threadKeepAliveTime = 1000;
		int maxBlockingThreadNum = 100;
		if (cmd.hasOption("threadPoolCoreSize")) {
			threadPoolCoreSize = Integer.valueOf(cmd
					.getOptionValue("threadPoolCoreSize"));
		}
		if (cmd.hasOption("threadKeepAliveTime")) {
			threadKeepAliveTime = Long.valueOf(cmd
					.getOptionValue("threadKeepAliveTime"));
		}
		if (cmd.hasOption("maxBlockingThreadNum")) {
			threadKeepAliveTime = Integer.valueOf(cmd
					.getOptionValue("maxBlockingThreadNum"));
		}
		BasicHttpServerImpl serverImpl = new BasicHttpServerImpl(port,
				maxWorkThread, threadPoolCoreSize, threadKeepAliveTime,
				maxBlockingThreadNum);
		serverImpl.init();
		serverImpl.start();
	}

}
