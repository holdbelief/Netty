package cc.zychen.com;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public class EchoClient {

	public void connect(String ip, int port) throws InterruptedException {
		EventLoopGroup group = new NioEventLoopGroup();
		
		try {
			Bootstrap b = new Bootstrap();
			b.group(group)
			.channel(NioSocketChannel.class)
			.handler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(65535,0,4,0,4));
					ch.pipeline().addLast(new MsgPackDecoder());
					ch.pipeline().addLast(new LengthFieldPrepender(4,false));
					ch.pipeline().addLast(new MsgPackEncoder());
					ch.pipeline().addLast(new EchoClientHandler());
				}
			});
			
			ChannelFuture f = b.connect(ip, port).sync();
			f.channel().close().sync();
		} finally {
			group.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) {
		int port = 8080;
		try {
			if (args != null && args.length > 0) {
				port = Integer.parseInt(args[0]);
			}
			new EchoClient().connect("127.0.0.1", port);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
