package at.innoc.roboat.radar.helpers;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration; 

public class IpHelper { 

	public static Object[] getMachineIp() {
        try {
            NetworkInterface defaultInterface = getDefaultNetworkInterface();
            if (defaultInterface != null) {
				return new Object[]{defaultInterface.getInetAddresses().nextElement().getHostAddress(), null};
            } else {
                return new Object[]{null, "No default network interface found."};
            }
        } catch (Exception e) {
            return new Object[]{null, e.toString()};
        }
    }

    private static NetworkInterface getDefaultNetworkInterface() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                return networkInterface;
            }
        }
        return null;
    }

} 
	