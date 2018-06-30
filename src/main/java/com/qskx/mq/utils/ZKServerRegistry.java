package com.qskx.mq.utils;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 111111
 * @date 2018-06-09 14:59
 */
public class ZKServerRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ZKServerRegistry.class);

    private static ZooKeeper zooKeeper;
    private static ReentrantLock lock = new ReentrantLock(true);
//    public static ZooKeeper getInstance(){
//            if (zooKeeper == null){
//                try {
//                    if (lock.tryLock(5, TimeUnit.SECONDS)){
//                        if (zooKeeper == null){
//                            try {
//                                zooKeeper = new ZooKeeper(Environment.ZK_ADDRESS, 10000, new Watcher() {
//                                    @Override
//                                    public void process(WatchedEvent event) {
//
//                                        // session expire, close old and create new
//                                        if (event.getState() == Event.KeeperState.Expired) {
//                                            try {
//                                                zooKeeper.close();
//                                            } catch (InterruptedException e) {
//                                                logger.error("", e);
//                                            }
//                                            zooKeeper = null;
//                                            getInstance();
//                                        }
//                                    }
//                                });
//
//                                Stat baseStat = zooKeeper.exists(Environment.ZK_BASE_PATH, true);
//                                if (baseStat == null){
//                                    zooKeeper.create(Environment.ZK_BASE_PATH, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//                                }
//
//                                Stat serverStat = zooKeeper.exists(Environment.ZK_SERVICES_PATH, true);
//                                if (serverStat == null){
//                                    zooKeeper.create(Environment.ZK_SERVICES_PATH, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//                                }
//
//                                logger.info(">>>>>>>>> xxl-rpc zookeeper connnect success.");
//                            } catch (Exception e){
//
//                            }finally {
//                                lock.unlock();
//                            }
//                        }
//                    }
//                } catch (Exception e){
//
//                }
//            }
//        if (zooKeeper == null) {
//            throw new NullPointerException(">>>>>>>>>>> xxl-rpc, zookeeper connect fail.");
//        }
//        return zooKeeper;
//    }


public static ZooKeeper getInstance(){

            if (zooKeeper == null){
                try {
                    if (lock.tryLock(5, TimeUnit.SECONDS)){

                        if (zooKeeper == null) {
                            try {
                                zooKeeper = new ZooKeeper(Environment.ZK_ADDRESS, 10000, new Watcher() {
                                    @Override
                                    public void process(WatchedEvent watchedEvent) {
                                        if (watchedEvent.getState() == Event.KeeperState.Expired) {
                                            try {
                                                zooKeeper.close();
                                            } catch (InterruptedException e) {
                                                logger.error("", e);
                                            }
                                            zooKeeper = null;
                                            getInstance();
                                        }
                                    }

                                });
                                Stat baseStat = zooKeeper.exists(Environment.ZK_BASE_PATH, false);
                                if (baseStat == null) {
                                    zooKeeper.create(Environment.ZK_BASE_PATH, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                                }

                                Stat serverStat = zooKeeper.exists(Environment.ZK_SERVICES_PATH, false);
                                if (serverStat == null) {
                                    zooKeeper.create(Environment.ZK_SERVICES_PATH, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                                }

                                logger.info(">>>>>>>>> xxl-rpc zookeeper connnect success.");
                            } catch (Exception E) {

                            } finally {
                                lock.unlock();
                            }
                        }
                    }
                } catch (Exception e){

                }

            }

        if (zooKeeper == null){
            throw new NullPointerException("********* ZK instance is null.***********");
        }
        return zooKeeper;
    }

    public static void registerServers(int port, Set<String> registryKeyList) throws KeeperException, InterruptedException {
        if (port < 1 || registryKeyList == null || registryKeyList.size() == 0){
            return;
        }

        String address = IpUtil.getAddress(port);
        for (String registerKey : registryKeyList){
            String registerKeyPath = Environment.ZK_SERVICES_PATH.concat("/").concat(registerKey);
            Stat registerKeyPathStat = getInstance().exists(registerKeyPath, false);
            if (registerKeyPathStat == null){
                getInstance().create(registerKeyPath, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

            String registerKeyAddressPath = registerKeyPath.concat("/").concat(address);
            Stat addressStat = getInstance().exists(registerKeyAddressPath, false);
            if (addressStat ==  null){
                getInstance().create(registerKeyAddressPath, address.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            }
            logger.info("registerServers -> registerKey {}, registerKeyPath {}, address {}", registerKey, registerKeyPath, address);
        }
    }

    public static void main(String[] args) throws KeeperException, InterruptedException {
        registerServers(3333, new HashSet<String>(Arrays.asList("path2")));
        TimeUnit.SECONDS.sleep(9999);
    }
}
