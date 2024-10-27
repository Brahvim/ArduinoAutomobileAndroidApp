package com.brahvim.esp_cam_stream_viewer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.brahvim.esp_cam_stream_viewer.databinding.FragmentStreamOnlyBinding;

@SuppressWarnings("deprecation")
public final class FragmentStreamOnly extends Fragment {

	private FragmentStreamOnlyBinding binding;

	@Override
	public View onCreateView(final LayoutInflater p_inflater, @Nullable final ViewGroup p_viewGroup, final Bundle p_saveState) {
		this.binding = FragmentStreamOnlyBinding.inflate(p_inflater, p_viewGroup, false);
		return this.binding.getRoot();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		this.binding = null; // Make it GC-able.
	}

}
