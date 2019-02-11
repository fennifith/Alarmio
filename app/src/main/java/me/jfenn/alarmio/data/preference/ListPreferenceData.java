package me.jfenn.alarmio.data.preference;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.aesthetic.Aesthetic;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.functions.Consumer;
import me.jfenn.alarmio.R;
import me.jfenn.alarmio.data.PreferenceData;

public abstract class ListPreferenceData extends BasePreferenceData<ListPreferenceData.ViewHolder> {

    private PreferenceData preference;
    private int title;

    public ListPreferenceData(PreferenceData preference, int title) {
        this.preference = preference;
        this.title = title;
    }

    abstract RecyclerView.Adapter getAdapter(Context context, String[] items);

    abstract void requestAddItem(ViewHolder holder);

    @Override
    public ViewHolder getViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_preference_list, parent, false));
    }

    @Override
    public void bindViewHolder(final ViewHolder holder) {
        String[] items = getItems(holder.getContext());

        holder.title.setText(title);
        holder.recycler.setLayoutManager(new LinearLayoutManager(holder.getContext()));
        holder.recycler.setAdapter(getAdapter(holder.getContext(), items));

        holder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestAddItem(holder);
            }
        });

        holder.remove.setVisibility(items.length > 1 ? View.VISIBLE : View.GONE);
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem(holder);
            }
        });

        Aesthetic.Companion.get()
                .textColorPrimary()
                .take(1)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        holder.add.setColorFilter(new PorterDuffColorFilter(integer, PorterDuff.Mode.SRC_IN));
                        holder.remove.setColorFilter(new PorterDuffColorFilter(integer, PorterDuff.Mode.SRC_IN));
                    }
                });
    }

    public final String[] getItems(Context context) {
        return preference.getValue(context);
    }

    /**
     * adds an item to the end of the list
     *
     * @param holder the ViewHolder containing the RecyclerView
     * @param item   the item to add
     */
    public final void addItem(ViewHolder holder, String item) {
        String[] items = getItems(holder.getContext());
        String[] newItems = new String[items.length + 1];
        System.arraycopy(items, 0, newItems, 0, items.length);
        newItems[items.length] = item;

        preference.setValue(holder.getContext(), newItems);
        bindViewHolder(holder);
    }

    /**
     * Removes the last item in the list
     *
     * @param holder the ViewHolder containing the RecyclerView
     */
    public final void removeItem(ViewHolder holder) {
        String[] items = getItems(holder.getContext());
        if (items.length > 1) {
            String[] newItems = new String[items.length - 1];
            System.arraycopy(items, 0, newItems, 0, items.length - 1);

            preference.setValue(holder.getContext(), newItems);
            bindViewHolder(holder);
        }
    }

    public static class ViewHolder extends BasePreferenceData.ViewHolder {

        private TextView title;
        private RecyclerView recycler;
        private ImageView add, remove;

        public ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            recycler = v.findViewById(R.id.recycler);
            add = v.findViewById(R.id.add);
            remove = v.findViewById(R.id.remove);
        }
    }


}
