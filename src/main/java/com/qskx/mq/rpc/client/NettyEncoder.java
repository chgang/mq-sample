package com.qskx.mq.rpc.client;

import com.qskx.mq.utils.HessianSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * @author 111111
 * @date 2018-06-10 10:31
 */
public class NettyEncoder extends MessageToByteEncoder<Object> {

    private Class<?> genericClass;
    public NettyEncoder(Class<?> genericClass){
        this.genericClass = genericClass;
    }
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (genericClass.isInstance(o)){
            byte[] data = HessianSerializer.serialize(o);//读取消息的长度
            byteBuf.writeInt(data.length);//先将消息长度写入，也就是消息头
            byteBuf.writeBytes(data);//消息体中包含我们要发送的数据
        }
    }
}
