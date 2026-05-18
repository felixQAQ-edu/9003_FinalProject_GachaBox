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

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gachabox.model.GachaItem;
import com.example.gachabox.data.GachaRepository;
import com.example.gachabox.gallery.GalleryAdapter;
import com.example.gachabox.gallery.GalleryItem;
import com.example.gachabox.logic.GachaEngine;
import com.google.android.material.card.MaterialCardView;
import com.example.gachabox.data.entity.InventoryEntity;

import java.util.ArrayList;
import java.util.List;

public class  MainActivity extends AppCompatActivity {

    private boolean isAnimating = false;
    private boolean isCardFrontShowing = false;
    private boolean isCardShowing = false;
    private GachaRepository repository;
    private TextView chanceCountText;
    private ImageView starButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        repository = GachaRepository.getInstance(this);
        repository.initializeData();

        GachaEngine engine = new GachaEngine();

        starButton = findViewById(R.id.starButton);
        View cardSpawnAnchor = findViewById(R.id.cardSpawnAnchor);
        MaterialCardView resultCard = findViewById(R.id.resultCard);
        ImageView cardBack = findViewById(R.id.cardBack);
        ImageView cardFront = findViewById(R.id.cardFront);
        chanceCountText = findViewById(R.id.chanceCountText);
        refreshTokenDisplay();

        starButton.setOnClickListener(v -> {
            if (isAnimating || isCardShowing) return;

            // Lock immediately to absorb rapid double-taps. Both flags are
            // reset on the failure path here, or by the card animation /
            // dismiss flow on the success path.
            isAnimating = true;
            isCardShowing = true;

            repository.spendOneToken((success, message) -> {
                if (!success) {
                    // Out of tokens — release locks and tell the user.
                    isAnimating = false;
                    isCardShowing = false;
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Token spent. Update the displayed balance (and re-enable /
                // disable the star button if we just hit zero).
                refreshTokenDisplay();

                // Run the gacha pull.
                GachaItem pulledItem = engine.pull();
                String rarity = pulledItem.getRarity();
                int cardImageResId = getResources().getIdentifier(
                        pulledItem.getImageResName(),
                        "drawable",
                        getPackageName()
                );

                // Persist the unlock to the inventory table. The Repository
                // returns a "New! ..." or "Duplicate: ..." message; surface
                // it as a Toast so the player can tell new pulls from dupes.
                repository.unlockItem(pulledItem.getId(), (ok, msg) -> {
                    Log.d("GachaPull", msg);
                    if (ok) {
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

                // Button press animation
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

                // Card spawn + flip animation (unchanged)
                showDrawCard(
                        resultCard,
                        cardBack,
                        cardFront,
                        cardSpawnAnchor,
                        rarity,
                        cardImageResId
                );

                // History stub still empty — closed in a later iteration.
                saveDrawHistory(pulledItem);
                updateCollection(pulledItem);
            });
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

        // Bottom action bar — Get Chances button (adds 10 tokens).
        TextView getChancesButton = findViewById(R.id.getChancesButton);
        getChancesButton.setOnClickListener(v -> {
            repository.addTokens(10, (success, message) -> {
                if (success) {
                    refreshTokenDisplay();
                    Toast.makeText(MainActivity.this, "+10 chances added!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Bottom action bar — Gallery button (shows the live inventory).
        TextView galleryButton = findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(v -> {
            // Pull the real inventory snapshot before showing the dialog.
            // Callback fires on the main thread (Repository guarantees this),
            // so all UI work below is safe.
            repository.getAllInventory(inventory -> {

                // Convert Room entities to the GalleryAdapter's view model,
                // and count unlocked entries for the progress header.
                List<GalleryItem> itemList = new ArrayList<>();
                int unlockedCount = 0;
                int totalCount = 0;
                if (inventory != null) {
                    totalCount = inventory.size();
                    for (InventoryEntity entity : inventory) {
                        if (entity.unlocked) {
                            unlockedCount++;
                        }
                        int imageResId = getResources().getIdentifier(
                                entity.imageResName,
                                "drawable",
                                getPackageName()
                        );
                        itemList.add(new GalleryItem(
                                imageResId,
                                entity.itemName,
                                entity.rarity,
                                entity.unlocked
                        ));
                    }
                }

                // Build and display the dialog.
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                View dialogView = inflater.inflate(R.layout.dialog_gallery, null);

                // Collection progress header. Null-checked so the build
                // doesn't break if the XML doesn't have the TextView yet
                // (e.g. teammate hasn't pulled the layout change).
                TextView collectionProgressText =
                        dialogView.findViewById(R.id.collectionProgressText);
                if (collectionProgressText != null) {
                    collectionProgressText.setText(
                            "Collected: " + unlockedCount + " / " + totalCount);
                }

                RecyclerView dialogGalleryRecyclerView = dialogView.findViewById(R.id.dialogGalleryRecyclerView);
                dialogGalleryRecyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 3));

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

    private void refreshTokenDisplay() {
        repository.getUser(user -> {
            if (user != null) {
                chanceCountText.setText(String.valueOf(user.tokenBalance));

                boolean hasTokens = user.tokenBalance > 0;
                starButton.setEnabled(hasTokens);

                if (hasTokens) {
                    // Restore full color
                    starButton.clearColorFilter();
                    starButton.setAlpha(1f);
                } else {
                    // Desaturate to grayscale — the colorful star becomes a
                    // grey star, which reads clearly as "disabled".
                    ColorMatrix matrix = new ColorMatrix();
                    matrix.setSaturation(0);
                    starButton.setColorFilter(new ColorMatrixColorFilter(matrix));
                    starButton.setAlpha(0.6f);
                }
            }
        });
    }
}
