package com.owuor.somolink.dev;

import com.owuor.somolink.network.config.RouterOSClient;

public class RouterOSRunner {

    public static void main(String[] args) {

        RouterOSClient router = new RouterOSClient(
                "41.84.146.142",   // router IP
                "chris",
                "Brukhie@20"
        );

        System.out.println("=== Testing MikroTik connection ===");

        boolean ok = router.testConnection();

        System.out.println("Result: " + ok);

        // 🔽 Uncomment any method you want to test
        // router.assignIp("bridge-home", "192.168.10.1/24");
        // router.createVlan(...);
        // router.enableHotspot(...);
    }
}
