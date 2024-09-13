package project.server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class IPAddressUtil {

    public static String getIPv4Address() {
        return getIPAddress(true);
    }

    public static String getIPv6Address() {
        return getIPAddress(false);
    }

    private static String getIPAddress(boolean useIPv4) {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ipAddress = inetAddress.getHostAddress();
                        boolean isIPv4 = ipAddress.indexOf(':') < 0;
                        if (useIPv4) {
                            if (isIPv4) {
                                return ipAddress;
                            }
                        } else {
                            if (!isIPv4) {
                                int delimiter = ipAddress.indexOf('%'); // Drop IP6 zone suffix
                                return delimiter < 0 ? ipAddress : ipAddress.substring(0, delimiter);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        String ipv4Address = getIPv4Address();
        String ipv6Address = getIPv6Address();
        System.out.println("IPv4 Address: " + (ipv4Address != null ? ipv4Address : "Not Available"));
        System.out.println("IPv6 Address: " + (ipv6Address != null ? ipv6Address : "Not Available"));
    }
}
