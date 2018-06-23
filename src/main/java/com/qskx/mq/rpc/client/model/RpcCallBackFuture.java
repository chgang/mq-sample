package com.qskx.mq.rpc.client.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeoutException;

/**
 * @author 111111
 * @date 2018-06-23 12:27
 */
public class RpcCallBackFuture {
    public static ConcurrentMap<String, RpcCallBackFuture> futurePool = new ConcurrentHashMap<>();

    private RpcRequest request;
    private RpcResponse response;

    private boolean isDone;
    private Object lock = new Object();

    public RpcCallBackFuture(RpcRequest request){
        this.request = request;
        futurePool.put(request.getRequestId(), this);
    }

    public RpcResponse getResponse() {
        return response;
    }

    public void setResponse(RpcResponse response) {
        this.response = response;
        synchronized (lock){
            isDone = true;
            lock.notifyAll();
        }
    }

    public RpcResponse get(long timeoutMillis) throws TimeoutException {
        if (!isDone){
            synchronized (lock){
                try {
                    lock.wait(timeoutMillis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!isDone){
            throw new TimeoutException();
        }
        return response;
    }
}
