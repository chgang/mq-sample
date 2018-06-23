package com.qskx.mq.rpc.server;

import com.qskx.mq.rpc.client.model.RpcRequest;
import com.qskx.mq.rpc.client.model.RpcResponse;
import com.qskx.mq.rpc.factroy.NetComServerFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 111111
 * @date 2018-06-23 23:04
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger log = LoggerFactory.getLogger(NettyServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        RpcResponse response = NetComServerFactory.invokeService(request, null);

        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        log.error("********* netty sever caught exception.《" + cause + "》************");
        ctx.close();
    }
}
