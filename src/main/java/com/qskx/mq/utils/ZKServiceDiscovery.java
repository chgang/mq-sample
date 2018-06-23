package com.qskx.mq.utils;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 111111
 * @date 2018-06-03 22:28
 */
public class ZKServiceDiscovery {

    private static final Logger log = LoggerFactory.getLogger(ZKServiceDiscovery.class);

    private static ZooKeeper zooKeeper;
    private static ReentrantLock lock = new ReentrantLock(true);

    private static ZooKeeper getInstance(){
        if (zooKeeper == null){
            try {
                if (lock.tryLock(5, TimeUnit.SECONDS)){
                    zooKeeper = new ZooKeeper(Environment.ZK_ADDRESS, 10000, event -> {
                        if (event.getState() == Watcher.Event.KeeperState.Expired){
                            try {
                                zooKeeper.close();
                            } catch (InterruptedException e) {
                                log.error("process -> watch zookeeper client exception. error {}", e);
                            }
                            zooKeeper = null;
                            getInstance();
                        }

                        if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged && event.getPath() != null && event.getPath()
                                .startsWith(Environment.ZK_SERVICES_PATH) || event.getType() == Watcher.Event.EventType.None){
                            discoverServices();
                        }
                    });
                }
                log.info("getInstance -> zookeeper instance created successfully.");
            } catch (Exception e) {
                log.error("getInstance -> zookeeper instance created failed.");
            } finally {
                lock.unlock();
            }
        }

        if (zooKeeper == null){
            throw new NullPointerException("getInstance -> zookeeper connect failed.");
        }
        return zooKeeper;
    }

    public static volatile ConcurrentMap<String, Set<String>> serverAddrMap = new ConcurrentHashMap<>();

    private static void discoverServices(){
        if (serverAddrMap == null || serverAddrMap.size() == 0){
            return;
        }

        for (String registryKey : serverAddrMap.keySet()){
            Set<String> addressSet = new HashSet<>();

            //  /qskx-mq/rpc/registryKey
            try {
                String registryKeyPath = Environment.ZK_SERVICES_PATH.concat("/").concat(registryKey);
                Stat registryKeyStat = getInstance().exists(registryKeyPath, true);

                if (registryKeyStat != null){
                    List<String> addressList = getInstance().getChildren(registryKeyPath, true);
                    if (addressList != null && addressList.size() != 0){
                        addressSet.addAll(addressList);
                    }
                }

                serverAddrMap.put(registryKey, addressSet);
                log.info("discoverServices -> dicover service, registryKey {}, addressSet {}", registryKey, addressSet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String discover(String registryKey) {
        Set<String> addrSet = serverAddrMap.get(registryKey);
        if (addrSet == null){
            serverAddrMap.put(registryKey, new HashSet<String>());
            discoverServices();
            addrSet = serverAddrMap.get(registryKey);
        }

        if (addrSet.size() == 0){
            return null;
        }

        String address;
        List<String> addrList = new ArrayList<>(addrSet);
        if (addrList.size() == 1){
            address = addrList.get(0);
        } else {
            address = addrList.get(new Random().nextInt(addrList.size()));
        }

        return address;
    }
}
