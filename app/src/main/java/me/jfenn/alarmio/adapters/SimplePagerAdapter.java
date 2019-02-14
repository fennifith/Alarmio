package me.jfenn.alarmio.adapters;

import android.content.Context;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import me.jfenn.alarmio.fragments.BasePagerFragment;

public class SimplePagerAdapter extends FragmentStatePagerAdapter {

    private Context context;
    private BasePagerFragment[] fragments;

    public SimplePagerAdapter(Context context, FragmentManager fm, BasePagerFragment... fragments) {
        super(fm);
        this.context = context;
        this.fragments = fragments;
    }

    @Override
    public BasePagerFragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragments[position].getTitle(context);
    }

    @Override
    public int getCount() {
        return fragments.length;
    }
}
