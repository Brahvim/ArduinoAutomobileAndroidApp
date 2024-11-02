package com.brahvim.esp_cam_stream_viewer;

import android.app.Activity;
import android.os.Bundle;

public final class ActivityMain extends Activity {

	public static final String TAG = ApplicationEspCamStreamViewer.formTag(ActivityMain.class);

	@Override
	protected void onCreate(final Bundle p_saveState) {
		super.onCreate(p_saveState);
		super.setContentView(R.layout.activity_main);
	}

}
