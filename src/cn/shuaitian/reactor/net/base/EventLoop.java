package cn.shuaitian.reactor.net.base;

import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * EventLoop事件循环，与线程绑定，每个IO线程对应一个EventLoop，每个EventLoop对应一个IO线程，绑定关系通过ThreadLocal实现。 
 * 该类是线程安全的
 * @author shuaitian
 *
 */
public class EventLoop {
	private static final int POLLTIMEMS = 500;
	private volatile boolean looping;
	private Thread thread;
	private volatile boolean quit;
	private static ThreadLocal<EventLoop> currentLoop = new ThreadLocal<>();
	private Poller poller;
	private List<Channel> activeChannels;
	private	List<Runnable> pendingTasks = new LinkedList<>();
	
	public EventLoop(){
		if(currentLoop.get() != null){
			throw new RuntimeException("Event loop in thread '"+Thread.currentThread().getName()+"' already exist!");
		}
		else{
			currentLoop.set(this);
		}
		looping = false;
		thread = Thread.currentThread();
		poller = new Poller(this);
		activeChannels = new ArrayList<>();
	}
	
	/**
	 * 事件循环入口。
	 * 通过assertInEventLoop保证只能在IO线程调用
	 * @author shuaitian
	 */
	public void loop(){
		assert(!looping);
		assertInLoopThread();
		looping = true;
		quit = false;
		//事件循环从这里开始
		while(!quit){
			activeChannels.clear();
			poller.poll(POLLTIMEMS, activeChannels);
			for(Channel chan : activeChannels){
				chan.handleEvent();
			}
			doPendingTasks();
		}
		
		looping = false;
	}

	public boolean isInLoopThread(){
		return thread.equals(Thread.currentThread());
	}
	
	/**
	 * assertInLoopThread函数断言当前线程为IO线程，否则会抛出异常！这一操作可以简化共享数据的同步锁开销。
	 * @author shuaitian
	 */
	public void assertInLoopThread(){
		if(!isInLoopThread()){
			abortNotInLoopThread();
		}
	}
	
	private void abortNotInLoopThread(){
		throw new RuntimeException("Not in Loop thread");
	}
	
	public static EventLoop getEventLoopOfCurrentThread(){
		return currentLoop.get();
	}
	
	/**
	 * 退出当前EventLoop
	 * 该操作是线程安全的，因为quit变量为volatile类型
	 * @author shuaitian
	 */
	public void quit(){
		quit = true;
	}
	
	public void updateChannel(Channel channel) {
		assertInLoopThread();
		poller.updateChannel(channel);
	}

	public Selector getSelector() {
		return poller.getSelector();
	}
	
	/**
	 * 如果为当前线程为IO线程，则直接调用，否则将任务放入队列。
	 * @author shuaitian
	 * @param runnable
	 */
	public void runInLoop(Runnable runnable){
		if(isInLoopThread()){
			runnable.run();
		}else{
			queueInLoop(runnable);
		}
	}
	
	/**
	 * 该方法需要同步，因为会被其他线程调用
	 * 添加任务到任务队列
	 * @author shuaitian
	 */
	private synchronized void queueInLoop(Runnable runnable) {
		pendingTasks.add(runnable);
		poller.wakeup();
	}
	

	/**
	 * IO线程处理当前任务队列的任务,这里先将pendingTask拷贝到本地，减小临界区。
	 * @author shuaitian
	 */
	private void doPendingTasks() {
		List<Runnable> temp = new LinkedList<>	();
		synchronized (this) {
			temp.addAll(pendingTasks);
			pendingTasks.clear();
		}
		for(Runnable runnable : temp){
			runnable.run();
		}
	}
}
