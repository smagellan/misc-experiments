package smagellan.test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.incubator.channel.uring.IOUringEventLoopGroup;
import io.netty.incubator.channel.uring.IOUringServerSocketChannel;

public class IOUringNettyMain {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup parentGroup = new IOUringEventLoopGroup(1);
        EventLoopGroup childGroup  = new IOUringEventLoopGroup();
        try {
            final ChannelHandler serverHandler = new HttpServerCodec();

            ServerBootstrap b = new ServerBootstrap();
            b.group(parentGroup, childGroup)
                    .channel(IOUringServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(serverHandler);
                        }
                    });

            // Start the server.
            ChannelFuture f = b.bind(8081).sync();

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
        }
    }
}
