package cn.xpleaf.echo;

import cn.xpleaf.msgpack.MsgpackDecoder;
import cn.xpleaf.msgpack.MsgpackEncoder;
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

public class EchoClient {

	public void connect(String host, int port, final int sendNumber) throws Exception {
		// 配置客户端NIO线程
		EventLoopGroup group = new NioEventLoopGroup();
		
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)
			.option(ChannelOption.TCP_NODELAY, true)
			// 设置TCP连接超时时间
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
			.handler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					// 添加长度字段解码器
                    // 在MessagePack解码器之前增加LengthFieldBasedFrameDecoder，用于处理半包消息
                    // 它会解析消息头部的长度字段信息，这样后面的MsgpackDecoder接收到的永远是整包消息
                    ch.pipeline().addLast("frameDecoder", new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
					// 添加MessagePack解码器
					ch.pipeline().addLast("msgpack decoder", new MsgpackDecoder());
					// 添加长度字段编码器
                    // 在MessagePack编码器之前增加LengthFieldPrepender，它将在ByteBuf之前增加2个字节的消息长度字段
                    ch.pipeline().addLast("frameEncoder", new LengthFieldPrepender(2));
					// 添加MessagePack编码器
					ch.pipeline().addLast("msgpack encoder", new MsgpackEncoder());
					// 添加业务处理的Handler
					ch.pipeline().addLast(new EchoClientHandler(sendNumber));
				}
			});
			
			 // 发起异步连接操作
            ChannelFuture f = b.connect(host, port).sync();

            // 等待客户端链路关闭
            f.channel().closeFuture().sync();
			
		} finally {
            // 优雅退出，释放NIO线程组
            group.shutdownGracefully();
        }
	}
	
	public static void main(String[] args) throws Exception {
		int port = 8080;
		
		if(args != null && args.length > 0) {
            try {
                port = Integer.valueOf(port);
            } catch (NumberFormatException e) {
                // 采用默认值
            }
        }
        int sendNumber = 1000;
        new EchoClient().connect("localhost", port, sendNumber);
	}
}
