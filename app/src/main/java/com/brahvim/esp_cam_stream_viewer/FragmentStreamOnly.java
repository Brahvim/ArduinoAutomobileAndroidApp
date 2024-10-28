package com.brahvim.esp_cam_stream_viewer;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.brahvim.esp_cam_stream_viewer.databinding.FragmentStreamOnlyBinding;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@SuppressWarnings("deprecation")
public final class FragmentStreamOnly extends Fragment {

	private Thread threadStream;

	private Activity activityHost;

	private FragmentStreamOnlyBinding binding;

	@Override
	public View onCreateView(final LayoutInflater p_inflater, @Nullable final ViewGroup p_viewGroup, final Bundle p_saveState) {
		this.activityHost = super.getActivity();

		this.threadStreamStart();
		this.binding = FragmentStreamOnlyBinding.inflate(p_inflater, p_viewGroup, false);
		return this.binding.getRoot();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		this.threadStreamStop();

		// Make these GC-able:
		this.activityHost = null;
		this.binding = null;
	}

	private void threadStreamStop() {
		try {
			this.threadStream.join();
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			this.threadStream = null;
		}
	}

	private void threadStreamStart() {
		this.threadStream = new Thread(this::threadStreamRoutine, "App:JpegStreamTransfer");
		this.threadStream.start();
	}

	private void threadStreamRoutine() {
		try {
			final OkHttpClient client = new OkHttpClient();
			final Request request = new Request.Builder()
			  .url("http://" + "" + "/stream")
			  .build();

			try (final Response response = client.newCall(request).execute()) {

				if (response.isSuccessful() && response.body() != null) {
					byte[] imageBytes = response.body().bytes();

					final Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
					this.getActivity().runOnUiThread(() -> {
						final Canvas c = this.binding.textureViewCamera.lockCanvas();
						c.drawBitmap(bitmap, 0, 0, null);
						this.binding.textureViewCamera.unlockCanvasAndPost(c);
					});
				}

			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
