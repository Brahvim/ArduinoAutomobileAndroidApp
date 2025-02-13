package com.brahvim.esp_cam_stream_viewer;

import android.app.Application;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;

public class ApplicationEspCamStreamViewer extends Application {

	public String espIp;

	private OkHttpClient client;

	@NonNull
	public static String formTag(final Class<?> p_class) {
		return "Esp:" + p_class.getSimpleName();
	}

	@NonNull
	public static OkHttpClient createClient() {
		return new OkHttpClient.Builder()
		  .protocols(Collections.singletonList(Protocol.HTTP_1_1))
		  .writeTimeout(120, TimeUnit.MILLISECONDS)
		  .readTimeout(201, TimeUnit.MILLISECONDS)
		  .connectTimeout(5, TimeUnit.SECONDS)
		  .retryOnConnectionFailure(false)
		  .build();
	}

	@NonNull
	public OkHttpClient getClient() {
		final Object clientWaiter = new Object();

		if (this.client == null) {
			new Thread(() -> {

				synchronized (clientWaiter) {
					this.client = ApplicationEspCamStreamViewer.createClient();
					clientWaiter.notifyAll();
				}

			}, "EspCamStreamViewer:OkHttpClientCreator"
			).start();
		}

		boolean retry = true;

		synchronized (clientWaiter) {
			while (retry) {
				try {
					clientWaiter.wait();
					retry = false;
				} catch (final InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
			}
		}

		return this.client;
	}

}
