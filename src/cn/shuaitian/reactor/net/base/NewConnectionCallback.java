package cn.shuaitian.reactor.net.base;

import java.nio.channels.SocketChannel;

public interface NewConnectionCallback {
	public void callback(SocketChannel socketChannel);
}
