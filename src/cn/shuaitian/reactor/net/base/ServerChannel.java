package cn.shuaitian.reactor.net.base;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public class ServerChannel extends Channel {
	private EventCallback acceptableCallback;
	private EventCallback connectCallback;
	public ServerChannel(EventLoop loop, SelectableChannel jdkChannel) {
		super(loop, jdkChannel);
	}
	
	public void setAcceptableCallback(EventCallback acceptableCallback){
		this.acceptableCallback = acceptableCallback;
	}
	
	
	public void setConnectCallback(EventCallback connectCallback) {
		this.connectCallback = connectCallback;
	}
	
	@Override
	public void handleEvent(){
		SelectionKey key = jdkChannel.keyFor(eventLoop.getSelector());
		if(key.isAcceptable()){
			acceptableCallback.handle(this);
		}
		else if(key.isConnectable()){
			connectCallback.handle(this);
		}
		else if(key.isReadable()){
			readCallback.handle(this);
		}
		else if(key.isWritable()){
			writeCallback.handle(this);
		}
		else{
			//TODO log unknow event type
		}
	}
	
	public void enableAccept(){
		events |= SelectionKey.OP_ACCEPT;
		update();
	}
	
	public void enableConnect(){
		events |= SelectionKey.OP_CONNECT;
		update();
	}

}
