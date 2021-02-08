package io.vertx.core.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.incubator.channel.uring.*;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.net.ClientOptionsBase;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.impl.transport.Transport;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;

class IOURingTransport extends Transport {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(IOURingTransport.class);

    private static volatile int pendingFastOpenRequestsThreshold = 256;

    public IOURingTransport() {
        logger.info("creating IOURingTransport");
    }

    /**
     * Return the number of of pending TFO connections in SYN-RCVD state for TCP_FASTOPEN.
     * <p>
     * {@see #setPendingFastOpenRequestsThreshold}
     */
    public static int getPendingFastOpenRequestsThreshold() {
        return pendingFastOpenRequestsThreshold;
    }

    /**
     * Set the number of of pending TFO connections in SYN-RCVD state for TCP_FASTOPEN
     * <p/>
     * If this value goes over a certain limit the server disables all TFO connections.
     */
    public static void setPendingFastOpenRequestsThreshold(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Invalid " + value);
        }
        pendingFastOpenRequestsThreshold = value;
    }

    @Override
    public boolean isAvailable() {
        return IOUring.isAvailable();
    }

    @Override
    public Throwable unavailabilityCause() {
        return IOUring.unavailabilityCause();
    }

    @Override
    public EventLoopGroup eventLoopGroup(int type, int nThreads, ThreadFactory threadFactory, int ioRatio) {
        return new IOUringEventLoopGroup(nThreads, threadFactory);
    }

    @Override
    public DatagramChannel datagramChannel() {
        return new IOUringDatagramChannel();
    }

    @Override
    public DatagramChannel datagramChannel(InternetProtocolFamily family) {
        return new IOUringDatagramChannel(family);
    }

    @Override
    public ChannelFactory<? extends Channel> channelFactory(boolean domainSocket) {
        return IOUringSocketChannel::new;
    }

    public ChannelFactory<? extends ServerChannel> serverChannelFactory(boolean domainSocket) {
        return IOUringServerSocketChannel::new;
    }

    @Override
    public void configure(DatagramChannel channel, DatagramSocketOptions options) {
        channel.config().setOption(IOUringChannelOption.SO_REUSEPORT, options.isReusePort());
        super.configure(channel, options);
    }

    @Override
    public void configure(NetServerOptions options, boolean domainSocket, ServerBootstrap bootstrap) {
        if (!domainSocket) {
            bootstrap.option(IOUringChannelOption.SO_REUSEPORT, options.isReusePort());
            if (options.isTcpFastOpen()) {
                bootstrap.option(IOUringChannelOption.TCP_FASTOPEN, options.isTcpFastOpen() ? pendingFastOpenRequestsThreshold : 0);
            }
            bootstrap.childOption(IOUringChannelOption.TCP_QUICKACK, options.isTcpQuickAck());
            bootstrap.childOption(IOUringChannelOption.TCP_CORK, options.isTcpCork());
        }
        super.configure(options, domainSocket, bootstrap);
    }

    @Override
    public void configure(ClientOptionsBase options, boolean domainSocket, Bootstrap bootstrap) {
        if (!domainSocket) {
            if (options.isTcpFastOpen()) {
                bootstrap.option(IOUringChannelOption.TCP_FASTOPEN_CONNECT, options.isTcpFastOpen());
            }
            bootstrap.option(IOUringChannelOption.TCP_QUICKACK, options.isTcpQuickAck());
            bootstrap.option(IOUringChannelOption.TCP_CORK, options.isTcpCork());
        }
        super.configure(options, domainSocket, bootstrap);
    }
}
