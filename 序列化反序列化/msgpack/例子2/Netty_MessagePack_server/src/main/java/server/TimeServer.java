package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import server.decoder.MsgpackageDecoder;
import server.encoder.MsgpackEncoder;
import server.handler.TimeServerHandler;

public class TimeServer {
	public void bind(int port) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workGroup = new NioEventLoopGroup();
		
		ServerBootstrap bootStrap = new ServerBootstrap();
		bootStrap.group(bossGroup, workGroup)
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, 1024)
			.childHandler(new ChildChannelHandler());
		
		try {
			ChannelFuture future = bootStrap.bind(port).sync();
			future.channel().closeFuture().sync();
		} catch ( Exception e ) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workGroup.shutdownGracefully();
		}
	}
	
	private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast("frameDecoder",new LengthFieldBasedFrameDecoder(1024, 0, 2,0,2));
            ch.pipeline().addLast("msgpack decoder",new MsgpackageDecoder());
            ch.pipeline().addLast("frameEncoder",new LengthFieldPrepender(2));
            ch.pipeline().addLast("msgpack encoder",new MsgpackEncoder());
            ch.pipeline().addLast(new TimeServerHandler());
        }
	}
	
	public static void main(String[] args) {
		int port = 12580;
		try {
			new TimeServer().bind(port);
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
}


