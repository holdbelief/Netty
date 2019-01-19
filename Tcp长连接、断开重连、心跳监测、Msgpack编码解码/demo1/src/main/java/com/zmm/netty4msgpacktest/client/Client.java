package com.zmm.netty4msgpacktest.client;

import com.zmm.netty4msgpacktest.code.MsgPackDecode;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class Client {

	private NioEventLoopGroup workGroup = new NioEventLoopGroup(4);
	private Channel channel;
	private Bootstrap bootstrap;
	
	public static void main(String[] args) {
		
	}
	
	public void start() {
		try {
			bootstrap = new Bootstrap();
			bootstrap.group(workGroup)
			.channel(NioSocketChannel.class)
			.handler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline p = ch.pipeline();
					p.addLast(new IdleStateHandler(0, 0, 5));
					p.addLast(new MsgPackDecode());
					
				}
			});
		} catch ( Exception e ) {
			throw new RuntimeException(e);
		}
	}
}
