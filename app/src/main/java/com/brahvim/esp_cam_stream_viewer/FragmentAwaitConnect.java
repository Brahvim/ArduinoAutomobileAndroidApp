package com.brahvim.esp_cam_stream_viewer;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brahvim.esp_cam_stream_viewer.databinding.FragmentAwaitConnectBinding;

@SuppressWarnings("deprecation")
public class FragmentAwaitConnect extends Fragment {

	// region Fields.
	public static final String TAG = ApplicationEspCamStreamViewer.formTag(FragmentAwaitConnect.class);

	private FragmentAwaitConnectBinding binding;
	private ApplicationEspCamStreamViewer context;
	private ThreadArpCacheMonitor threadArpCacheMonitor;
	// endregion

	// region `Fragment`-lifecycle callbacks.
	@Override
	public View onCreateView(final LayoutInflater p_inflater, final ViewGroup p_viewGroup, final Bundle p_saveState) {
		if (p_viewGroup != null) {
			p_viewGroup.removeAllViews();
		}

		this.context = (ApplicationEspCamStreamViewer) super.getActivity().getApplicationContext();
		this.binding = FragmentAwaitConnectBinding.inflate(p_inflater, p_viewGroup, false);

		this.threadArpCacheMonitorStart();

		return this.binding.getRoot();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		this.threadArpCacheMonitorStop();

		this.binding = null;
		this.context = null;
	}
	// endregion

	@SuppressWarnings("deprecation")
	private void threadArpCacheMonitorCbckEspIpFound(final String p_ip) {
		super.getActivity().runOnUiThread(() -> {
			Log.i(TAG, String.format("ESP IP found: `%s`.", p_ip));
			this.context.espIp = p_ip;

			super.getFragmentManager()
				 .beginTransaction()
				 .replace(R.id.fragmentStream, new FragmentStreamControls())
				 .addToBackStack(null)
				 .commit();

			this.threadArpCacheMonitorStop();
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
				FragmentAwaitConnect.this.threadArpCacheMonitorCbckEspIpFound(p_ip);
			}

		};

		this.threadArpCacheMonitor.start();
	}
	// endregion

}
