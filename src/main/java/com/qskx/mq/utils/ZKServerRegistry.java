package com.qskx.mq.utils;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 111111
 * @date 2018-06-09 14:59
 */
public class ZKServerRegistry {

    private static final Logger log = LoggerFactory.getLogger(ZKServerRegistry.class);

    private static ZooKeeper zooKeeper;
    private static ReentrantLock lock = new ReentrantLock(true);
    public static ZooKeeper getInstance(){
        try {
            if (zooKeeper == null){
                if (lock.tryLock(2, TimeUnit.SECONDS)){
                    if (zooKeeper == null){
                        zooKeeper = new ZooKeeper(Environment.ZK_ADDRESS, 10000, event -> {
                            if (event.getState() == Watcher.Event.KeeperState.Expired){
                                try {
                                    zooKeeper.close();
                                } catch (InterruptedException e) {
                                    log.error("getInstance -> zookeeper session expired and close error. error {}", e.getMessage(), e);
                                } finally {
                                    zooKeeper = null;
                                }
                                getInstance();
                            }
                        });
                    }
                }
            }

            Stat baseStat = zooKeeper.exists(Environment.ZK_BASE_PATH, true);
            if (baseStat == null){
                zooKeeper.create(Environment.ZK_BASE_PATH, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

            Stat serverStat = zooKeeper.exists(Environment.ZK_SERVICES_PATH, true);
            if (serverStat == null){
                zooKeeper.create(Environment.ZK_SERVICES_PATH, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        if (zooKeeper == null){
            throw new NullPointerException("");
        }
        return zooKeeper;
    }

    public static void registerServers(){

    }
}
