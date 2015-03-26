package com.fd.basichttpserver;

/**
 * 处理线程基类
 * 
 * @author caoly
 *
 */
public abstract class BasicReqProcessor implements BasicWorker {
	public abstract void doProcess();

	public void run() {
		doProcess();
	}

}
