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

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gachabox.model.Banner;
import com.example.gachabox.model.GachaItem;
import com.example.gachabox.data.GachaRepository;
import com.example.gachabox.gallery.GalleryAdapter;
import com.example.gachabox.gallery.GalleryItem;
import com.example.gachabox.logic.GachaEngine;
import com.google.android.material.card.MaterialCardView;
import com.example.gachabox.data.entity.HistoryEntity;
import com.example.gachabox.data.entity.InventoryEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class  MainActivity extends AppCompatActivity {

    private boolean isAnimating = false;
    private boolean isCardFrontShowing = false;
    private boolean isCardShowing = false;
    private GachaRepository repository;
    private TextView chanceCountText;
    private ImageView starButton;

    // ----- Banner state (Phase 2) -----
    // Currently-selected banner. Mutable now (Phase 1 had it as final);
    // the three main-screen tabs flip this between DOGS / ANIME / FOOD,
    // and every pull / gallery-fetch reads this value.
    private String currentBannerId = Banner.DOGS.id;
    private TextView tabBannerDogs;
    private TextView tabBannerAnime;
    private TextView tabBannerFood;

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

        // ----- Banner tabs (main screen) -----
        tabBannerDogs  = findViewById(R.id.tabBannerDogs);
        tabBannerAnime = findViewById(R.id.tabBannerAnime);
        tabBannerFood  = findViewById(R.id.tabBannerFood);
        tabBannerDogs .setOnClickListener(v -> selectBanner(Banner.DOGS.id));
        tabBannerAnime.setOnClickListener(v -> selectBanner(Banner.ANIME.id));
        tabBannerFood .setOnClickListener(v -> selectBanner(Banner.FOOD.id));
        // Make sure tab styling matches the initial banner.
        selectBanner(currentBannerId);

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

                // Run the gacha pull from the currently-selected banner.
                GachaItem pulledItem = engine.pull(currentBannerId);
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

                // Append a row to the history table. Fire-and-forget — the
                // dialog reads it lazily when the user opens the History tab.
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
                // Hide the column-header row — our formatted text doesn't
                // align to it, and the timestamp + rarity are clear enough
                // inline that the headers add noise.
                historyHeaderRow.setVisibility(View.GONE);
                historyEmptyText.setVisibility(View.VISIBLE);
                historyEmptyText.setText("Loading...");

                tabInfo.setTextColor(0xFF8A7BA3);
                tabHistory.setTextColor(0xFF4F4A57);

                loadDrawHistory(historyEmptyText);
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

        // Bottom action bar — Gallery button (shows the current banner's inventory).
        TextView galleryButton = findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(v -> openGalleryDialog());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Update the active banner. Refreshes the main-screen tab styling so
     * the user sees which banner pulls will be drawn from. The next
     * star-button press will use this banner via {@code engine.pull()}.
     */
    private void selectBanner(String bannerId) {
        currentBannerId = bannerId;
        styleTab(tabBannerDogs,  Banner.DOGS.id.equals(bannerId));
        styleTab(tabBannerAnime, Banner.ANIME.id.equals(bannerId));
        styleTab(tabBannerFood,  Banner.FOOD.id.equals(bannerId));
    }

    /**
     * Apply selected / unselected text styling to a tab TextView.
     * Selected = dark + bold; unselected = faded purple + normal.
     * Matches the colour palette used by the existing info dialog tabs.
     */
    private void styleTab(TextView tab, boolean selected) {
        if (tab == null) return;
        if (selected) {
            tab.setTextColor(0xFF4F4A57);
            tab.setTypeface(null, Typeface.BOLD);
        } else {
            tab.setTextColor(0xFF8A7BA3);
            tab.setTypeface(null, Typeface.NORMAL);
        }
    }

    /**
     * Build and show the Gallery dialog, which now contains its own
     * banner-tab strip. Selecting a tab inside the dialog also flips the
     * main screen's active banner, so pulling after closing the dialog
     * uses whichever banner the user was last looking at.
     */
    private void openGalleryDialog() {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View dialogView = inflater.inflate(R.layout.dialog_gallery, null);

        TextView galleryTabDogs  = dialogView.findViewById(R.id.galleryTabDogs);
        TextView galleryTabAnime = dialogView.findViewById(R.id.galleryTabAnime);
        TextView galleryTabFood  = dialogView.findViewById(R.id.galleryTabFood);
        TextView collectionProgressText =
                dialogView.findViewById(R.id.collectionProgressText);
        RecyclerView recyclerView =
                dialogView.findViewById(R.id.dialogGalleryRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 3));

        // Repaint the dialog (tabs + progress + grid) for the current banner.
        // Defined as a Runnable so tab clicks below can re-run it.
        Runnable refresh = () -> {
            styleTab(galleryTabDogs,  Banner.DOGS.id.equals(currentBannerId));
            styleTab(galleryTabAnime, Banner.ANIME.id.equals(currentBannerId));
            styleTab(galleryTabFood,  Banner.FOOD.id.equals(currentBannerId));

            repository.getInventoryByBanner(currentBannerId, inventory -> {
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
                if (collectionProgressText != null) {
                    collectionProgressText.setText(
                            "Collected: " + unlockedCount + " / " + totalCount);
                }
                recyclerView.setAdapter(new GalleryAdapter(itemList));
            });
        };

        // Each in-dialog tab updates currentBannerId (which also restyles
        // the main screen tabs via selectBanner) then re-renders the dialog.
        galleryTabDogs .setOnClickListener(v -> { selectBanner(Banner.DOGS.id);  refresh.run(); });
        galleryTabAnime.setOnClickListener(v -> { selectBanner(Banner.ANIME.id); refresh.run(); });
        galleryTabFood .setOnClickListener(v -> { selectBanner(Banner.FOOD.id);  refresh.run(); });

        // Initial render — show whichever banner is currently active.
        refresh.run();

        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setView(dialogView)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            dialog.getWindow().setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
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



    /**
     * Append the just-pulled item to the persistent history log.
     * Fire-and-forget — we don't gate the UI on this completing.
     */
    private void saveDrawHistory(GachaItem pulledItem) {
        repository.recordHistory(pulledItem, (success, message) -> {
            Log.d("GachaHistory", message);
        });
    }

    private void updateCollection(GachaItem pulledItem) {
        // No-op. Unlock state is already persisted by repository.unlockItem
        // in the pull flow; the gallery dialog reads the latest snapshot
        // on demand via repository.getInventoryByBanner.
    }

    /**
     * Populate the Info dialog's History tab with the 50 most recent
     * pulls. Format each line (monospace) as:
     *   MMM dd HH:mm   Banner   Name              Rarity
     *   May 19 13:33   Doggos   Common 1          Common
     *   May 19 13:33   Anime    Hero 5            Uncommon
     *   May 19 13:33   Foodies  Snack 10          Secret
     * The banner column lets the user tell which pool each pull came
     * from at a glance — important once anime / food banners are in use.
     */
    private void loadDrawHistory(TextView target) {
        repository.getHistory(history -> {
            if (history == null || history.isEmpty()) {
                target.setTypeface(Typeface.DEFAULT);
                target.setText("No pulls yet — try the gacha machine!");
                return;
            }

            target.setTypeface(Typeface.MONOSPACE);

            SimpleDateFormat fmt = new SimpleDateFormat("MMM dd HH:mm", Locale.US);
            StringBuilder sb = new StringBuilder();
            for (HistoryEntity h : history) {
                String time = fmt.format(new Date(h.timestamp));
                String banner = fitColumn(Banner.byId(h.bannerId).displayName, 7);
                String name = fitColumn(h.itemName, 14);
                sb.append(time)
                  .append("  ")
                  .append(banner)
                  .append("  ")
                  .append(name)
                  .append("  ")
                  .append(h.rarity)
                  .append("\n");
            }
            target.setText(sb.toString().trim());
        });
    }

    /**
     * Pad with trailing spaces if shorter than width; truncate if longer.
     * Used to keep monospace history rows aligned.
     */
    private static String fitColumn(String value, int width) {
        if (value == null) value = "";
        if (value.length() > width) {
            return value.substring(0, width);
        }
        return String.format(Locale.US, "%-" + width + "s", value);
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
