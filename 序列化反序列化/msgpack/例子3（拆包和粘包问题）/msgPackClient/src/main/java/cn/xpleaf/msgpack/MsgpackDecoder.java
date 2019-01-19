package cn.xpleaf.msgpack;

import java.util.List;

import org.msgpack.MessagePack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * MsgpackDecoder继承自Netty中的MessageToMessageDecoder类，
 * 并重写抽象方法decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out)
 * 首先从数据报msg（数据类型取决于继承MessageToMessageDecoder时填写的泛型类型）中获取需要解码的byte数组
 * 然后调用MessagePack的read方法将其反序列化（解码）为Object对象
 * 将解码后的对象加入到解码列表out中，这样就完成了MessagePack的解码操作
 * @author yeyonghao
 *
 */
public class MsgpackDecoder extends MessageToMessageDecoder<ByteBuf> {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
		// 从数据报msg中（这里的数据类型为ByteBuf，因为Netty的通信基于ByteBuf对象）
		final byte[] array;
		final int length = msg.readableBytes();
		array = new byte[length];
		
		/*
         * 这里使用的是ByteBuf的getBytes方法来将ByteBuf对象转换为字节数组，前面是使用readBytes，直接传入一个接收的字节数组参数即可
         * 这里的参数比较多，第一个参数是index，关于readerIndex，说明如下：
         * ByteBuf是通过readerIndex跟writerIndex两个位置指针来协助缓冲区的读写操作的，具体原理等到Netty源码分析时再详细学习一下
         * 第二个参数是接收的字节数组
         * 第三个参数是dstIndex the first index of the destination
         * 第四个参数是length   the number of bytes to transfer
         */
		msg.getBytes(msg.readerIndex(), array, 0, length);
		
		//创建一个MessagePack对象
		MessagePack msgpack = new MessagePack();
		
		// 解码并添加到列表out中
		out.add(msgpack.read(array));
	}

}
