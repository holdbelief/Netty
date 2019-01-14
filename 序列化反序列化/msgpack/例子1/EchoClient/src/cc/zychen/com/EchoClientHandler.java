package cc.zychen.com;

import java.util.List;

import org.msgpack.MessagePack;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class EchoClientHandler extends ChannelInboundHandlerAdapter {

	private int counter = 0;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		@SuppressWarnings("unchecked")
		List<UserInfo> u = (List<UserInfo>) msg;
		System.out.println(u);

		UserInfo t = MessagePack.unpack(MessagePack.pack(msg), UserInfo.class);
		System.out.println("this is "+ ++ counter +" times receive server:name[" + t.getName() +"]" +" age["+ t.getAge() +"]");
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		UserInfo usInfo = null;
		for(int n=0;n<10;n++){
			usInfo = new UserInfo();
			usInfo.setName("aaaaaaa");
			usInfo.setAge(88);
			ctx.write(usInfo);
		}
		ctx.flush();
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}
}
