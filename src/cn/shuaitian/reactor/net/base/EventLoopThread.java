package cn.shuaitian.reactor.net.base;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * EventLoopThread，绑定EventLoop的线程类
 * @author shuaitian
 *
 */
public class EventLoopThread implements Runnable{
	private Thread thread;
	private EventLoop eventLoop;
	private Lock lock = new ReentrantLock();
	private Condition cond = lock.newCondition();
	public EventLoopThread(){
		thread = new Thread(this);
	}
	
	public void run(){
		eventLoop = new EventLoop();
		try{
			lock.lock();
			cond.signal();
		}finally{
			lock.unlock();
		}
		eventLoop.loop();
	}
	
	/**
	 * 用condtion变量确保返回的eventLoop已经构造完成
	 * @return
	 */
	public EventLoop startLoop(){
		thread.start();	
		try {
			lock.lock();
			while(eventLoop == null)
				cond.await();
		} catch (InterruptedException e) {
			if(eventLoop == null)
				throw new RuntimeException("Waiting for eventloop ready has been interrupted,start loop failed");
		}
		finally{
			lock.unlock();
		}
		return eventLoop;
	}
	
	public void join() throws InterruptedException{
		thread.join();
	}
}
