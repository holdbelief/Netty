package com.phei.netty.protocol.http.fileServer;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.LOCATION;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpUtil.isKeepAlive;
import static io.netty.handler.codec.http.HttpUtil.setContentLength;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	
	private final String url;
	private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
	
	public HttpFileServerHandler(String url) {
		this.url = url;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		/*
		 * 首先对HTTP请求消息的解码结果进行判断
		 * 如果解码失败，直接构造HTTP 400错误返回
		 */
		if ( !request.decoderResult().isSuccess() ) {
			sendError(ctx, BAD_REQUEST);
			return;
		}
		
		/*
		 * 对请求方法进行判断，如果不是从浏览器或者表单设置为GET发起的请求
		 * 则构造HTTP 405错误返回
		 */
		if ( request.method() != GET ) {
			sendError(ctx, METHOD_NOT_ALLOWED);
			return;
		}
		
		final String uri = request.uri();
		final String path = sanitizeUri(uri);
		
		// 如果构建的URI不合法，则返回HTTP 403错误
		if ( path == null ) {
			sendError(ctx, FORBIDDEN);
			return;
		}
		
		// 使用新组装的文件路径构建File对象
		File file = new File(path);
		
		// 如果文件不存在或者文件是系统的隐藏文件，则返回HTTP 404错误
		if ( file.isHidden() || !file.exists() ) {
			sendError(ctx, NOT_FOUND);
			return;
		}
		
		// 如果文件是目录，则发送目录的链接给客户端浏览器
		if ( file.isDirectory() ) {
			if ( uri.endsWith("/") ) {
				sendListing(ctx, file);
			} else {
				sendRedirect(ctx, uri + "/");
			}
			return;
		}
		if ( !file.isFile() ) {
			sendError(ctx, FORBIDDEN);
			return;
		}
		
		RandomAccessFile randomAccessFile = null;
		try {
			randomAccessFile = new RandomAccessFile(file, "r");
		} catch ( FileNotFoundException fnfe ) {
			sendError(ctx, NOT_FOUND);
			return;
		}
		
		// 获取文件长度
		long fileLength = randomAccessFile.length();
		// 构建Http 应答消息
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
		// 在消息头中设置content length
		setContentLength(response, fileLength);
		// 在消息头中设置content type
		setContentTypeHeader(response, file);
		// 判断request是否是Keep-Alive，如果是，则在应答消息头中设置Connection为Keep-Alive
		if ( isKeepAlive(request) ) {
			response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		}
		// 发送应答消息
		ctx.write(response);
		// 通过Netty的ChunkedFile对象直接将文件写入到发送缓冲区中。
		// 最后为sendFileFuture增加GenericFuntureListener，如果发送完成，打印Transfer complete
		ChannelFuture sendFileFuture;
		sendFileFuture = ctx.write(new ChunkedFile(randomAccessFile, 0, fileLength, 8192), ctx.newProgressivePromise());
		sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
			
			public void operationComplete(ChannelProgressiveFuture future) throws Exception {
				System.out.println("Transfer complete.");
			}
			
			public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception {
				if ( total < 0 ) {
					System.err.println("Transfer progress: " + progress);
				} else {
					System.err.println("Transfer progress: " + progress + " / " + total);
				}
			}
		});
		
		// 如果使用chunked编码，最后需要发送一个编码结束的空消息体，将LastHttpContent的EMPTY_LAST_CONTENT发送到缓冲区，标识所有的消息体已经发送完成
		// 同时调用flush方法将之前在发送缓冲区的消息刷新到SocketChannel中发送给对方 
		ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
		
		// 如果是费Keey-Alive的，最后一包消息发送完成之后，服务器要主动关闭链接。
		if (!isKeepAlive(request)) {
		    lastContentFuture.addListener(ChannelFutureListener.CLOSE);
		}
	}
	
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		if (ctx.channel().isActive()) {
		    sendError(ctx, INTERNAL_SERVER_ERROR);
		}
    }
	
	/**
	 * 对请求的URL进行包装
	 * @param uri
	 * @return
	 */
	private String sanitizeUri(String uri) {
		try {
			// 使用JDK的java.net.URLDecoder对URL进行解码，只用UTF-8字符集
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch ( UnsupportedEncodingException e ) {
			throw new Error();
		}
		if ( !uri.startsWith(url) ) {
			return null;
		}
		if (!uri.startsWith("/")) {
		    return null;
		}
		
		// 将硬编码的文件路径分隔符替换为本地操作系统的文件路径分隔符
		uri = uri.replace('/', File.separatorChar);
		if (uri.contains(File.separator + '.')
				|| uri.contains('.' + File.separator) || uri.startsWith(".")
				|| uri.endsWith(".") || INSECURE_URI.matcher(uri).matches()) {
			    return null;
		}
		
		return System.getProperty("user.dir") + File.separator + uri;
	}
	
	private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");
	
	/**
	 * 返回文件目录的列表以及列表链接给客户端
	 * @param ctx
	 * @param dir
	 */
	private static void sendListing(ChannelHandlerContext ctx, File dir) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
		response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
		StringBuilder buf = new StringBuilder();
		String dirPath = dir.getPath();
		buf.append("<!DOCTYPE html>\r\n");
		buf.append("<html><head><title>");
		buf.append(dirPath);
		buf.append(" 目录：");
		buf.append("</title></head><body>\r\n");
		buf.append("<h3>");
		buf.append(dirPath).append(" 目录：");
		buf.append("</h3>\r\n");
		buf.append("<ul>");
		buf.append("<li>链接：<a href=\"../\">..</a></li>\r\n");
		
		for ( File f : dir.listFiles() ) {
			if ( f.isHidden() || !f.canRead() ) {
				continue;
			}
			String name = f.getName();
			if ( !ALLOWED_FILE_NAME.matcher(name).matches() ) {
				continue;
			}
			buf.append("<li>链接：<a href=\"");
		    buf.append(name);
		    buf.append("\">");
		    buf.append(name);
		    buf.append("</a></li>\r\n");
		}
		
		buf.append("</ul></body></html>\r\n");
		ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
		response.content().writeBytes(buffer);
		buffer.release();
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}
	
	private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND);
		response.headers().set(LOCATION, newUri);
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}
	
	private static void sendError( ChannelHandlerContext ctx, HttpResponseStatus status ) {
		FullHttpResponse response 
			= new DefaultFullHttpResponse(HTTP_1_1, status, 
					Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
		
		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	private static void setContentTypeHeader( HttpResponse response, File file ) {
		MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		response.headers().set(CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));
	}

}
