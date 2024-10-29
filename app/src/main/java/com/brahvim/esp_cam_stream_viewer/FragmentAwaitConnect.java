package com.brahvim.esp_cam_stream_viewer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brahvim.esp_cam_stream_viewer.databinding.FragmentAwaitConnectBinding;

@SuppressWarnings("deprecation")
public class FragmentAwaitConnect extends Fragment {

	private FragmentAwaitConnectBinding binding;

	@Override
	public View onCreateView(final LayoutInflater p_inflater, final ViewGroup p_viewGroup, final Bundle p_saveState) {
		this.binding = FragmentAwaitConnectBinding.inflate(p_inflater, p_viewGroup, false);
		return this.binding.getRoot();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		// Make stuff GCable as always...:
		this.binding = null;
	}

}
