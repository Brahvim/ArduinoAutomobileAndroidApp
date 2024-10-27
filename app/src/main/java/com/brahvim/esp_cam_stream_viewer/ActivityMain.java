package com.brahvim.esp_cam_stream_viewer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public final class ActivityMain extends Activity {

	private Context context;
	private ThreadMonitorArpCache arpCacheMonitor;

	// region `Activity` lifecycle methods.
	@Override
	protected void onCreate(final Bundle p_saveState) {
		super.onCreate(p_saveState);
		super.setContentView(R.layout.activity_main);

		this.arpCacheMonitor = new ThreadMonitorArpCache(this.context.getString(R.string.esp_mac)) {

			@Override
			protected void onMacFound() {
				try {
					ActivityMain.this.arpCacheMonitor.join(1);
					ActivityMain.this.arpCacheMonitor = null;
				} catch (final InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
			}

		};
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.context = null;
	}
	// endregion

}
