package cn.shuaitian.reactor.net.base;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Poller {
	private EventLoop eventLoop;
	private Selector selector;
	public Poller(EventLoop eventLoop){
		this.eventLoop = eventLoop;
		try {
			selector = Selector.open();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	long poll(int timeoutMs,List<Channel> activeChannels){
		long returnTime = 0L;
		try {
			int nums = selector.select(timeoutMs);
			returnTime = System.currentTimeMillis();
			if(nums == 0)
				return returnTime;
			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			Iterator<SelectionKey> iter = selectedKeys.iterator();
			while(iter.hasNext()){
				SelectionKey key = iter.next();
				activeChannels.add((Channel) key.attachment());
				iter.remove();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return returnTime;
	}
	
	void assertInLoopThread(){
		eventLoop.assertInLoopThread();
	}

	public void updateChannel(Channel channel) {
		SelectableChannel jdkChannel = channel.getJDKChannel();
		try {
			SelectionKey key = jdkChannel.register(selector, channel.getEvents());
			key.attach(channel);
		} catch (ClosedChannelException e) {
			//TODO log this exception
			System.out.println("Regist channel failue because it has been closed");
		}
	}

	public Selector getSelector() {
		return this.selector;
	}
	
}
