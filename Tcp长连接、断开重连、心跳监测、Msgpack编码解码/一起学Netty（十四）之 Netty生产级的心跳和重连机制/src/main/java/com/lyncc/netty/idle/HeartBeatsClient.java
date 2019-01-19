package com.lyncc.netty.idle;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;

public class HeartBeatsClient {
	
	protected final HashedWheelTimer timer = new HashedWheelTimer();
	private Bootstrap boot;
	private final ConnectorIdleStateTrigger idleStateTrigger = new ConnectorIdleStateTrigger();
	
	public void Connector(int port, String host) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		
		boot = new Bootstrap();
		boot.group(group).channel(NioSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO));
		
		final ConnectionWatchdog watchdog = new ConnectionWatchdog(boot, timer, port, host, true) {
			
			public ChannelHandler[] handlers() {
				return new ChannelHandler[] {
						this,
						new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS),
						idleStateTrigger,
						new StringDecoder(),
						new StringEncoder(),
						new HeartBeatClientHandler()
				};
			}
		};
	}

}
