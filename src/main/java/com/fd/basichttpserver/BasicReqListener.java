package com.fd.basichttpserver;

/**
 * 接收线程基类
 * 
 * @author caoly
 *
 */
public abstract class BasicReqListener implements BasicWorker {
	public abstract void doListen();

	public void run() {
		doListen();
	}

}
