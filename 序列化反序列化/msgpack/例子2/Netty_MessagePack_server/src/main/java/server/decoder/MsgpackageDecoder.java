package server.decoder;

import java.util.List;

import org.msgpack.MessagePack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class MsgpackageDecoder extends MessageToMessageDecoder<ByteBuf> {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
		final int length = msg.readableBytes();
		byte[] b = new byte[length];
		msg.getBytes(msg.readerIndex(), b,0,length);
		MessagePack msgpack = new MessagePack();
		out.add(msgpack.read(b));
	}
}
