package cc.zychen.com;

import java.util.List;

import org.msgpack.MessagePack;
import org.msgpack.template.Templates;
import org.msgpack.type.Value;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class MsgPackDecoder extends MessageToMessageDecoder<ByteBuf> {
	
	@Override
	protected void decode( ChannelHandlerContext arg0, ByteBuf arg1, List<Object> arg2 ) throws Exception
	{
		final byte[] array;
		int length = arg1.readableBytes();
		array = new byte[length];
		arg1.getBytes(arg1.readerIndex(),array,0,length);
		MessagePack mpPack = new MessagePack();
		Value v = mpPack.read(array,Templates.TValue);
		arg2.add(v);
	}
}
