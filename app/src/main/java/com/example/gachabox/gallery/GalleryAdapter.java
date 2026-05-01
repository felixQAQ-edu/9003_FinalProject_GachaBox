package com.example.gachabox.gallery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.gachabox.R;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter used to bind gallery card data to the RecyclerView.
 *
 * This adapter is responsible for:
 * - showing unlocked cards with their real image, name, and rarity
 * - showing locked cards with the locked overlay and placeholder text
 *
 * TODO Interface 8:
 * The itemList currently comes from static mock data.
 * Later, it should be populated from the real collection / gallery data source.
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    // List of gallery items to be displayed in the RecyclerView
    private final List<GalleryItem> itemList;

    /**
     * Creates a new gallery adapter.
     *
     * @param itemList list of gallery items
     */
    public GalleryAdapter(List<GalleryItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the gallery item layout for each card cell
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gallery, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        GalleryItem item = itemList.get(position);

        if (item.isUnlocked()) {
            // Show the actual card image and information if the card is unlocked
            holder.itemImage.setImageResource(item.getImageResId());
            holder.itemImage.setAlpha(1.0f);
            holder.itemName.setText(item.getName());
            holder.itemRarity.setText(item.getRarity());
        } else {
            // Show the locked overlay and placeholder text if the card is still locked
            holder.itemImage.setImageResource(R.drawable.locked_overlay);
            holder.itemImage.setAlpha(1.0f);
            holder.itemName.setText("Locked");
            holder.itemRarity.setText("Unknown");
        }

        // TODO Interface 9:
        // If later the project needs extra gallery interactions,
        // such as card detail popups, rarity filters, or click events,
        // they can be added here or inside the ViewHolder.
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    /**
     * ViewHolder for one gallery card item.
     */
    static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemName;
        TextView itemRarity;

        public GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemName = itemView.findViewById(R.id.itemName);
            itemRarity = itemView.findViewById(R.id.itemRarity);
        }
    }
}