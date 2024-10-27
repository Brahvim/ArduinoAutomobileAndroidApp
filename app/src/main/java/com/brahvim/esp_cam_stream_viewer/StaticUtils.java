package com.brahvim.esp_cam_stream_viewer;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public final class StaticUtils {

	private StaticUtils() throws IllegalAccessError {
		throw new IllegalAccessError();
	}

	public static NetworkInterface getHotspotItf() {
		try {

			final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				final NetworkInterface i = interfaces.nextElement();

				// It needs to be *not local*, and since it's Wi-Fi, *multicast:*
				if (!i.isLoopback() && i.supportsMulticast()) {
					final String interfaceName = i.getName();

					// Wi-Fi hotspot / "Access point"?
					if (interfaceName.startsWith("ap")) {
						return i;
					}
				}
			}

		} catch (SocketException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static NetworkInterface getWifiItf() {
		try {

			final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				final NetworkInterface i = interfaces.nextElement();

				// It needs to be *not local*, and since it's Wi-Fi, *multicast:*
				if (!i.isLoopback() && i.supportsMulticast()) {
					final String interfaceName = i.getName();

					// Wi-Fi?
					if (
					  interfaceName.startsWith("wifi") ||
					  interfaceName.startsWith("wlan") ||
					  interfaceName.startsWith("wl")
					) {
						return i;
					}
				}
			}

		} catch (SocketException e) {
			e.printStackTrace();
		}

		return null;
	}

}
