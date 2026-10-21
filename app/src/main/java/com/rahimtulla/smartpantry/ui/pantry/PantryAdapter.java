package com.rahimtulla.smartpantry.ui.pantry;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.rahimtulla.smartpantry.R;
import com.rahimtulla.smartpantry.data.PantryItem;
import com.rahimtulla.smartpantry.util.Formatters;

import java.util.Objects;

/**
 * Custom RecyclerView adapter for the pantry list. ListAdapter + DiffUtil work out which rows changed
 * when Room delivers a new list, so only those rows are redrawn (with animations) instead of the whole list.
 */
public class PantryAdapter extends ListAdapter<PantryItem, PantryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
    }

    private final OnItemClickListener listener;
    private long todayEpochDay = Formatters.todayEpochDay();
    private boolean highlightExpiring = true;
    private int expiryWindowDays = 3;

    public PantryAdapter(OnItemClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    /** Applies the current settings; called from the fragment's onResume. */
    public void setExpirySettings(long todayEpochDay, boolean highlightExpiring, int expiryWindowDays) {
        this.todayEpochDay = todayEpochDay;
        this.highlightExpiring = highlightExpiring;
        this.expiryWindowDays = expiryWindowDays;
        notifyDataSetChanged();
    }

    public PantryItem getItemAt(int position) {
        return getItem(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PantryItem item = getItem(position);
        Context context = holder.itemView.getContext();

        holder.name.setText(item.name);
        holder.quantity.setText(Formatters.amount(item.quantity, item.unit));
        holder.card.setOnClickListener(v -> listener.onItemClick(item));

        // Default (neutral) styling - rows are recycled, so always reset before applying a status colour.
        int neutralText = MaterialColors.getColor(holder.card, com.google.android.material.R.attr.colorOnSurfaceVariant);
        int cardBackground = MaterialColors.getColor(holder.card, com.google.android.material.R.attr.colorSurface);
        int expiryText = neutralText;

        if (item.expiryEpochDay == null) {
            holder.expiry.setText(R.string.no_expiry);
        } else {
            long daysLeft = item.expiryEpochDay - todayEpochDay;
            String date = Formatters.date(item.expiryEpochDay);
            if (daysLeft < 0) {
                holder.expiry.setText(context.getString(R.string.expired_on, date));
                expiryText = ContextCompat.getColor(context, R.color.status_expired_text);
                if (highlightExpiring) {
                    cardBackground = ContextCompat.getColor(context, R.color.status_expired_bg);
                }
            } else if (daysLeft == 0) {
                holder.expiry.setText(R.string.expires_today);
                expiryText = ContextCompat.getColor(context, R.color.status_soon_text);
                if (highlightExpiring) {
                    cardBackground = ContextCompat.getColor(context, R.color.status_soon_bg);
                }
            } else if (highlightExpiring && daysLeft <= expiryWindowDays) {
                holder.expiry.setText(context.getResources()
                        .getQuantityString(R.plurals.expires_in_days, (int) daysLeft, (int) daysLeft));
                expiryText = ContextCompat.getColor(context, R.color.status_soon_text);
                cardBackground = ContextCompat.getColor(context, R.color.status_soon_bg);
            } else {
                holder.expiry.setText(context.getString(R.string.expires_on, date));
            }
        }

        holder.card.setCardBackgroundColor(cardBackground);
        holder.expiry.setTextColor(expiryText);
        TextViewCompat.setCompoundDrawableTintList(holder.expiry, ColorStateList.valueOf(expiryText));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final TextView name;
        final TextView quantity;
        final TextView expiry;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            name = itemView.findViewById(R.id.item_name);
            quantity = itemView.findViewById(R.id.item_quantity);
            expiry = itemView.findViewById(R.id.item_expiry);
        }
    }

    private static final DiffUtil.ItemCallback<PantryItem> DIFF = new DiffUtil.ItemCallback<PantryItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull PantryItem a, @NonNull PantryItem b) {
            return a.id == b.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull PantryItem a, @NonNull PantryItem b) {
            return a.name.equals(b.name)
                    && a.quantity == b.quantity
                    && a.unit.equals(b.unit)
                    && Objects.equals(a.expiryEpochDay, b.expiryEpochDay);
        }
    };
}
