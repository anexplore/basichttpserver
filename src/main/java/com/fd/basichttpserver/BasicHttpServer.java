package com.fd.basichttpserver;
/**
 * 程序入口
 * @author caoly
 *
 */
public abstract class BasicHttpServer {
	public abstract boolean init() throws Exception;
	public abstract boolean start() throws Exception;
	public abstract void stop();
	
}
