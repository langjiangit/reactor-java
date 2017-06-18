package cn.shuaitian.reactor.net.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import cn.shuaitian.reactor.net.base.Channel;
import cn.shuaitian.reactor.net.base.EventCallback;
import cn.shuaitian.reactor.net.base.EventLoop;
import cn.shuaitian.reactor.net.base.NewConnectionCallback;
import cn.shuaitian.reactor.net.base.ServerChannel;

public class Acceptor implements EventCallback{
	private NewConnectionCallback callback = null;
	private boolean listenning;
	private ServerChannel serverChannel;
	private EventLoop eventLoop;
	
	public Acceptor(EventLoop eventLoop,int port){
		this(eventLoop,new InetSocketAddress(port));
	}
	public Acceptor(EventLoop eventLoop,InetSocketAddress listenAddr){
		try {
			this.eventLoop = eventLoop;
			ServerSocketChannel ssc = ServerSocketChannel.open();
			ssc.configureBlocking(false);
			ssc.bind(listenAddr);
			serverChannel = new ServerChannel(eventLoop,ssc);
			serverChannel.setAcceptableCallback(this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setNewConnectionCallback(NewConnectionCallback callback){
		this.callback = callback;
	}
	
	public boolean isListenning(){
		return this.listenning;
	}
	
	public void listen(){
		eventLoop.assertInLoopThread();
		listenning = true;
		serverChannel.enableAccept();
	}

	@Override
	public void handle(Channel channel) {
		eventLoop.assertInLoopThread();
		try {
			SocketChannel sc = serverChannel.accept();
			if(this.callback != null){
				callback.callback(sc);
			}
		} catch (IOException e) {
			//TODO log this exception
			e.printStackTrace();
		}
	}
}
