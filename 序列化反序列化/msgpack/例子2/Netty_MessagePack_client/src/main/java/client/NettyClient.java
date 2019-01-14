package client;

import java.net.UnknownHostException;

import client.decoder.MsgpackageDecoder;
import client.encoder.MsgpackEncoder;
import client.handler.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public class NettyClient {
	
	private void bind( int port, String host ) {
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap b = new Bootstrap();
		b.group(group).channel(NioSocketChannel.class)
		.option(ChannelOption.TCP_NODELAY, true)
		.handler(new ClientHandlerInit());
		
		try {
			ChannelFuture f = b.connect(host, port).sync();
			f.channel().closeFuture().sync();
		} catch ( Exception e ) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully();
		}
	}
	
	private class ClientHandlerInit extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			//这里设置通过增加包头表示报文长度来避免粘包
			ch.pipeline().addLast("frameDecoder",new LengthFieldBasedFrameDecoder(1024, 0, 2,0,2));
			//增加解码器
			ch.pipeline().addLast("msgpack decoder",new MsgpackageDecoder());
			//这里设置读取报文的包头长度来避免粘包
            ch.pipeline().addLast("frameEncoder",new LengthFieldPrepender(2));
            //增加编码器
            ch.pipeline().addLast("msgpack encoder",new MsgpackEncoder());
            ch.pipeline().addLast(new ClientHandler());
		}
		
	}
	
	public static void main(String[] args) throws UnknownHostException {
        NettyClient client = new NettyClient();
        client.bind(12580,"localhost");
    }
}


