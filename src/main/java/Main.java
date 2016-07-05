import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.handler.codec.base64.Base64Decoder;

/**
 * Created by vladimir on 7/5/16.
 */
public class Main {
    public static void main(String[] args) throws Exception{
        int workerThreads = 2;
        String host = "localhost";
        int port = 9090;
        Bootstrap bootstrap = new Bootstrap()
                .group(new EpollEventLoopGroup(workerThreads))
                .channel(EpollDatagramChannel.class)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(EpollChannelOption.SO_REUSEPORT, true)
                .handler(new Base64Decoder());

        ChannelFuture future;
        for(int i = 0; i < workerThreads; ++i) {
            future = bootstrap.bind(host, port).await();
            if(!future.isSuccess())
                throw new Exception(String.format("Fail to bind on [host = %s , port = %d].", host, port), future.cause());
        }
    }
}
