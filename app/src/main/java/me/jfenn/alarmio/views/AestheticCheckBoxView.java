package me.jfenn.alarmio.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;

import com.afollestad.aesthetic.Aesthetic;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.widget.CompoundButtonCompat;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import me.jfenn.alarmio.interfaces.Subscribblable;

public class AestheticCheckBoxView extends AppCompatCheckBox implements Subscribblable {

    private Disposable colorAccentSubscription;
    private Disposable textColorPrimarySubscription;

    public AestheticCheckBoxView(Context context) {
        super(context);
        subscribe();
    }

    public AestheticCheckBoxView(Context context, AttributeSet attrs) {
        super(context, attrs);
        subscribe();
    }

    public AestheticCheckBoxView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        subscribe();
    }

    @Override
    public void subscribe() {
        colorAccentSubscription = Aesthetic.Companion.get().colorAccent()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        int[][] states = new int[][]{new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked}};

                        ColorStateList colorStateList = new ColorStateList(
                                states,
                                new int[]{
                                        Color.argb(255, 128, 128, 128),
                                        integer
                                }
                        );

                        CompoundButtonCompat.setButtonTintList(AestheticCheckBoxView.this, colorStateList);
                    }
                });

        textColorPrimarySubscription = Aesthetic.Companion.get().textColorPrimary()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        setTextColor(integer);
                    }
                });
    }

    @Override
    public void unsubscribe() {
        colorAccentSubscription.dispose();
        textColorPrimarySubscription.dispose();
    }
}
