package com.brahvim.esp_cam_stream_viewer;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.brahvim.esp_cam_stream_viewer.databinding.FragmentStreamControlsBinding;

@SuppressWarnings("deprecation")
public final class FragmentStreamControls extends Fragment {

	// region Fields.
	public static final String TAG = ApplicationEspCamStreamViewer.formTag(FragmentStreamControls.class);

	private ApplicationEspCamStreamViewer context;
	private FragmentStreamControlsBinding binding;
	private ThreadEspStream threadEspStream;
	private Activity activityHost;
	// endregion

	// region Fragment lifecycle callbacks.
	@Override
	public View onCreateView(final LayoutInflater p_inflater, @Nullable final ViewGroup p_viewGroup, final Bundle p_saveState) {
		if (p_viewGroup != null) {
			p_viewGroup.removeAllViews();
		}

		this.activityHost = super.getActivity();
		this.binding = FragmentStreamControlsBinding.inflate(p_inflater, p_viewGroup, false);
		this.context = (ApplicationEspCamStreamViewer) this.activityHost.getApplicationContext();

		this.addButtonCallbacks();
		this.threadEspStreamStart();

		return this.binding.getRoot();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		this.threadEspStreamStop();

		// Make these GC-able:
		this.activityHost = null;
		this.binding = null;
		this.context = null;
	}
	// endregion

	private void addButtonCallbacks() {
		// this.binding.buttonUp.setOnClickListener();
	}

	public void threadEspStreamStop() {
		if (this.threadEspStream == null)
			return;

		this.threadEspStream.shutdown();
		this.threadEspStream = null;
	}

	public void threadEspStreamStart() {
		this.threadEspStream = new ThreadEspStream(
		  this.binding.surfaceViewCamera.getHolder(),
		  this::threadEspStreamCbckCrash,
		  this.context.getClient(),
		  this.context.espIp
		);

		this.threadEspStream.start();
	}

	public void threadEspStreamCbckCrash() {
		this.activityHost.runOnUiThread(() -> {
			super.getFragmentManager()
				 .beginTransaction()
				 .replace(R.id.fragmentStream, new FragmentAwaitConnect())
				 .addToBackStack(null)
				 .commit();
		});
	}

}
