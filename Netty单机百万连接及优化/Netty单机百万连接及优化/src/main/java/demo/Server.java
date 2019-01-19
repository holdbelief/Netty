package demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {
	private static final int BEGIN_PORT = 8000;
	private static final int N_PORT = 100;
	
	public static void main(String[] args) {
		new Server().start(BEGIN_PORT, N_PORT);
	}
	
	public void start( int beginPort, int nPort ) {
		System.out.println("server starting....");
		
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workerGroup);
		bootstrap.channel(NioServerSocketChannel.class);
		bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
		bootstrap.childHandler(new ConnectionCountHandler());
		
		for ( int i = 0; i < nPort; i++ ) {
			int port = beginPort + i;
			bootstrap.bind(port).addListener(future -> {
				System.out.println("bind success in port: " + port);
			});
		}
		
		System.out.println("server started!");
	}
}
