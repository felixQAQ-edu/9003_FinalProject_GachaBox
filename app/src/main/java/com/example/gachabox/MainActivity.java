package com.example.gachabox;

//import androidx.appcompat.app.AppCompatActivity;
//import android.os.Bundle;
//import android.util.Log;
//
//import com.example.gachabox.database.DatabaseHelper;
//import com.example.gachabox.logic.GachaEngine;
//import com.example.gachabox.model.GachaItem;
//
//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//
//        GachaEngine engine = new GachaEngine();
//        DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);
//
//        Log.d("GachaTest", "start test...");
//
//        for (int i = 1; i <= 35; i++) {
//            GachaItem result = engine.pull();
//
//            boolean success = dbHelper.addGachaItem(result.getName(), result.getRarity());
//
//            if (success) {
//                Log.d("GachaTest", "第 " + i + " 抽: " + result.toString() + " | 存入成功!");
//            } else {
//                Log.e("GachaTest", "第 " + i + " 抽存入失败!");
//            }
//        }
//    }
//}
package com.example.gachabox;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gachabox.database.DatabaseHelper;
import com.example.gachabox.gallery.GalleryAdapter;
import com.example.gachabox.gallery.GalleryItem;
import com.example.gachabox.logic.GachaEngine;
import com.example.gachabox.model.GachaItem;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private boolean isAnimating = false;
    private boolean isCardFrontShowing = false;
    private boolean isCardShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        GachaEngine engine = new GachaEngine();
        DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);

        ImageView starButton = findViewById(R.id.starButton);
        View cardSpawnAnchor = findViewById(R.id.cardSpawnAnchor);
        MaterialCardView resultCard = findViewById(R.id.resultCard);
        ImageView cardBack = findViewById(R.id.cardBack);
        ImageView cardFront = findViewById(R.id.cardFront);
        TextView chanceCountText = findViewById(R.id.chanceCountText);

        starButton.setOnClickListener(v -> {
            if (isAnimating || isCardShowing) return;

            // TODO Interface 1:
            // Check whether the user still has available draw chances.
            // This is currently not connected to the real token/chance system.

            isAnimating = true;
            isCardShowing = true;

            GachaItem pulledItem = engine.pull();
            boolean success = dbHelper.addGachaItem(pulledItem.getName(), pulledItem.getRarity());

            if (success) {
                Log.d("GachaTest", "Pull success: " + pulledItem.toString());
            } else {
                Log.e("GachaTest", "Failed to save pull result");
            }

            String rarity = pulledItem.getRarity();
            String cardName = pulledItem.getName();
            int cardImageResId = mapCardNameToImage(cardName, rarity);

            v.animate()
                    .scaleX(0.88f)
                    .scaleY(0.88f)
                    .alpha(0.85f)
                    .setDuration(100)
                    .withEndAction(() -> v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(120)
                            .start())
                    .start();

            showDrawCard(
                    resultCard,
                    cardBack,
                    cardFront,
                    cardSpawnAnchor,
                    rarity,
                    cardImageResId
            );

            // TODO Interface 4:
            // Replace this with the real remaining chances / token count.
            // For now, the existing UI text is left unchanged.
            chanceCountText.setText(chanceCountText.getText());

            // TODO Interface 5:
            // Save draw history using the real persistence layer.
            saveDrawHistory(pulledItem);

            // TODO Interface 6:
            // Update collection / gallery unlock state using real data.
            updateCollection(pulledItem);
        });

        LinearLayout infoBadge = findViewById(R.id.infoBadge);
        infoBadge.setOnClickListener(v -> {
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            View dialogView = inflater.inflate(R.layout.dialog_info, null);

            TextView tabInfo = dialogView.findViewById(R.id.tabInfo);
            TextView tabHistory = dialogView.findViewById(R.id.tabHistory);
            TextView dialogInfoContent = dialogView.findViewById(R.id.dialogInfoContent);
            LinearLayout historyHeaderRow = dialogView.findViewById(R.id.historyHeaderRow);
            TextView historyEmptyText = dialogView.findViewById(R.id.historyEmptyText);

            String infoContent = "Rules & Drop Rates\n\nCommon: 40%\nUncommon: 30%\nRare: 20%\nSecret: 10%";
            dialogInfoContent.setText(infoContent);

            dialogInfoContent.setVisibility(View.VISIBLE);
            historyHeaderRow.setVisibility(View.GONE);
            historyEmptyText.setVisibility(View.GONE);

            tabInfo.setOnClickListener(view -> {
                dialogInfoContent.setVisibility(View.VISIBLE);
                historyHeaderRow.setVisibility(View.GONE);
                historyEmptyText.setVisibility(View.GONE);

                tabInfo.setTextColor(0xFF4F4A57);
                tabHistory.setTextColor(0xFF8A7BA3);
            });

            tabHistory.setOnClickListener(view -> {
                dialogInfoContent.setVisibility(View.GONE);
                historyHeaderRow.setVisibility(View.VISIBLE);
                historyEmptyText.setVisibility(View.VISIBLE);

                tabInfo.setTextColor(0xFF8A7BA3);
                tabHistory.setTextColor(0xFF4F4A57);

                // TODO Interface 7:
                // Load real draw history here.
                loadDrawHistory();
            });

            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setView(dialogView)
                    .create();

            dialog.show();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
        });

        TextView galleryButton = findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(v -> {
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            View dialogView = inflater.inflate(R.layout.dialog_gallery, null);

            RecyclerView dialogGalleryRecyclerView = dialogView.findViewById(R.id.dialogGalleryRecyclerView);
            dialogGalleryRecyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 3));

            // TODO Interface 8:
            // Replace this mock data with the real collection / inventory data source.
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
            dialogGalleryRecyclerView.setAdapter(adapter);

            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setView(dialogView)
                    .create();

            dialog.show();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
                dialog.getWindow().setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showDrawCard(MaterialCardView resultCard,
                              ImageView cardBack,
                              ImageView cardFront,
                              View anchor,
                              String rarity,
                              int cardImageResId) {

        resultCard.animate().cancel();
        resultCard.clearAnimation();

        isCardFrontShowing = false;
        resultCard.setStrokeColor(getRarityColor(rarity));

        cardBack.setVisibility(View.VISIBLE);
        cardFront.setVisibility(View.GONE);
        cardFront.setImageResource(cardImageResId);

        resultCard.setVisibility(View.VISIBLE);

        resultCard.post(() -> {
            int[] anchorLoc = new int[2];
            int[] parentLoc = new int[2];

            anchor.getLocationOnScreen(anchorLoc);
            ((View) resultCard.getParent()).getLocationOnScreen(parentLoc);

            float startX = anchorLoc[0] - parentLoc[0] + anchor.getWidth() / 2f - resultCard.getWidth() / 2f;
            float startY = anchorLoc[1] - parentLoc[1] + anchor.getHeight() / 2f - resultCard.getHeight() / 2f;

            View parent = (View) resultCard.getParent();
            float finalX = (parent.getWidth() - resultCard.getWidth()) / 2f;
            float finalY = (parent.getHeight() - resultCard.getHeight()) / 2f - 60f;

            resultCard.setX(startX);
            resultCard.setY(startY);
            resultCard.setScaleX(0.2f);
            resultCard.setScaleY(0.2f);
            resultCard.setAlpha(0f);
            resultCard.setRotationY(0f);

            resultCard.animate()
                    .x(finalX)
                    .y(finalY)
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(650)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(() -> {
                        isAnimating = false;
                        enableCardFlip(resultCard, cardBack, cardFront);
                    })
                    .start();
        });
    }

    private void enableCardFlip(MaterialCardView resultCard,
                                ImageView cardBack,
                                ImageView cardFront) {

        resultCard.setOnClickListener(v -> {
            if (isAnimating) return;

            if (isCardFrontShowing) {
                isAnimating = true;

                resultCard.animate()
                        .alpha(0f)
                        .scaleX(0.85f)
                        .scaleY(0.85f)
                        .setDuration(180)
                        .withEndAction(() -> {
                            resultCard.setVisibility(View.INVISIBLE);
                            resultCard.setAlpha(1f);
                            resultCard.setScaleX(1f);
                            resultCard.setScaleY(1f);
                            resultCard.setRotationY(0f);
                            cardBack.setVisibility(View.VISIBLE);
                            cardFront.setVisibility(View.GONE);
                            isCardFrontShowing = false;
                            isCardShowing = false;
                            isAnimating = false;
                        })
                        .start();
                return;
            }

            isAnimating = true;

            float scale = getResources().getDisplayMetrics().density;
            resultCard.setCameraDistance(8000 * scale);

            resultCard.animate()
                    .rotationY(90f)
                    .setDuration(180)
                    .withEndAction(() -> {
                        cardBack.setVisibility(View.GONE);
                        cardFront.setVisibility(View.VISIBLE);
                        resultCard.setRotationY(-90f);

                        resultCard.animate()
                                .rotationY(0f)
                                .setDuration(180)
                                .withEndAction(() -> {
                                    isAnimating = false;
                                    isCardFrontShowing = true;
                                })
                                .start();
                    })
                    .start();
        });
    }

    private int getRarityColor(String rarity) {
        switch (rarity) {
            case "Common":
                return 0xFF7EC8FF;
            case "Uncommon":
                return 0xFFC08CFF;
            case "Rare":
                return 0xFFFFD86B;
            case "Secret":
                return 0xFFFF6B6B;
            default:
                return 0xFF6BB6FF;
        }
    }

    private int mapCardNameToImage(String cardName, String rarity) {
        switch (cardName) {
            case "Golden Retriever":
                return R.drawable.common_1;
            case "Husky":
                return R.drawable.rare_1;
            case "Godzilla":
                return R.drawable.secret_1;
            default:
                switch (rarity) {
                    case "Common":
                        return R.drawable.common_1;
                    case "Uncommon":
                        return R.drawable.uncommon_1;
                    case "Rare":
                        return R.drawable.rare_1;
                    case "Secret":
                        return R.drawable.secret_1;
                    default:
                        return R.drawable.common_1;
                }
        }
    }

    private void saveDrawHistory(GachaItem pulledItem) {
        // TODO Interface 5:
        // Save draw history using the real persistence layer.
    }

    private void updateCollection(GachaItem pulledItem) {
        // TODO Interface 6:
        // Update collection / gallery unlock state using real data.
    }

    private void loadDrawHistory() {
        // TODO Interface 7:
        // Load real draw history here.
    }
}