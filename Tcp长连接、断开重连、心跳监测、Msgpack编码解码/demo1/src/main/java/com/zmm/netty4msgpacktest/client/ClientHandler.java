package com.zmm.netty4msgpacktest.client;

import com.zmm.netty4msgpacktest.common.CustomHeartbeatHandler;

import io.netty.channel.ChannelHandlerContext;

public class ClientHandler extends CustomHeartbeatHandler {

	
	public ClientHandler(String name) {
		super(name);
	}


	private Client client;
	
	
	@Override
	protected void handleData(ChannelHandlerContext channelHandlerContext, Object msg) {
		// TODO Auto-generated method stub
		
	}

}
