package com.qskx.mq.utils;

import java.util.Properties;

/**
 * @author 111111
 * @date 2018-06-03 22:46
 */
public class Environment {

    public static final String ZK_BASE_PATH = "/qskx-mq";

    public static final String ZK_SERVICES_PATH = ZK_BASE_PATH.concat("/rpc");

    public static final String ZK_ADDRESS;

    private static final String ZK_ADDRESS_FILE = "/mq-conf.properties";

    static {
        Properties prop = PropertiesUtil.loadProperties(ZK_ADDRESS_FILE);
        ZK_ADDRESS = PropertiesUtil.getString(prop, ZK_ADDRESS_FILE);
    }

}
