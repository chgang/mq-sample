package com.qskx.mq.rpc.client.model;

import java.io.Serializable;

/**
 * @author 111111
 * @date 2018-06-10 14:27
 */
public class RpcResponse implements Serializable {

    private static final long serialVersionUID = 4802809738055367573L;

    private String requestId;
    private Throwable error;
    private Object result;

    public boolean isError() {
        return error != null;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "NettyResponse [requestId=" + requestId + ", error=" + error
                + ", result=" + result + "]";
    }
}
