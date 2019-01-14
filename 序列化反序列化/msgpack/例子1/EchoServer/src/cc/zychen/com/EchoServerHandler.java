package cc.zychen.com;

import java.util.List;

import org.msgpack.MessagePack;
import org.msgpack.type.Value;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class EchoServerHandler extends ChannelInboundHandlerAdapter {
	private int counter = 0;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		@SuppressWarnings("unchecked")
		List<Value> u = (List<Value>) msg;
		System.out.println(u);

		@SuppressWarnings("deprecation")
		UserInfo t = MessagePack.unpack(MessagePack.pack(msg), UserInfo.class);
		System.out.println("this is " + ++counter + " times receive client:name[" + t.getName() + "]" + " age["
				+ t.getAge() + "]");

		UserInfo usInfo = new UserInfo();
		usInfo.setName("bbbbbbbbbb");
		usInfo.setAge(89);
		ctx.writeAndFlush(usInfo);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}
