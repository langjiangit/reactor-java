package cn.shuaitian.reactor.net.base;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public class Channel {
	protected EventLoop eventLoop;
	protected EventCallback readCallback;
	protected EventCallback writeCallback;
	protected EventCallback errorCallback;
	protected SelectableChannel jdkChannel;
	protected int events;							//该channel所关心的事件
	
	public Channel(EventLoop loop,SelectableChannel jdkChannel){
		this.eventLoop = loop;
		this.jdkChannel = jdkChannel;
		events = 0;   	
	}
	
	public void handleEvent(){
		SelectionKey key = jdkChannel.keyFor(eventLoop.getSelector());
		if(key.isReadable()){
			readCallback.handle(this);
		}
		else if(key.isWritable()){
			writeCallback.handle(this);
		}
		else{
			//TODO log unknow event type
		}
	}

	public void setReadCallback(EventCallback readCallback) {
		this.readCallback = readCallback;
	}

	public void setWriteCallback(EventCallback writeCallback) {
		this.writeCallback = writeCallback;
	}

	public void setErrorCallback(EventCallback errorCallback) {
		this.errorCallback = errorCallback;
	}
	

	public EventLoop ownerLoop(){
		return this.eventLoop;
	}
	
	public void enableReading(){
		events |= SelectionKey.OP_READ;
		update();
	}
	
	public void enableWriting(){
		events |= SelectionKey.OP_WRITE;
		update();
	}
	
	public void disableAll(){
		events = 0;
		update();
	}
	
	public void update(){
		eventLoop.updateChannel(this);
	}
	
	public SelectableChannel getJDKChannel(){
		return this.jdkChannel;
	}

	public int getEvents() {
		return this.events;
	}
}
