package com.qskx.mq.rpc.server;

import com.qskx.mq.rpc.client.NettyDecoder;
import com.qskx.mq.rpc.client.NettyEncoder;
import com.qskx.mq.rpc.client.model.RpcRequest;
import com.qskx.mq.rpc.client.model.RpcResponse;
import com.qskx.mq.rpc.factroy.NetComServerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 111111
 * @date 2018-06-09 23:21
 */
public class NettyServer {
    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    public void start(final int port){
        new Thread(() -> {
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline()
                                        .addLast(new NettyDecoder(RpcRequest.class))
                                        .addLast(new NettyEncoder(RpcResponse.class))
                                        .addLast(new NettyServerHandler());
                            }
                        }).option(ChannelOption.SO_BACKLOG, 128)//标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50。
                        .option(ChannelOption.TCP_NODELAY, true)
                        .option(ChannelOption.SO_REUSEADDR, true)
                        .option(ChannelOption.SO_KEEPALIVE, true);
                ChannelFuture future = bootstrap.bind(port).sync();
                NetComServerFactory.registry();
                future.channel().closeFuture().sync();
            } catch (Exception e){

            } finally {
                bossGroup.shutdownGracefully();
                workGroup.shutdownGracefully();
            }
        }).start();

    }
}
