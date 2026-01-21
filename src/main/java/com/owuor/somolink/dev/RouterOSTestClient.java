package com.owuor.somolink.dev;

import me.legrange.mikrotik.ApiConnection;

import javax.net.SocketFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RouterOSTestClient {

    private static ApiConnection connect() throws Exception {
        String host = "41.84.146.142";
        int port = 8292;
        String username = "chris";
        String password = "Brukhie@20";

        ApiConnection con = ApiConnection.connect(
                SocketFactory.getDefault(),
                host,
                port,
                60000
        );
        con.login(username, password);
        return con;
    }

    /** List all interfaces and return as a List<Map<String, String>> */
    public static List<Map<String, String>> listAllInterfaces() {
        List<Map<String, String>> interfaces = new ArrayList<>();
        try (ApiConnection con = connect()) {

            List<Map<String, String>> res = con.execute("/interface/print");

            for (Map<String, String> iface : res) {
                // Optional: print for debug
                System.out.printf(
                        "name=%s type=%s running=%s disabled=%s id=%s%n",
                        iface.get("name"),
                        iface.get("type"),
                        iface.get("running"),
                        iface.get("disabled"),
                        iface.get(".id")
                );

                interfaces.add(iface);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return interfaces;
    }


    /** Enable an interface (only if it can be toggled, e.g., ether ports) */
    public static boolean enableInterface(String ifaceName) {
        if (ifaceName == null || ifaceName.isEmpty()) {
            System.out.println("Interface name cannot be empty.");
            return false;
        }

        try (ApiConnection con = connect()) {
            String cmd = String.format("/interface/enable numbers=%s", ifaceName);
            con.execute(cmd);
            System.out.printf("✅ Enabled interface: %s%n", ifaceName);
            return true;
        } catch (Exception e) {
            System.err.printf("⚠ Failed to enable interface: %s%n", ifaceName);
            e.printStackTrace();
            return false;
        }
    }

    /** Disable an interface (only if it can be toggled) */
    public static boolean disableInterface(String ifaceName) {
        if (ifaceName == null || ifaceName.isEmpty()) {
            System.out.println("Interface name cannot be empty.");
            return false;
        }

        try (ApiConnection con = connect()) {
            String cmd = String.format("/interface/disable numbers=%s", ifaceName);
            con.execute(cmd);
            System.out.printf("❌ Disabled interface: %s%n", ifaceName);
            return true;
        } catch (Exception e) {
            System.err.printf("⚠ Failed to disable interface: %s%n", ifaceName);
            e.printStackTrace();
            return false;
        }
    }


    public static void main(String[] args) {
        // Example usage
        listAllInterfaces();

        // Only enable/disable individual ether ports
         enableInterface("ether3");
//         disableInterface("ether3");

        // Disable a bridge and its member ports
        listAllInterfaces();
        // disableBridgeWithMembers("bridge", true);
    }
}
