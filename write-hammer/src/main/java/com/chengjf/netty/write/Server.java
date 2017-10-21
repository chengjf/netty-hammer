package com.chengjf.netty.write;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

/**
 * Created by jeff on 2017/10/21.
 */
public class Server {

    private static final int PORT = 6666;

    public static void main(String[] args) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        // init bootstrap

        EventLoopGroup bossGroup = new NioEventLoopGroup(4);
        EventLoopGroup workGroup = new NioEventLoopGroup(4);

        // init event loop group
        serverBootstrap.group(bossGroup, workGroup);
        // init channel
        serverBootstrap.channel(NioServerSocketChannel.class);
        // init option
        serverBootstrap.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true);
        // init child handler
        serverBootstrap.childHandler(new ChannelInitializer() {
            protected void initChannel(Channel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast(new LoggingHandler(LogLevel.WARN));
                pipeline.addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
                        String s = byteBuf.toString(CharsetUtil.UTF_8);
                        System.out.println("Received message from " + channelHandlerContext.channel().remoteAddress() + ", message is " + s);
                        channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer(s, CharsetUtil.UTF_8));
                    }
                });
            }
        });

        System.out.println("Server start at port " + PORT);
        serverBootstrap.bind(PORT);
    }
}
