package com.qskx.mq.rpc.factroy;

import com.qskx.mq.rpc.client.model.RpcRequest;
import com.qskx.mq.rpc.client.model.RpcResponse;
import com.qskx.mq.rpc.server.NettyServer;
import com.qskx.mq.utils.ZKServerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 111111
 * @date 2018-06-23 23:26
 */
public class NetComServerFactory {

    private static final Logger log = LoggerFactory.getLogger(NetComServerFactory.class);

    private static int port;
    private static Map<String, Object> registryMap;

    public NetComServerFactory(int port, Map<String, Object> registryMap){
        this.port = port;
        this.registryMap = registryMap;

        new NettyServer().start(port);
    }

    private static Executor executor = Executors.newCachedThreadPool();
    public static void registry() {
        ZKServerRegistry.registerServers(port, registryMap.keySet());
    }

    public static RpcResponse invokeService(RpcRequest request, Object serviceBean) {
        if (serviceBean == null){
            serviceBean = registryMap.get(request.getRegistryKey());
        }

        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());

        try {
            Class<?> serviceClass = serviceBean.getClass();
            String methodName = request.getMethodName();
            Class<?>[] parameterTypes = request.getParameterTypes();
            Object[] parameters = request.getParameters();

            /*Method method = serviceClass.getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(serviceBean, parameters);*/

            FastClass fastClass = FastClass.create(serviceClass);
            FastMethod fastMethod = fastClass.getMethod(methodName, parameterTypes);

            Object result = fastMethod.invoke(serviceBean, parameters);
            response.setResult(result);
        } catch (Exception e){
            log.error("********* netty server response client caught exception.《" + e + "》***********");
            response.setError(e);
        }
        return response;
    }
}
