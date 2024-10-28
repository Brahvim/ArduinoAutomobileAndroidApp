package com.brahvim.esp_cam_stream_viewer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public final class ActivityMain extends Activity {

	private String ipEsp;
	private Context context;
	private ThreadMonitorArpCache arpCacheMonitor;

	// region `Activity` lifecycle methods.
	@Override
	protected void onCreate(final Bundle p_saveState) {
		super.onCreate(p_saveState);
		super.setContentView(R.layout.activity_main);

		arpCacheMonitorThreadCreate();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.context = null;
	}
	// endregion


	private void arpCacheMonitorThreadCreate() {
		this.arpCacheMonitor = new ThreadMonitorArpCache(this.context.getString(R.string.esp_mac)) {

			@Override
			protected void onMacFound(final String p_ip) {
				ActivityMain.this.arpCacheMonitorThreadDestroy();
				ActivityMain.this.ipEsp = p_ip;
			}

		};
	}

	private void arpCacheMonitorThreadDestroy() {
		try {
			this.arpCacheMonitor.join(1);
		} catch (final InterruptedException ie) {
			Thread.currentThread().interrupt();
		} finally {
			this.arpCacheMonitor = null;
		}
	}

}
