package com.qskx.mq.rpc.client;

import com.qskx.mq.rpc.client.model.RpcCallBackFuture;
import com.qskx.mq.rpc.client.model.RpcRequest;
import com.qskx.mq.rpc.client.model.RpcResponse;
import com.qskx.mq.utils.ZKServiceDiscovery;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 111111
 * @date 2018-06-09 23:20
 */
public class NettyCilent {
    private static final Logger log = LoggerFactory.getLogger(NettyCilent.class);

    private static ConcurrentHashMap<String, Channel> channelMap = new ConcurrentHashMap<>();
    public static Channel getChannel(String address) throws InterruptedException {
        if (StringUtils.isBlank(address)){
            return null;
        }

        Channel channel = channelMap.get(address);
        if (channel != null && channel.isActive()){
            log.info("");
            return channel;
        }

        String[] array = address.split(":");
        String host = array[0];
        int port = Integer.parseInt(array[1]);

        EventLoopGroup loopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(loopGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline()
                                .addLast(new NettyEncoder(RpcRequest.class))
                                .addLast(new NettyDecoder(RpcResponse.class))
                                .addLast(new NettyClientHandler());
                    }
                })
                .option(ChannelOption.TCP_NODELAY, true)//禁止使用Nagle算法
                .option(ChannelOption.SO_REUSEADDR, true)//允许重复使用本地地址和端口
                .option(ChannelOption.SO_KEEPALIVE, true);//如果在两小时内没有数据的通信时，TCP会自动发送一个活动探测数据报文
        channel = bootstrap.connect(host, port).sync().channel();
        if (channel != null && channel.isActive()) {
            channelMap.put(address, channel);
        }
        return channel;
    }

    private static void writeAndFlush(Channel channel, RpcRequest request) throws Exception {
        channel.writeAndFlush(request).sync();
    }

    public RpcResponse send(RpcRequest request) throws Exception {
        try {
            String address = ZKServiceDiscovery.discover(request.getRegistryKey());

            if (address == null){
                throw new RuntimeException("************ no address for service. *************" + request.getClassName());
            }

            Channel channel = getChannel(address);
            if (channel == null){
                throw new RuntimeException("********** no channel for service.《{}" + request.getClassName() + "》***********");
            }

            RpcCallBackFuture future = new RpcCallBackFuture(request);
//            RpcCallBackFuture.futurePool.put(request.getRequestId(), future);
            writeAndFlush(channel, request);

            return future.get(5000);
        } catch (Exception e){
            log.error("********** send -> client send request error.《{}" + e.getMessage(), e + "》**********");
            throw e;
        } finally {
            RpcCallBackFuture.futurePool.remove(request.getRequestId());
        }

    }
}
