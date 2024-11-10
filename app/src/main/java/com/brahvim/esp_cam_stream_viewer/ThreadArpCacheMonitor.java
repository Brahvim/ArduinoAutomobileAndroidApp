package com.brahvim.esp_cam_stream_viewer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ThreadArpCacheMonitor extends Thread {

	// region Fields.
	public static final long LOOP_INTERVAL_LOW = 1000;
	public static final long LOOP_INTERVAL_FASTEST = 0;
	public static final long LOOP_INTERVAL_DAEMON = 5000;

	// region Instance fields.
	public final String macToFind;
	public long loopInterval = ThreadArpCacheMonitor.LOOP_INTERVAL_DAEMON;

	private final ArrayList<String> arpLines = new ArrayList<>();
	private final AtomicBoolean shouldRun = new AtomicBoolean(true);
	// endregion
	// endregion

	protected ThreadArpCacheMonitor(final String p_macToFind) {
		super.setName("EspCamStreamViewer:ArpMonitor");
		this.macToFind = p_macToFind;
	}

	@Override
	public void run() {
		while (this.shouldRun.get()) {
			try {
				this.readArpCacheFile();
				Thread.sleep(this.loopInterval);
			} catch (final InterruptedException ie) {
				super.interrupt();
			}
		}
	}

	public void shutdown() {
		this.shouldRun.set(false);

		boolean retry = true;
		while (retry) {
			try {
				super.join();
				retry = false;
			} catch (final InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}
	}

	protected abstract void onIpFound(final String ip);

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
					this.onIpFound(s.substring(0, s.indexOf(' ')));
				}
			}

		} catch (final IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
