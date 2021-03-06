package client.handler;

import client.mode.UserInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println(msg);
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        //发送50个UserInfo给服务器，由于启用了粘包/拆包支持，所以这里连续发送多个也不会出现粘包的现象。
        for (int i = 0; i < 50; i++) {
            UserInfo userInfo = new UserInfo();
            userInfo.setAge(i + "year");
            userInfo.setUsername("senninha");
            ctx.write(userInfo);
        }
        ctx.flush();
        System.out.println("-----------------send over-----------------");
    }
	
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // TODO Auto-generated method stub
        System.out.println("error");
    }
}
