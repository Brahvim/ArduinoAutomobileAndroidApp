package com.brahvim.esp_cam_stream_viewer;

import android.app.Application;

import okhttp3.OkHttpClient;

public class ApplicationEspCamStreamViewer extends Application {

	public String espIp;
	public OkHttpClient client;

	public static String formTag(final Class<?> p_class) {
		return "EspCamStreamViewer:" + p_class.getSimpleName();
	}

}
