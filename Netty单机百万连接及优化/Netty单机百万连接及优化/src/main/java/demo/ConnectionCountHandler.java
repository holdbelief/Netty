package demo;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Sharable
public class ConnectionCountHandler extends ChannelInboundHandlerAdapter {

	private AtomicInteger nConnection = new AtomicInteger();
	
	public ConnectionCountHandler() {
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				System.out.println("connections: " + nConnection.get());
			}
		}, 0, 2, TimeUnit.SECONDS);
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		this.nConnection.incrementAndGet();
	}
	
	@Override
	public void channelInactive( ChannelHandlerContext ctx ) {
		this.nConnection.decrementAndGet();
	}
}
