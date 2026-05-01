package com.example.gachabox.gallery;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gachabox.R;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        RecyclerView galleryRecyclerView = findViewById(R.id.galleryRecyclerView);

        // Display 3 items per row in the gallery grid.
        galleryRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // TODO Interface 8: Collection / gallery data source
        // The current itemList is static mock data for front-end display only.
        // Later, this should be replaced with the real collection data source.
        // The teammate responsible for collection/gallery data should provide:
        // 1. imageResId
        // 2. card name
        // 3. rarity
        // 4. unlocked status
        //
        // Expected behavior:
        // - Unlocked cards should display their real image and information
        // - Locked cards should display the locked overlay state
        List<GalleryItem> itemList = new ArrayList<>();
        itemList.add(new GalleryItem(R.drawable.common_1, "Common 1", "Common", true));
        itemList.add(new GalleryItem(R.drawable.common_2, "Common 2", "Common", true));
        itemList.add(new GalleryItem(R.drawable.common_3, "Common 3", "Common", true));
        itemList.add(new GalleryItem(R.drawable.common_4, "Common 4", "Common", true));
        itemList.add(new GalleryItem(R.drawable.uncommon_1, "Uncommon 1", "Uncommon", false));
        itemList.add(new GalleryItem(R.drawable.uncommon_2, "Uncommon 2", "Uncommon", false));
        itemList.add(new GalleryItem(R.drawable.uncommon_3, "Uncommon 3", "Uncommon", false));
        itemList.add(new GalleryItem(R.drawable.rare_1, "Rare 1", "Rare", false));
        itemList.add(new GalleryItem(R.drawable.rare_2, "Rare 2", "Rare", false));
        itemList.add(new GalleryItem(R.drawable.secret_1, "Secret 1", "Secret", false));

        GalleryAdapter adapter = new GalleryAdapter(itemList);

        // TODO Interface 9: Gallery refresh / synchronization
        // If the collection data changes after a draw, the gallery should be refreshed
        // using the latest unlocked status from the real data source.
        galleryRecyclerView.setAdapter(adapter);
    }
}