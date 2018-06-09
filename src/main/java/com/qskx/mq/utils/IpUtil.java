package com.qskx.mq.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author 111111
 * @date 2018-06-09 22:38
 */
public class IpUtil {
    private static final Logger log = LoggerFactory.getLogger(IpUtil.class);

    public static String getIp(){
        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress inetAddress = null;
            while (interfaces.hasMoreElements()){
                NetworkInterface ni = interfaces.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()){
                    inetAddress = addresses.nextElement();
                    if (inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().indexOf(":") == -1){
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            log.error("getIp -> get ip failed. error {}", e.getMessage(), e);
        }
        return null;
    }

    public static String getAddress(int port){
        String ip = getIp();
        if (ip == null){
            return null;
        }

        return ip.concat(":").concat(String.valueOf(port));
    }
}
