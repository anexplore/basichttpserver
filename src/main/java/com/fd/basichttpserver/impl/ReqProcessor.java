package com.fd.basichttpserver.impl;

import java.io.IOException;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpServerConnection;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fd.basichttpserver.BasicReqProcessor;

public class ReqProcessor extends BasicReqProcessor {
	private final static Logger logger  = LogManager.getLogger(ReqProcessor.class);
	
	private final HttpService httpservice;
    private final HttpServerConnection conn;

    public ReqProcessor(
            final HttpService httpservice,
            final HttpServerConnection conn) {
        super();
        this.httpservice = httpservice;
        this.conn = conn;
    }
    
	@Override
	public void doProcess() {
		 HttpContext context = new BasicHttpContext(null);
         try {
        	 //短连接
             if (!Thread.interrupted() && this.conn.isOpen()) {
                 this.httpservice.handleRequest(this.conn, context);
             }
         } catch (ConnectionClosedException ex) {
        	 logger.info("Client closed connection",ex);
         } catch (IOException ex) {
             logger.warn("I/O error: ",ex);
         } catch (HttpException ex) {
        	 logger.warn("Unrecoverable HTTP protocol violation:",ex);
         } finally {
             try {
                 this.conn.shutdown();
             } catch (IOException ignore) {}
         }

	}

}
