package cn.xpleaf.msgpack;

import org.msgpack.MessagePack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * MsgpackEncoder继承自Netty中的MessageToByteEncoder类，
 * 并重写抽象方法encode(ChannelHandlerContext ctx, Object msg, ByteBuf out)
 * 它负责将Object类型的POJO对象编码为byte数组，然后写入到ByteBuf中
 * @author yeyonghao
 *
 */
public class MsgpackEncoder extends MessageToByteEncoder<Object> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		// 创建MessagePack对象
		MessagePack msgpack = new MessagePack();
		// 将对象编码为MessagePack格式的字节数组
		byte[] raw = msgpack.write(msg);
		// 将字节数组写入到ByteBuf中
		out.writeBytes(raw);
	}

}
