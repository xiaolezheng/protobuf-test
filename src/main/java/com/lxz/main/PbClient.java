package com.lxz.main;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lxz.protobuf.MessageRequest;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * Created by xiaolezheng on 16/4/22.
 */
public class PbClient {
    private static final Logger logger = LoggerFactory.getLogger(PbClient.class);

    private static final int PORT = 9090;
    private static final String HOST = "127.0.0.1";

    public static void main(String[] args) throws Exception {
        // ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        sendMessageNetty();

    }

    private static void sendMessageNetty() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();

                    // pipeline.addLast("encoder", new StringEncoder());
                    pipeline.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
                    pipeline.addLast("protobufDecoder",
                            new ProtobufDecoder(MessageRequest.Message.getDefaultInstance()));
                    pipeline.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
                    pipeline.addLast("protobufEncoder", new ProtobufEncoder());
                    pipeline.addLast("handler", new MessageHandler());
                }
            });

            // Start the client.


            ChannelFuture f = b.connect(HOST, PORT).sync(); // (5)
            Channel channel = f.channel();

            for (int i = 0; i < 1000000; i++) {
                MessageRequest.Message message  = MessageRequest.Message.newBuilder().setType(MessageRequest.Message.Type.FOO)
                            .setFoo(MessageRequest.Foo.newBuilder().setId(i).setName("foo").build()).build();

                MessageRequest.Message message1  = MessageRequest.Message.newBuilder().setType(MessageRequest.Message.Type.BAR)
                            .setBar(MessageRequest.Bar.newBuilder().setId(i).setName("bar").build()).build();

                MessageRequest.Message message2  = MessageRequest.Message.newBuilder().setType(MessageRequest.Message.Type.BAZ)
                            .setBaz(MessageRequest.Baz.newBuilder().setId(i).setName("baz").build()).build();

                channel.write(message);
                channel.write(message1);
                channel.write(message2);

                if(i % 10 == 0) {
                    channel.flush();
                }
            }

            channel.flush();

            // Wait until the connection is closed.
            channel.closeFuture().sync();

        } catch (Exception e) {
            logger.error("", e);
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    private static class MessageHandler extends ChannelHandlerAdapter {

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            logger.error("", cause);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

            logger.info("response: {}", msg);
        }

    }
}
