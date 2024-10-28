package com.brahvim.esp_cam_stream_viewer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public abstract class ThreadMonitorArpCache extends Thread {

	// region Fields.
	public static final long LOOP_INTERVAL_LOW = 1000;
	public static final long LOOP_INTERVAL_FASTEST = 0;
	public static final long LOOP_INTERVAL_DAEMON = 5000;

	// region Instance fields.
	public long loopInterval = ThreadMonitorArpCache.LOOP_INTERVAL_DAEMON;

	public final String macToFind;

	private final ArrayList<String> arpLines = new ArrayList<>();
	// endregion
	// endregion

	protected ThreadMonitorArpCache(final String p_macToFind) {
		super.setName("App:ArpMonitor");
		this.macToFind = p_macToFind;
	}

	@Override
	public void run() {
		while (true) {
			try {
				this.readArpCacheFile();
				Thread.sleep(this.loopInterval);
			} catch (final InterruptedException ie) {
				super.interrupt();
			}
		}
	}

	protected abstract void onMacFound(final String ip);

	private void readArpCacheFile() {
		try (final BufferedReader reader = new BufferedReader(new FileReader("/proc/net/arp"))) {

			final String header = reader.readLine();
			final int idsMacsStart = header.indexOf("HW address");

			for (String s; (s = reader.readLine()) != null; ) {
				this.arpLines.add(s);
			}

			for (final String s : this.arpLines) {
				final String mac = s.substring(idsMacsStart, s.indexOf(' ', idsMacsStart));

				if (this.macToFind.equals(mac)) {
					this.arpLines.clear();
					this.onMacFound(s.substring(0, s.indexOf(' ')));
				}
			}

		} catch (final IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
