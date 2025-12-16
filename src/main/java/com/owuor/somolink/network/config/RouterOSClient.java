package com.owuor.somolink.network.config;

import com.owuor.somolink.network.dto.HotspotLoginResponse;
import jakarta.validation.constraints.NotBlank;
import me.legrange.mikrotik.ApiConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A wrapper to interact with MikroTik RouterOS
 */
public class RouterOSClient {

    private final String host;      // Router IP
    private final String username;
    private final String password;

    public RouterOSClient(String host, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
    }

    /**
     * Connect to the router
     */
    private ApiConnection connect() throws Exception {
        ApiConnection con = ApiConnection.connect(host);
        con.login(username, password);
        return con;
    }
    /**
     * Test if the router is reachable and credentials work
     */
    public boolean testConnection() {
        try (ApiConnection con = connect()) {
            // Simple test: fetch router identity
            String identity = con.execute("/system/identity/print").get(0).get("name");
            System.out.println("Router identity: " + identity);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Assign an IP to a port/interface
     */
    public void assignIp(String portName, String cidr) throws Exception {
        try (ApiConnection con = connect()) {
            con.execute(String.format("/ip/address/add address=%s interface=%s", cidr, portName));
        }
    }

    /**
     * Create DHCP server on the port
     */
    public void createDhcp(String portName, String subnetCidr, String poolRange) throws Exception {
        try (ApiConnection con = connect()) {
            String poolName = "pool_" + portName + "_" + UUID.randomUUID().toString().substring(0, 6);


            // 1. Create pool
            con.execute(String.format("/ip/pool/add name=%s ranges=%s", poolName, poolRange));

            // 2. Add DHCP server
            con.execute(String.format("/ip/dhcp-server/add name=dhcp_%s interface=%s address-pool=%s",
                    portName, portName, poolName));

            // 3. Add network
            String gatewayIp = subnetCidr.split("/")[0]; // first IP in CIDR
            con.execute(String.format("/ip/dhcp-server/network/add address=%s gateway=%s", subnetCidr, gatewayIp));
        }
    }

    public List<String> getInterfaces() throws Exception {
        try (ApiConnection con = connect()) {
            List<String> interfaces = new ArrayList<>();

            List<Map<String, String>> result = con.execute("/interface/print");
            for (Map<String, String> row : result) {
                String name = row.get("name");
                if (name != null) {
                    interfaces.add(name);
                }
            }

            return interfaces;
        }
    }

    public void createDhcpAuto(String portName, String ip, int prefix, String poolName, String poolRange, String networkCidr) throws Exception {
        try (ApiConnection con = connect()) {

            // 1. Create pool
            System.out.println("Creating DHCP pool: " + poolName + " with range " + poolRange);
            con.execute(String.format("/ip/pool/add name=%s ranges=%s", poolName, poolRange));

            // 2. Create DHCP server
            System.out.println("Creating DHCP server on interface: " + portName);
            con.execute(String.format(
                    "/ip/dhcp-server/add name=dhcp_%s interface=%s address-pool=%s",
                    portName, portName, poolName
            ));

            // 3. Add DHCP network
            System.out.println("Adding DHCP network: " + networkCidr + " gateway: " + ip);
            con.execute(String.format(
                    "/ip/dhcp-server/network/add address=%s gateway=%s",
                    networkCidr, ip
            ));

            System.out.println("DHCP configuration applied successfully.");
        }
    }


    public void createHotspotUserProfile(String profileName, int rateUpload, int rateDownload,
                                         String sessionTimeout, String idleTimeout) throws Exception {
        System.out.println("[DEBUG] Starting createHotspotUserProfile...");
        System.out.println("[DEBUG] Profile Name: " + profileName);
        System.out.println("[DEBUG] Rate Upload: " + rateUpload + "kbps");
        System.out.println("[DEBUG] Rate Download: " + rateDownload + "kbps");
        System.out.println("[DEBUG] Session Timeout: " + sessionTimeout);
        System.out.println("[DEBUG] Idle Timeout: " + idleTimeout);

        try (ApiConnection con = connect()) {
            System.out.println("[DEBUG] Connected to MikroTik router");

            // Default values
            String session = sessionTimeout != null ? sessionTimeout : "00:00:00";
            String idle = idleTimeout != null ? idleTimeout : "00:00:00";

            // Build rate-limit string
            String rateLimitStr = rateUpload + "k/" + rateDownload + "k";

            System.out.println("[DEBUG] Rate-limit string: " + rateLimitStr);

            // Build command
            String cmd = String.format("/ip/hotspot/user/profile/add name=\"%s\" rate-limit=%s session-timeout=%s idle-timeout=%s",
                    profileName,
                    rateLimitStr,
                    session,
                    idle
            );

            System.out.println("[DEBUG] Executing command: " + cmd);

            // Execute
            try {
                con.execute(cmd);
                System.out.println("[DEBUG] Hotspot profile created: " + profileName);
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to create profile: " + e.getMessage());
                throw e;
            }
        }
    }


    public void setupHotspot(String interfaceName, String hotspotName, String profileName) throws Exception {
        try (ApiConnection con = connect()) {
            // Default values if hotspotName not provided
            String hsName = hotspotName != null ? hotspotName : interfaceName;

            // Add hotspot server
            String cmd = String.format("/ip/hotspot/add name=%s interface=%s profile=%s address-pool=dhcp_%s",
                    hsName, interfaceName, profileName, interfaceName);
            con.execute(cmd);

            // Enable hotspot
            con.execute("/ip/hotspot/enable " + hsName);

            System.out.println("Hotspot setup completed on interface: " + interfaceName);
        }
    }

    public void createHotspotServerProfile(String profileName, String hotspotAddress, String dnsName) throws Exception {
        try (ApiConnection con = connect()) {
            // Check if profile already exists
            con.execute("/ip/hotspot/profile/print where name=" + profileName);

            // Add hotspot profile
            String cmd = String.format("/ip/hotspot/profile/add name=%s hotspot-address=%s dns-name=%s html-directory=hotspot login-by=cookie,http-chap,http-pap",
                    profileName, hotspotAddress, dnsName);
            con.execute(cmd);

            System.out.println("Hotspot server profile created: " + profileName);
        }
    }


    public void createHotspotUser(
            String username,
            String password,
            String profileName,
            String dnsName
    ) throws Exception {

        try (ApiConnection con = connect()) {

            System.out.println("[HOTSPOT] Creating user...");
            System.out.println("Username: " + username);
            System.out.println("Profile: " + profileName);

            // 1️⃣ Create hotspot user
            String cmd = String.format(
                    "/ip/hotspot/user/add name=%s password=%s profile=%s",
                    username,
                    password,
                    profileName
            );

            con.execute(cmd);

            System.out.println("[HOTSPOT] User created successfully");

        }
    }
    public void createOrUpdateHotspotUser(
            String username,
            String password,
            String schoolProfile
    ) throws Exception {

        try (ApiConnection con = connect()) {

            System.out.println("[HOTSPOT] Creating user...");
            System.out.println("Username: " + username);

            // 1️⃣ Create hotspot user
            String cmd = String.format(
                    "/ip/hotspot/user/add name=%s password=%s profile=%s",
                    username,
                    password,
                    schoolProfile
            );

            con.execute(cmd);

            System.out.println("[HOTSPOT] User created successfully");


        }
    }

    public void createBridge(String bridgeName, List<String> interfaces) throws Exception {
        try (ApiConnection con = connect()) {
            // 1. Add bridge
            con.execute(String.format("/interface/bridge/add name=%s", bridgeName));
            System.out.println("Bridge " + bridgeName + " created");

            // 2. Add interfaces to bridge
            for (String iface : interfaces) {
                con.execute(String.format("/interface/bridge/port/add bridge=%s interface=%s", bridgeName, iface));
                System.out.println("Interface " + iface + " added to bridge " + bridgeName);
            }
        }
    }

    public void deleteBridgeIfExists(String bridgeName) throws Exception {

        try (ApiConnection con = connect()) {

            // -----------------------------------------------------
            // STEP 1: Find bridge ID
            // -----------------------------------------------------
            List<Map<String, String>> bridges = con.execute(
                    "/interface/bridge/print where name=" + bridgeName
            );

            if (bridges.isEmpty()) {
                System.out.println("[ROLLBACK] Bridge does not exist: " + bridgeName);
                return; // Nothing to delete
            }

            String bridgeId = bridges.get(0).get(".id");
            System.out.println("[ROLLBACK] Found bridge " + bridgeName + " with id " + bridgeId);

            // -----------------------------------------------------
            // STEP 2: Remove bridge ports (interfaces)
            // -----------------------------------------------------
            List<Map<String, String>> ports = con.execute(
                    "/interface/bridge/port/print where bridge=" + bridgeName
            );

            for (Map<String, String> port : ports) {
                String portId = port.get(".id");
                String iface = port.get("interface");

                System.out.println("[ROLLBACK] Removing interface " + iface + " from bridge " + bridgeName);
                con.execute("/interface/bridge/port/remove " + portId);
            }

            // -----------------------------------------------------
            // STEP 3: Remove IP addresses from bridge
            // -----------------------------------------------------
            List<Map<String, String>> ips = con.execute(
                    "/ip/address/print where interface=" + bridgeName
            );

            for (Map<String, String> ip : ips) {
                String ipId = ip.get(".id");
                System.out.println("[ROLLBACK] Removing IP from bridge " + bridgeName);
                con.execute("/ip/address/remove " + ipId);
            }

            // -----------------------------------------------------
            // STEP 4: Remove DHCP server attached to bridge
            // -----------------------------------------------------
            List<Map<String, String>> dhcps = con.execute(
                    "/ip/dhcp-server/print where interface=" + bridgeName
            );

            for (Map<String, String> dhcp : dhcps) {
                String dhcpId = dhcp.get(".id");
                System.out.println("[ROLLBACK] Removing DHCP server from bridge " + bridgeName);
                con.execute("/ip/dhcp-server/remove " + dhcpId);
            }

            // -----------------------------------------------------
            // STEP 5: Finally remove the bridge
            // -----------------------------------------------------
            con.execute("/interface/bridge/remove " + bridgeId);

            System.out.println("[ROLLBACK] Bridge removed successfully: " + bridgeName);
        }
    }

}
