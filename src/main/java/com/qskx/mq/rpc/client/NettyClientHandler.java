package com.qskx.mq.rpc.client;

import com.qskx.mq.rpc.client.model.RpcCallBackFuture;
import com.qskx.mq.rpc.client.model.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 111111
 * @date 2018-06-09 23:22
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger log = LoggerFactory.getLogger(NettyClientHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        RpcCallBackFuture future = RpcCallBackFuture.futurePool.get(rpcResponse.getRequestId());
        future.setResponse(rpcResponse);
        RpcCallBackFuture.futurePool.put(rpcResponse.getRequestId(), future);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(">>>>>>>>>>> netty client caught exception", cause);
        ctx.close();
    }
}
