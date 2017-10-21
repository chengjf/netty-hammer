package com.chengjf.netty.write;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.TimeUnit;

/**
 * Created by jeff on 2017/10/21.
 */
public class Client {
    private static final int PORT = 6666;


    public static void main(String[] args) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();

        // init event loop group
        EventLoopGroup workGroup = new NioEventLoopGroup(4);
        bootstrap.group(workGroup);
        // init channel
        bootstrap.channel(NioSocketChannel.class);
        // init option
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true);
        // init handler
        bootstrap.handler(new ChannelInitializer() {
            protected void initChannel(Channel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));

                pipeline.addLast("outbound channel-1", new ChannelOutboundHandlerAdapter() {
                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                        System.out.println("1-send message");
                        super.write(ctx, msg, promise);
                    }

                    @Override
                    public void flush(ChannelHandlerContext ctx) throws Exception {
                        System.out.println("1-flush message");
                        super.flush(ctx);
                    }
                });

                pipeline.addLast("inbound channel-1", new SimpleChannelInboundHandler<ByteBuf>() {
                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
                        System.out.println("1-Received message from server, message is " + byteBuf.toString(CharsetUtil.UTF_8));
//                        channelHandlerContext.fireChannelRead(byteBuf.retain());
                    }

                    @Override
                    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
                        System.out.println("Connection is active");
                        ctx.executor().scheduleWithFixedDelay(
                                new HelloTask(ctx, "ChannelHandlerContext msg"), 0, 5000, TimeUnit.MILLISECONDS);
                    }
                });
                pipeline.addLast("inbound channel-2", new SimpleChannelInboundHandler<ByteBuf>() {
                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
                        System.out.println("2-Received message from server, message is " + byteBuf.toString(CharsetUtil.UTF_8));
                    }
                });
                pipeline.addLast("outbound channel-2", new ChannelOutboundHandlerAdapter() {
                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                        System.out.println("2-send message");
                        super.write(ctx, msg, promise);
                    }

                    @Override
                    public void flush(ChannelHandlerContext ctx) throws Exception {
                        System.out.println("2-flush message");
                        super.flush(ctx);
                    }
                });

            }
        });

        // connect
        System.out.println("Connect to server at " + PORT);
        Channel channel = bootstrap.connect("localhost", PORT).sync().channel();

        channel.eventLoop().scheduleWithFixedDelay(new WorldTask(channel, "Channel msg"), 0 ,5000, TimeUnit.MILLISECONDS);
    }

    static class HelloTask implements Runnable {

        ChannelHandlerContext ctx;
        String msg;

        HelloTask(ChannelHandlerContext ctx, String msg) {
            this.ctx = ctx;
            this.msg = msg;
        }

        public void run() {
            System.out.println("Send to server with message " + msg);
            this.ctx.writeAndFlush(Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
        }
    }
    static class WorldTask implements Runnable {

        Channel channel;
        String msg;

        WorldTask(Channel channel, String msg) {
            this.channel = channel;
            this.msg = msg;
        }

        public void run() {
            System.out.println("Send to server with message " + msg);
            this.channel.writeAndFlush(Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
        }
    }
}
