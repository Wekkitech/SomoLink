package com.owuor.somolink.network.utils;


public class NetworkUtils {

    /** Converts IP + prefix → network CIDR */
    public static String toNetworkCidr(String ip, int prefix) {
        String[] octets = ip.split("\\.");
        int a = Integer.parseInt(octets[0]);
        int b = Integer.parseInt(octets[1]);
        int c = Integer.parseInt(octets[2]);
        int d = Integer.parseInt(octets[3]);

        int mask = 0xffffffff << (32 - prefix);

        int ipInt = (a << 24) | (b << 16) | (c << 8) | d;
        int network = ipInt & mask;

        return String.format("%d.%d.%d.%d/%d",
                (network >> 24) & 0xff,
                (network >> 16) & 0xff,
                (network >> 8) & 0xff,
                network & 0xff,
                prefix
        );
    }

    /** Calculates DHCP pool range automatically */
    public static String calculatePool(String ip, int prefix) {
        String network = toNetworkCidr(ip, prefix).split("/")[0];
        String[] parts = network.split("\\.");

        int a = Integer.parseInt(parts[0]);
        int b = Integer.parseInt(parts[1]);
        int c = Integer.parseInt(parts[2]);

        String start = a + "." + b + "." + c + ".2";
        String end   = a + "." + b + "." + c + ".254";

        return start + "-" + end;
    }
}
