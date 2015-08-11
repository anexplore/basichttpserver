package com.fd.basichttpserver;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

public class HttpServerConnectionPool {
	private final ReentrantLock lock;
	private LinkedList<HttpServerConnectionEntry> pool;
	private Thread timeoutProcessThread;
	private volatile boolean start;
	
	public HttpServerConnectionPool() {
		lock = new ReentrantLock();
		pool = new LinkedList<>();
	}

	public void start() {
		timeoutProcessThread = new TimeoutThread();
		timeoutProcessThread.start();
		start = true;
	}
	
	public void shutdown() {
		timeoutProcessThread.interrupt();
		releaseAll();
		start = false;
	}
	
	public void addConnectionEntry (HttpServerConnectionEntry connEntry) {
		if (!start || connEntry == null) {
			return;
		}
		lock.lock();
		try {
			pool.addLast(connEntry);
		} finally {
			lock.unlock();
		}
	}
	
	public void releaseAll() {
		if (!start) {
			return;
		}
		lock.lock();
		try {
			while (!pool.isEmpty()) {
				pool.poll().shutdown();
			}
		} finally {
			lock.unlock();
		}
	}
	
	public void release(HttpServerConnectionEntry connEntry) {
		if (!start || connEntry == null) {
			return;
		}
		connEntry.shutdown();
		lock.lock();
		try {
			pool.remove(connEntry);
		} finally {
			lock.unlock();
		}
	}
	
	private void checkAndRemoveTimeoutEntry() throws InterruptedException {
		if (!start) {
			return;
		}
		lock.lockInterruptibly();
		try {
			HttpServerConnectionEntry entry = pool.peek();
			while (entry != null) {
				if (entry.isTimeout()) {
					entry.shutdown();
					pool.removeFirst();
					entry = pool.peek();
				} else {
					break;
				}
			}
		} finally {
			lock.unlock();
		}
	}
	
	public class TimeoutThread extends Thread {
		public void run() {
			waitFor(2000);
			try {
				checkAndRemoveTimeoutEntry();
			} catch (InterruptedException e) {
			}
		}
		
		public void waitFor(long interval) {
			try {
				sleep(interval);
			} catch (Exception e) {
			}
		}
	}
}
