package com.owuor.somolink.network.config;

import lombok.extern.slf4j.Slf4j;
import me.legrange.mikrotik.ApiConnection;

import java.util.*;

/**
 * RouterOSClient
 * <p>
 * Low-level wrapper for provisioning MikroTik RouterOS devices.
 * Handles interfaces, bridges, DHCP, hotspot, and wireless setup.
 * <p>
 * IMPORTANT:
 * RouterOS creates some resources DISABLED by default.
 * This class explicitly enables everything that must be enabled
 * for the configuration to actually work.
 */
@Slf4j
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
     * Establishes and returns a logged-in RouterOS API connection.
     */
    private ApiConnection connect() throws Exception {
        ApiConnection con = ApiConnection.connect(host);
        con.login(username, password);
        return con;
    }

    /**
     * Tests router reachability and credentials by reading system identity.
     */
    public boolean testConnection() {
        try (ApiConnection con = connect()) {
            String identity = con.execute("/system/identity/print")
                    .get(0).get("name");
            System.out.println("Router identity: " + identity);
            return true;
        } catch (Exception e) {
            log.info("Router error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Assigns an IP address to an interface.
     * Ensures the interface is ENABLED before assigning the IP.
     */
    public void assignIp(String bridgeName, String cidr) throws Exception {
        try (ApiConnection con = connect()) {
            String cmd = String.format("/ip/address/add address=%s interface=%s", cidr, bridgeName);
            System.out.println("[DEBUG] Executing: " + cmd);
            try {
                con.execute(cmd);
            } catch (Exception ex) {
                System.err.println("[ERROR] Failed to assign IP %s to bridge " + bridgeName + ": " + ex.getMessage());
                throw ex;
            }
        }
    }


    /**
     * Creates a DHCP server on a given interface or bridge.
     * Explicitly enables the interface and the DHCP server.
     */
    public void createDhcp(String portName, String subnetCidr, String poolRange) throws Exception {
        try (ApiConnection con = connect()) {

            String poolName = "pool_" + portName + "_" +
                    UUID.randomUUID().toString().substring(0, 6);
            String dhcpName = "dhcp_" + portName;

            // Ensure interface is enabled
            con.execute("/interface/set [find name=" + portName + "] disabled=no");

            // Create IP pool (pools are always enabled by default)
            con.execute(String.format(
                    "/ip/pool/add name=%s ranges=%s",
                    poolName, poolRange
            ));

            // Create DHCP server (RouterOS creates this DISABLED)
            con.execute(String.format(
                    "/ip/dhcp-server/add name=%s interface=%s address-pool=%s",
                    dhcpName, portName, poolName
            ));

            // Add DHCP network
            String gatewayIp = subnetCidr.split("/")[0];
            con.execute(String.format(
                    "/ip/dhcp-server/network/add address=%s gateway=%s",
                    subnetCidr, gatewayIp
            ));

            // REQUIRED: Enable DHCP server
            con.execute("/ip/dhcp-server/set [find name=" + dhcpName + "] disabled=no");
        }
    }

    /**
     * Returns a list of raw (unused) interfaces.
     * Filters out bridges, disabled interfaces, and interfaces already in use.
     */
    public List<String> getInterfaces() throws Exception {
        try (ApiConnection con = connect()) {

            List<Map<String, String>> allIfaces = con.execute("/interface/print");
            log.info("All interfaces: {}", allIfaces);
            List<Map<String, String>> bridgePorts = con.execute("/interface/bridge/port/print");
            List<Map<String, String>> ipAddresses = con.execute("/ip/address/print");
            List<Map<String, String>> dhcpServers = con.execute("/ip/dhcp-server/print");

            Set<String> usedInterfaces = new HashSet<>();

            for (Map<String, String> p : bridgePorts) {
                usedInterfaces.add(p.get("interface"));
            }
            for (Map<String, String> ip : ipAddresses) {
                usedInterfaces.add(ip.get("interface"));
            }
            for (Map<String, String> dhcp : dhcpServers) {
                usedInterfaces.add(dhcp.get("interface"));
            }

            List<String> rawInterfaces = new ArrayList<>();

            for (Map<String, String> iface : allIfaces) {
                String name = iface.get("name");
                String type = iface.get("type");

                if (name == null) continue;
                if ("loopback".equals(type)) continue;
                if ("bridge".equals(type)) continue;

                if (!usedInterfaces.contains(name)) {
                    rawInterfaces.add(name);
                }
            }

            return rawInterfaces;
        }
    }

    /**
     * Automatically creates DHCP pool, server, and network.
     * Explicitly enables the interface and DHCP server.
     */

    public void createDhcpAuto(String bridgeName, String ip, String poolName, String poolRange, String networkCidr) throws Exception {
        try (ApiConnection con = connect()) {

            String dhcpName = "dhcp_" + bridgeName;

            // Create pool
            String cmdPool = String.format("/ip/pool/add name=%s ranges=%s", poolName, poolRange);
            System.out.println("[DEBUG] Executing: " + cmdPool);
            try {
                con.execute(cmdPool);
            } catch (Exception ex) {
                System.err.println("[ERROR] Failed to create pool: " + ex.getMessage());
                throw ex;
            }

            // Create DHCP server
            String cmdDhcp = String.format("/ip/dhcp-server/add name=%s interface=%s address-pool=%s", dhcpName, bridgeName, poolName);
            System.out.println("[DEBUG] Executing: " + cmdDhcp);
            try {
                con.execute(cmdDhcp);
            } catch (Exception ex) {
                System.err.println("[ERROR] Failed to create DHCP server: " + ex.getMessage());
                throw ex;
            }

            // Add DHCP network
            String cmdNetwork = String.format("/ip/dhcp-server/network/add address=%s gateway=%s", networkCidr, ip);
            System.out.println("[DEBUG] Executing: " + cmdNetwork);
            try {
                con.execute(cmdNetwork);
            } catch (Exception ex) {
                System.err.println("[ERROR] Failed to create DHCP network: " + ex.getMessage());
                throw ex;
            }

            // Enable DHCP server
            String cmdEnableDhcp = String.format("/ip/dhcp-server/enable numbers=%s", dhcpName);
            System.out.println("[DEBUG] Executing: " + cmdEnableDhcp);
            try {
                con.execute(cmdEnableDhcp);
            } catch (Exception ex) {
                System.err.println("[ERROR] Failed to enable DHCP: " + ex.getMessage());
                throw ex;
            }
        }
    }


    /**
     * Creates a hotspot user profile with bandwidth and timeout limits.
     */
    public void createHotspotUserProfile(
            String profileName,
            int rateUpload,
            int rateDownload,
            String sessionTimeout,
            String idleTimeout
    ) throws Exception {

        try (ApiConnection con = connect()) {

            String rateLimit = rateUpload + "k/" + rateDownload + "k";
            String session = sessionTimeout != null ? sessionTimeout : "00:00:00";
            String idle = idleTimeout != null ? idleTimeout : "00:00:00";

            con.execute(String.format(
                    "/ip/hotspot/user/profile/add name=\"%s\" rate-limit=%s session-timeout=%s idle-timeout=%s",
                    profileName, rateLimit, session, idle
            ));
        }
    }

    /**
     * Creates and enables a hotspot server on an interface or bridge.
     * Hotspots are CREATED DISABLED by RouterOS.
     */
    public void setupHotspot(String interfaceName, String hotspotName, String profileName) throws Exception {
        try (ApiConnection con = connect()) {

            // Ensure interface or bridge is enabled
            con.execute("/interface/set [find name=" + interfaceName + "] disabled=no");

            String hsName = hotspotName != null ? hotspotName : interfaceName;

            con.execute(String.format(
                    "/ip/hotspot/add name=%s interface=%s profile=%s address-pool=dhcp_%s",
                    hsName, interfaceName, profileName, interfaceName
            ));

            // REQUIRED: Enable hotspot
            con.execute("/ip/hotspot/set [find name=" + hsName + "] disabled=no");
        }
    }

    /**
     * Creates a hotspot server profile (HTML login, auth methods, DNS).
     */
    public void createHotspotServerProfile(
            String profileName,
            String hotspotAddress,
            String dnsName
    ) throws Exception {

        try (ApiConnection con = connect()) {

            con.execute(String.format(
                    "/ip/hotspot/profile/add name=%s hotspot-address=%s dns-name=%s " +
                            "html-directory=hotspot login-by=cookie,http-chap,http-pap",
                    profileName, hotspotAddress, dnsName
            ));
        }
    }

    /**
     * Creates a hotspot user with username, password, and profile.
     */
    public void createHotspotUser(
            String username,
            String password,
            String profileName,
            String dnsName
    ) throws Exception {

        try (ApiConnection con = connect()) {

            con.execute(String.format(
                    "/ip/hotspot/user/add name=%s password=%s profile=%s",
                    username, password, profileName
            ));
        }
    }

    /**
     * Creates a bridge and attaches interfaces.
     * Ensures both bridge and interfaces are enabled.
     */
    public void createBridge(String bridgeName, List<String> interfaces) throws Exception {
        try (ApiConnection con = connect()) {

            // Enable interfaces FIRST
            for (String iface : interfaces) {
                String cmd;

                if (iface.toLowerCase().startsWith("wlan")) {
                    // Wireless: use 'set disabled=no'
                    cmd = String.format("/interface/wireless/enable numbers=%s", iface);
                } else if (iface.toLowerCase().startsWith("ether")) {
                    // Ethernet: can use 'enable'
                    cmd = String.format("/interface/enable numbers=%s", iface);
                } else {
                    // Fallback: generic
                    cmd = String.format("/interface/set %s disabled=no", iface);
                }

                System.out.println("[DEBUG] Executing: " + cmd);
                try {
                    con.execute(cmd);
                } catch (Exception ex) {
                    System.err.println("[ERROR] Failed to enable interface " + iface + ": " + ex.getMessage());
                    throw ex;
                }
            }


            // Create bridge (already enabled)
            String cmdBridge = String.format("/interface/bridge/add name=%s disabled=no", bridgeName);
            System.out.println("[DEBUG] Executing: " + cmdBridge);
            try {
                con.execute(cmdBridge);
            } catch (Exception ex) {
                System.err.println("[ERROR] Failed to create bridge " + bridgeName + ": " + ex.getMessage());
                throw ex;
            }

            // Attach interfaces to bridge
            for (String iface : interfaces) {
                String cmdPort = String.format("/interface/bridge/port/add bridge=%s interface=%s", bridgeName, iface);
                System.out.println("[DEBUG] Executing: " + cmdPort);
                try {
                    con.execute(cmdPort);
                } catch (Exception ex) {
                    System.err.println("[ERROR] Failed to add interface " + iface + " to bridge " + bridgeName + ": " + ex.getMessage());
                    throw ex;
                }
            }
        }
    }

    /**
     * Removes a bridge and all related configuration (ports, IPs, DHCP).
     * Used for rollback and cleanup.
     */
    public void deleteBridgeIfExists(String bridgeName) throws Exception {
        try (ApiConnection con = connect()) {
            String cmd = String.format("/interface/bridge/remove where name=%s", bridgeName);
            System.out.println("[DEBUG] Executing: " + cmd);
            try {
                con.execute(cmd);
            } catch (Exception ex) {
                System.err.println("[ERROR] Failed to delete bridge: " + ex.getMessage());
                throw ex;
            }
        }
    }

    /**
     * Configures an OPEN (no password) WLAN for hotspot usage.
     * Sets AP mode, SSID, removes security, and enables the interface.
     */
    public void setupOpenWlan(
            String wlanInterface,
            String ssid
    ) throws Exception {

        try (ApiConnection con = connect()) {

            // 1. Configure wireless interface
            String cmdSetWlan = String.format(
                    "/interface/wireless/set numbers=%s ssid=\"%s\" mode=ap-bridge " ,
                    wlanInterface,
                    ssid
            );
            System.out.println("[DEBUG] Executing: " + cmdSetWlan);
            try {
                con.execute(cmdSetWlan);
            } catch (Exception ex) {
                System.err.println("[ERROR] Failed to configure WLAN: " + ex.getMessage());
                throw ex;
            }

            // 2. Enable wireless interface
            String cmdEnableWlan = String.format(
                    "/interface/wireless/enable numbers=%s",
                    wlanInterface
            );
            System.out.println("[DEBUG] Executing: " + cmdEnableWlan);
            try {
                con.execute(cmdEnableWlan);
            } catch (Exception ex) {
                System.err.println("[ERROR] Failed to enable WLAN: " + ex.getMessage());
                throw ex;
            }
        }
    }


    /**
     * Creates a hotspot user for a SCHOOL device.
     * Used when provisioning a school router to allow device authentication
     * against the hotspot profile assigned to that school.
     */
    public void createSchoolHotspotUser(
            String username,
            String password,
            String schoolProfile
    ) throws Exception {

        try (ApiConnection con = connect()) {

            System.out.println("[HOTSPOT] Creating school hotspot user...");
            System.out.println("Username: " + username);

            // Create hotspot user
            String cmd = String.format(
                    "/ip/hotspot/user/add name=%s password=%s profile=%s",
                    username, password, schoolProfile
            );

            con.execute(cmd);

            System.out.println("[HOTSPOT] School hotspot user created successfully");
        }
    }


}
