package server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class TimeServerHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			// 直接输出msg
			System.out.println(msg.toString());
			String remsg = new String("has receive");
			ctx.writeAndFlush(remsg);
		} catch ( Exception e ) {
			e.printStackTrace();
		} finally {
		}
	}

	@Override
	public void channelReadComplete( ChannelHandlerContext ctx ) throws Exception {
		ctx.flush();
	}
}
