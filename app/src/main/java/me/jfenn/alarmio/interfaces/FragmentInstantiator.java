package me.jfenn.alarmio.interfaces;

import androidx.annotation.Nullable;

import me.jfenn.alarmio.ui.fragments.BasePagerFragment;

public interface FragmentInstantiator {
    @Nullable
    BasePagerFragment newInstance(int position);
    @Nullable
    String getTitle(int position);
}
