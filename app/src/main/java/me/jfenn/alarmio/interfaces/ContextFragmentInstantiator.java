package me.jfenn.alarmio.interfaces;

import android.content.Context;

import java.lang.ref.WeakReference;

import androidx.annotation.Nullable;

public abstract class ContextFragmentInstantiator implements FragmentInstantiator {

    private WeakReference<Context> context;

    public ContextFragmentInstantiator(Context context) {
        this.context = new WeakReference<>(context);
    }

    @Override
    @Nullable
    public String getTitle(int position) {
        Context context = this.context.get();
        if (context != null)
            return getTitle(context, position);
        else return null;
    }

    public abstract String getTitle(Context context, int position);
}
