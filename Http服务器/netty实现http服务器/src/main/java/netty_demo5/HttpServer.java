package netty_demo5;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

public class HttpServer {
	
	public static void start( final int port ) throws Exception {
		EventLoopGroup boss = new NioEventLoopGroup();
		EventLoopGroup worker = new NioEventLoopGroup();
		ServerBootstrap serverBootStrap = new ServerBootstrap();
		
		try {
			serverBootStrap.channel(NioServerSocketChannel.class)
				.group(boss, worker)
				.childOption(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.SO_BACKLOG, 1024)
				.childHandler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast("http-decoder", new HttpServerCodec());
						ch.pipeline().addLast(new HttpServerHandler());
					}
				});
			
			ChannelFuture future = serverBootStrap.bind(port).sync();
			future.channel().closeFuture().sync();
		} catch ( Exception e ) {
			e.printStackTrace();
		} finally {
			boss.shutdownGracefully();
			worker.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) throws Exception {
		start(8080);
	}

}
