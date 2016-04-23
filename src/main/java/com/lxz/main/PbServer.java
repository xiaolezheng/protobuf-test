package com.lxz.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lxz.protobuf.MessageRequest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * Created by xiaolezheng on 16/4/22.
 */
public class PbServer {
    private static final Logger logger = LoggerFactory.getLogger(PbServer.class);

    private static final int PORT = 9090;

    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(5); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup(5);

        try {

            ServerBootstrap bootstrap = new ServerBootstrap(); // (2)
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            // pipeline.addLast("encoder", new StringEncoder());
                            pipeline.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
                            pipeline.addLast("protobufDecoder",
                                    new ProtobufDecoder(MessageRequest.Message.getDefaultInstance()));
                            pipeline.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
                            pipeline.addLast("protobufEncoder", new ProtobufEncoder());
                            pipeline.addLast(new MessageServerHandler());
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128) // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            ChannelFuture f = bootstrap.bind(PORT).sync(); // (7)

            logger.info("protobuf server start ......");

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();

        } catch (Exception e) {
            logger.error("", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }

    private static class MessageServerHandler extends ChannelHandlerAdapter {
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);

            logger.error("", cause);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            MessageRequest.Message message = (MessageRequest.Message) msg;

            switch (message.getType()) {
            case FOO:
                MessageRequest.Foo foo = message.getFoo();
                logger.info("foo.info: {}", foo);

                ctx.channel().writeAndFlush(buildResponse(foo.getId()));
                break;
            case BAR:
                MessageRequest.Bar bar = message.getBar();
                logger.info("bar.info: {}", bar);

                ctx.channel().writeAndFlush(buildResponse(bar.getId()));
                break;
            case BAZ:
                MessageRequest.Baz baz = message.getBaz();
                logger.info("baz.info: {}", baz);

                ctx.channel().writeAndFlush(buildResponse(baz.getId()));
                break;

            default:
                logger.warn("不支持的类型");
                break;
            }
        }
    }

    private static MessageRequest.Message buildResponse(int msgId) {
        return MessageRequest.Message.newBuilder()
                .setType(MessageRequest.Message.Type.RESPONSE).setResponse(MessageRequest.Response.newBuilder()
                        .setStatus(MessageRequest.Response.Status.SUCCESS).setMsg("hello").setMsgId(msgId).build())
                .build();
    }

}
