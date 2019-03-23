package me.jfenn.alarmio.fragments;

import android.animation.Animator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import me.jfenn.alarmio.R;
import me.jfenn.alarmio.views.AppIconView;

public class SplashFragment extends BaseFragment implements Animator.AnimatorListener {

    private boolean isFinished;
    private boolean isVisible;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash, container, false);
        AppIconView iconView = view.findViewById(R.id.icon);
        iconView.addListener(this);
        return view;
    }

    @Override
    public void onResume() {
        isVisible = true;
        if (isFinished)
            finish();
        super.onResume();
    }

    @Override
    public void onPause() {
        isVisible = false;
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        isVisible = false;
        super.onDestroyView();
    }

    @Override
    public void onAnimationStart(Animator animator) {

    }

    @Override
    public void onAnimationEnd(Animator animator) {
        isFinished = true;
        if (isVisible)
            finish();
    }

    @Override
    public void onAnimationCancel(Animator animator) {

    }

    @Override
    public void onAnimationRepeat(Animator animator) {

    }

    private void finish() {
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(getArguments());

        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment, fragment)
                .commit();
    }
}
