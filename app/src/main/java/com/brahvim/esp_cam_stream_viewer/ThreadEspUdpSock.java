package com.brahvim.esp_cam_stream_viewer;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public final class ThreadEspUdpSock extends Thread {

	private DatagramSocket socket;

	public ThreadEspUdpSock() {
	}

	public void createEspSocket(final InetAddress p_ip) {
		try {
			this.socket = new DatagramSocket(8080, p_ip);
		} catch (final SocketException se) {
			se.printStackTrace();
		}
	}

}
