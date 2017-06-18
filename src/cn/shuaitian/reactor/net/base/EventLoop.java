package cn.shuaitian.reactor.net.base;

import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;

/**
 * EventLoop事件循环，与线程绑定，每个IO线程对应一个EventLoop，每个EventLoop对应一个IO线程，绑定关系通过ThreadLocal实现。
 * 因为EventLoop所有对外接口都会assertInEventLoop，也就是所有操作只能在IO线程实现，所以该类是线程安全的！
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
		}
		
		looping = false;
	}
	
	public boolean isInLoopThread(){
		return thread.equals(Thread.currentThread());
	}
	
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
}
