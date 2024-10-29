package com.brahvim.esp_cam_stream_viewer;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.brahvim.esp_cam_stream_viewer.databinding.FragmentStreamOnlyBinding;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

@SuppressWarnings("deprecation")
public final class FragmentStreamOnly extends Fragment {

	public static class SurfaceHolderCallbackEspStream implements SurfaceHolder.Callback {

		@Override
		public void surfaceCreated(@NonNull final SurfaceHolder p_surfaceHolder) {
			// this.threadDraw = new Thread(() -> {
			//
			// 	while (!Thread.interrupted()) {
			//
			// 	}
			// }, "Esp32CamStreamViewer:SurfaceHolderCallbackEspStream");
		}

		@Override
		public void surfaceDestroyed(@NonNull final SurfaceHolder p_surfaceHolder) {
			// boolean retry = true;
			//
			// while (retry) {
			// 	try {
			// 		 this.threadDraw.join();
			// 		 retry = false;
			// 	} catch (final InterruptedException ie) {
			// 		 Thread.currentThread().interrupt();
			// 	}
			// }
		}

		@Override
		public void surfaceChanged(@NonNull final SurfaceHolder p_surfaceHolder, final int p_format, final int p_width, final int p_height) {
			// Log.i(TAG, "Surface size changed!");
		}

	}

	// region Fields.
	private static final String TAG = ApplicationEspCamStreamViewer.formTag(FragmentStreamOnly.class);
	private final AtomicBoolean threadStreamShouldRun = new AtomicBoolean(true);
	private SurfaceHolderCallbackEspStream surfaceHolderCallbackEspStream;
	private ApplicationEspCamStreamViewer context;
	private FragmentStreamOnlyBinding binding;
	private SurfaceHolder surfaceHolder;
	private Activity activityHost;
	private OkHttpClient client;
	// endregion
	private Thread threadStream;

	// region Fragment lifecycle callbacks.
	@Override
	public View onCreateView(final LayoutInflater p_inflater, @Nullable final ViewGroup p_viewGroup, final Bundle p_saveState) {
		this.binding = FragmentStreamOnlyBinding.inflate(p_inflater, p_viewGroup, false);

		this.activityHost = super.getActivity();
		this.context = (ApplicationEspCamStreamViewer) this.activityHost.getApplicationContext();

		this.surfaceHolder = this.binding.surfaceViewCamera.getHolder();
		this.surfaceHolderCallbackEspStream = new SurfaceHolderCallbackEspStream();
		this.surfaceHolder.addCallback(this.surfaceHolderCallbackEspStream);

		return this.binding.getRoot();
	}
	// endregion

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		this.threadStreamStop();

		// Make these GC-able:
		this.surfaceHolderCallbackEspStream = null;
		this.activityHost = null;
		this.binding = null;
		this.context = null;
	}

	public void threadStreamStop() {
		boolean retry = true;

		while (retry) {
			try {
				this.threadStreamShouldRun.set(true);
				this.threadStream.join();
				retry = false;
			} catch (final InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}

		this.threadStream = null;
	}

	public void threadStreamStart() {
		this.threadStream = new Thread(() -> {

			Log.i(TAG, "Started streaming thread...");

			this.context.client = new OkHttpClient.Builder()
			  .protocols(Collections.singletonList(Protocol.HTTP_1_1))
			  .readTimeout(120, TimeUnit.MILLISECONDS)
			  .connectTimeout(5, TimeUnit.SECONDS)
			  .writeTimeout(5, TimeUnit.SECONDS)
			  .build();

			this.client = this.context.client;

			while (this.threadStreamShouldRun.get())
				this.threadStreamRoutine();

			Log.i(TAG, "STREAMING THREAD EXITED.");

		}, "Esp32CamStreamViewer:JpegStreamTransfer");
		this.threadStream.start();
	}

	private void threadStreamRoutine() {
		final Request request = new Request.Builder()
		  .url("http://" + this.context.espIp + ":81/stream")
		  .build();

		Log.i(TAG, "Requesting `/stream`.");

		try (final Response response = this.client.newCall(request).execute()) {

			if (response.isSuccessful() && response.body() != null) {
				Log.i(TAG, "Response OK! Attempting to fetch...");

				// HTTP packets are CRLF strings (typewriter-style strings) that use `\r\n` for new lines.
				// On the AI-Thinker ESP32-CAM side, in `app_httpd.cpp`, line 100 we have:

				/*
					```cpp
					#define PART_BOUNDARY "123456789000000000000987654321"
					static const char *_STREAM_BOUNDARY = "\r\n--" PART_BOUNDARY "\r\n";
					static const char *_STREAM_CONTENT_TYPE = "multipart/x-mixed-replace;boundary=" PART_BOUNDARY;
					static const char *_STREAM_PART = "Content-Type: image/jpeg\r\nContent-Length: %u\r\nX-Timestamp: %d.%06d\r\n\r\n";
					```
				 */

				// Function `static esp_err_t stream_handler(httpd_req_t *req)` defined on line 500 of the same file,
				// is the `httpd_uri_t::handler` callback for the `/stream` endpoint.
				// On line 506, this function declares `char *part_buf[128]`.
				// Lines 702 to 712 are:

				/*
				```cpp
				if (res == ESP_OK) {
					res = httpd_resp_send_chunk(req, _STREAM_BOUNDARY, strlen(_STREAM_BOUNDARY));
				}
				if (res == ESP_OK) {
					size_t hlen =
						snprintf((char *)part_buf, 128, _STREAM_PART, _jpg_buf_len, _timestamp.tv_sec, _timestamp.tv_usec);
					res = httpd_resp_send_chunk(req, (const char *)part_buf, hlen);
				}
				if (res == ESP_OK) {
					res = httpd_resp_send_chunk(req, (const char *)_jpg_buf, _jpg_buf_len);
				}
				```
				 */

				// Thus, *in the form of a string literal*, we get packets like:

				/*
				```
					\r\n
					--123456789000000000000987654321\r\n
					Content-Type: image/jpeg\r\n
					Content-Length: 6062\r\n
					X-Timestamp: 448.000000\r\n
					����??JFIF????????`
				```

				(Last line is only a part of the `6062`-byte image payload.)
				 */

				// Now that we know how and why we're getting these packets, we can parse them!
				// This code's aim is to read the packet till it has seen `\r\n` 5 times.
				// Because, *by then*, it'd have reached the end of the `X-Timestamp: 448.000000\r\n` line. End of the header!

				// Finally, we just parse out the `Content-Length` HTTP parameter's value, and then go on to read the payload.

				// region Parsing HTTP response...
				int bytesLastFewCounter = 0;
				int semaphoreSeeCrlfBytes = 5; // Believe it or not, I got this number right *first-try*.
				final byte[] bytesLastFew = new byte[2];
				final ArrayList<Byte> imageBytesList = new ArrayList<>(0);
				final ArrayList<Byte> headerBytesList = new ArrayList<>(128); // `108` bytes at least.

				// TODO: Corporate wants you to optimize this code by reading the entire stream first, parsing second.
				// 		MOAR CPU-FRIENDLINESS!!!

				try (final InputStream inputStream = response.body().byteStream()) {

					byte i;
					while (semaphoreSeeCrlfBytes > 0) {
						i = (byte) inputStream.read();

						headerBytesList.add(i);
						bytesLastFew[bytesLastFewCounter] = i;
						++bytesLastFewCounter;

						if (bytesLastFewCounter >= bytesLastFew.length)
							bytesLastFewCounter = 0;

						if ((bytesLastFew[0] == '\r' && bytesLastFew[1] == '\n'))
							--semaphoreSeeCrlfBytes;
					}

					final byte[] headerBytesArray = new byte[headerBytesList.size()];
					for (int j = 0; j < headerBytesArray.length; ++j) {
						headerBytesArray[j] = headerBytesList.get(j);
					}

					final String headerString = new String(headerBytesArray, 0, headerBytesList.size(), StandardCharsets.US_ASCII);
					final String[] headerStringSplit = headerString.split("\r\n");
					final String paramStringContentLength = headerStringSplit[3]; // Got this number *right* first time too, somehow! (Read `String::split()` docs).

					final int paramStringContentLengthColonId = paramStringContentLength.indexOf(':');
					final int paramStringContentLengthValueStartId = 2 + paramStringContentLengthColonId; // Also the `2` here. This one was obvious.
					// What did I *not* get right the first time? *Which `\r\n` to stop parsing at*.
					// I was going to parse till the end of the line with `Content-Length` first, but then decided to revert to reading the entire packet.

					final String valueStringContentLength = paramStringContentLength.substring(paramStringContentLengthValueStartId);
					final int valueContentLength = Integer.parseInt(valueStringContentLength);

					// Log.i(TAG, String.format("Parameter value: `%d`.", valueContentLength));
					imageBytesList.ensureCapacity(valueContentLength);

					// noinspection ResultOfMethodCallIgnored
					inputStream.skip(2); // Skip the next pair of `\r\n`! Also, // NOSONAR!

					for (int j = 0; j < valueContentLength; ++j) {
						imageBytesList.add((byte) inputStream.read());
					}

				} catch (final IOException e) {
					Log.e(TAG, "Exception in attempting to fetch `/stream` via an `InputStream` :(");
				}
				// endregion

				// Unboxing `Byte` objects from the heap without a `reinterpret_cast<T>()` (this should be in native code or something!):
				final int imageBytesListSize = imageBytesList.size();
				final byte[] imageBytesArray = new byte[imageBytesListSize];

				for (int i = 0; i < imageBytesListSize; ++i)
					imageBytesArray[i] = imageBytesList.get(i);

				Log.i(TAG, String.format("Got image data. Array size: `%d` bytes.", imageBytesListSize));

				final Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytesArray, 0, imageBytesListSize);

				if (bitmap == null)
					return;

				Log.i(TAG, "Decoded image.");

				// if (this.surfaceHolder.isCreating()) // Still wondering if we need this, because...
				// 	return;

				final Canvas canvas = this.surfaceHolder.lockCanvas(); // ...because this can be `null`! If that check fails!

				if (canvas != null) {
					canvas.drawBitmap(bitmap, 0, 0, null);
					this.surfaceHolder.unlockCanvasAndPost(canvas);
					Log.i(TAG, "Frame drawn!");
				}

				bitmap.recycle();

			} else {
				Log.i(TAG, "Illegal response received!");
			}

		} catch (
		  final IOException e) {
			Log.e(TAG, "Exception in attempting to fetch `/stream` :(");
			e.printStackTrace();
		} finally {
			this.client.connectionPool().evictAll();
		}
	}

}
