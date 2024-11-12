package com.brahvim.esp_cam_stream_viewer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.brahvim.esp_cam_stream_viewer.databinding.FragmentStreamControlsBinding;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@SuppressWarnings("deprecation")
public final class FragmentStreamControls extends Fragment {

	// region Fields.
	public static final String TAG = ApplicationEspCamStreamViewer.formTag(FragmentStreamControls.class);

	private OkHttpClient client;
	private Activity activityHost;
	private ExecutorService executorService;
	private ApplicationEspCamStreamViewer context;
	private FragmentStreamControlsBinding binding;
	private ThreadArpCacheMonitor threadArpCacheMonitor;
	private ThreadEspJpegStreamer threadEspJpegStreamer;
	// endregion

	// region Fragment lifecycle callbacks.
	@Override
	public View onCreateView(final LayoutInflater p_inflater, final ViewGroup p_viewGroup, final Bundle p_saveState) {
		if (p_viewGroup != null) {
			p_viewGroup.removeAllViews();
		}

		this.activityHost = super.getActivity();
		this.executorService = Executors.newFixedThreadPool(4);
		this.binding = FragmentStreamControlsBinding.inflate(p_inflater, p_viewGroup, false);
		this.context = (ApplicationEspCamStreamViewer) this.activityHost.getApplicationContext();

		this.client = this.context.getClient();

		this.threadArpCacheMonitorStart();
		this.addViewCallbacks();

		return this.binding.getRoot();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		this.runEspQueryOnExecutor("mode", "");

		this.threadEspStreamStop();
		this.threadArpCacheMonitorStop();
		this.executorService.shutdown();

		// Make these GC-able:
		this.threadArpCacheMonitor = null;
		this.threadEspJpegStreamer = null;
		this.executorService = null;
		this.activityHost = null;
		this.client = null;
		this.binding = null;
		this.context = null;
	}
	// endregion

	private void runEspQueryOnExecutor(final String p_param, final String p_paramValue) {
		this.executorService.submit(() -> this.runEspQuery("controls", p_param, p_paramValue));
	}

	private void runEspQuery(final String p_path, final String p_param, final String p_paramValue) {
		// try {
		final HttpUrl url = new HttpUrl.Builder()
		  .scheme("http")
		  .host(this.context.espIp)
		  .addPathSegment(p_path)
		  .addQueryParameter(p_param, p_paramValue)
		  .build();

		final Request request =
		  new Request.Builder()
			.url(url)
			.build();

		this.client.newCall(request) // .execute().close();
				   .enqueue(new Callback() {

					   @Override
					   public void onFailure(final Call p_call, final IOException p_exception) {
						   Log.e(TAG, String.format("Request to URL `%s` failed.", p_call.request()));
						   // FragmentStreamControls.this.client.newCall(request).enqueue(this);
					   }

					   @Override
					   public void onResponse(final Call p_call, final Response p_response) throws IOException {
						   Log.i(TAG, String.format("Request to URL `%s` successful.", p_response.request().url()));
						   p_response.close();
					   }

				   });
		// } catch (final IOException e) {
		// 	e.printStackTrace();
		// }
	}

	@SuppressLint("ClickableViewAccessibility")
	private void addViewCallbacks() {
		this.binding.buttonUp.setOnTouchListener((p_view, p_event) -> {

			switch (p_event.getAction()) {

				default: {
					return false;
				} // break;

				case MotionEvent.ACTION_DOWN: {
					Log.i(TAG, "Forwards gear...");
					this.runEspQueryOnExecutor("gear", "F");
					return true;
				} // break;

				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL: {
					Log.i(TAG, "Neutral gear...");
					this.runEspQueryOnExecutor("gear", "N");
					return true;
				} // break;

			}

		});

		this.binding.buttonDown.setOnTouchListener((p_view, p_event) -> {

			switch (p_event.getAction()) {

				default: {
					return false;
				} // break;

				case MotionEvent.ACTION_DOWN: {
					Log.i(TAG, "Backwards gear...");
					this.runEspQueryOnExecutor("gear", "B");
					return true;
				} // break;

				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL: {
					Log.i(TAG, "Neutral gear...");
					this.runEspQueryOnExecutor("gear", "N");
					return true;
				} // break;

			}

		});

		this.binding.seekBarSteering.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(final SeekBar p_seekBar, final int p_progress, final boolean p_fromUser) {
				FragmentStreamControls.this.runEspQueryOnExecutor("steer", Integer.toString(255 - p_progress)); // Invert it, haha!
			}

			@Override
			public void onStartTrackingTouch(final SeekBar p_seekBar) {
				//
			}

			@Override
			public void onStopTrackingTouch(final SeekBar p_seekBar) {
				//
			}

		});

	}

	public void threadEspStreamStop() {
		if (this.threadEspJpegStreamer == null)
			return;

		this.threadEspJpegStreamer.shutdown();
		this.threadEspJpegStreamer = null;
	}

	public void threadEspStreamStart() {

		// this.threadEspJpegStreamer = new ThreadEspJpegStreamer(
		//   this.binding.surfaceViewCamera.getHolder(),
		//   this::threadEspStreamCbckCrash,
		//   this.context.getClient(),
		//   this.context.espIp
		// );
		//
		// this.threadEspJpegStreamer.start();
	}

	// Called by crashing thread!

	public void threadEspStreamCbckCrash() {
		this.activityHost.runOnUiThread(() -> {
			// super.getFragmentManager()
			// 	 .beginTransaction()
			// 	 .replace(R.id.fragmentStream, new FragmentAwaitConnect())
			// 	 .addToBackStack(null)
			// 	 .commit();
			//
			// // Let the crashing thread actually shutdown:
			// this.threadEspStreamStop();

			this.threadEspStreamStop();
			this.threadEspStreamStart();
		});
	}

	@SuppressWarnings("deprecation")
	private void threadArpCacheMonitorCbckEspIpFound(final String p_ip) {
		super.getActivity().runOnUiThread(() -> {
			Log.i(TAG, String.format("ESP32-CAM IP found!: `%s`.", p_ip));
			this.context.espIp = p_ip;
			this.runEspQueryOnExecutor("mode", "");
			this.runEspQueryOnExecutor("gear", "N");
			this.runEspQueryOnExecutor("steer", "127");

			// super.getFragmentManager()
			// 	 .beginTransaction()
			// 	 .replace(R.id.fragmentStream, new FragmentStreamControls())
			// 	 .addToBackStack(null)
			// 	 .commit();

			this.threadArpCacheMonitorStop();
			this.threadEspStreamStart();
		});
	}

	// region ARP-cache monitor thread management methods.
	private void threadArpCacheMonitorStop() {
		if (this.threadArpCacheMonitor == null)
			return;

		this.threadArpCacheMonitor.shutdown();
		this.threadArpCacheMonitor = null;
	}

	private void threadArpCacheMonitorStart() {
		this.threadArpCacheMonitor = new ThreadArpCacheMonitor(this.context.getString(R.string.espMac)) {

			@Override
			protected void onIpFound(final String p_ip) {
				FragmentStreamControls.this.threadArpCacheMonitorCbckEspIpFound(p_ip);
			}

		};

		this.threadArpCacheMonitor.start();
	}
	// endregion

}
