package com.brahvim.esp_cam_stream_viewer;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;

public final class ActivityMain extends Activity {

	// region Fields.
	private static final String TAG = ApplicationEspCamStreamViewer.formTag(ActivityMain.class);

	// region Instance fields.
	private FragmentManager fragmentManager;
	private ThreadMonitorArpCache arpCacheMonitor;
	private ApplicationEspCamStreamViewer context;
	// endregion
	// endregion

	// region `Activity` lifecycle methods.
	@Override
	protected void onCreate(final Bundle p_saveState) {
		super.onCreate(p_saveState);
		super.setContentView(R.layout.activity_main);

		this.context = (ApplicationEspCamStreamViewer) super.getApplicationContext();
		this.fragmentManager = super.getFragmentManager();
		this.arpCacheMonitorThreadCreate();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.arpCacheMonitorThreadDestroy();

		this.fragmentManager = null;
		this.context = null;
	}
	// endregion

	@SuppressWarnings("deprecation")
	private void espIpFound(final String p_ip) {
		Log.i(TAG, String.format("ESP IP found: `%s`.", p_ip));
		this.context.espIp = p_ip;

		final FragmentStreamOnly fragmentCurrent = (FragmentStreamOnly) this.fragmentManager.findFragmentById(R.id.fragmentStream);
		fragmentCurrent.threadStreamStart();

		// super.runOnUiThread(() -> {
		// 	final Fragment fragmentNext = new FragmentStreamOnly();
		// 	this.fragmentManager.beginTransaction()
		// 						.replace(R.id.fragmentStream, fragmentNext)
		// 						.commit();
		// });

		this.arpCacheMonitorThreadDestroy();
	}

	private void arpCacheMonitorThreadCreate() {
		this.arpCacheMonitor = new ThreadMonitorArpCache(this.context.getString(R.string.esp_mac)) {

			@Override
			protected void onIpFound(final String p_ip) {
				ActivityMain.this.espIpFound(p_ip);
			}

		};

		this.arpCacheMonitor.start();
	}


	private void arpCacheMonitorThreadDestroy() {
		if (this.arpCacheMonitor == null)
			return;

		this.arpCacheMonitor.shutdown();
		this.arpCacheMonitor = null;
	}

}
