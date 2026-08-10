package org.telegram.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LyrxDeletedStorage;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;

public class LyrxDeletedSettingsActivity extends BaseFragment {

    private TextView clearText;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle("Show Deleted");
        actionBar.setAllowOverlayTitle(true);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        root.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(20), AndroidUtilities.dp(16), AndroidUtilities.dp(16));

        FrameLayout toggleCard = new FrameLayout(context);
        GradientDrawable tcBg = new GradientDrawable();
        tcBg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        tcBg.setCornerRadius(AndroidUtilities.dp(18));
        toggleCard.setBackground(tcBg);

        TextView toggleTitle = new TextView(context);
        toggleTitle.setText("Show Deleted Messages");
        toggleTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        toggleTitle.setTextSize(16);
        toggleTitle.setTypeface(AndroidUtilities.bold());
        toggleCard.addView(toggleTitle, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,
                Gravity.LEFT | Gravity.CENTER_VERTICAL, 16, 0, 70, 0));

        org.telegram.ui.Components.Switch sw = new org.telegram.ui.Components.Switch(context);
        sw.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_switchTrackBlueThumb, Theme.key_switchTrackBlueThumbChecked);
        sw.setChecked(SharedConfig.lyrxShowDeleted, false);
        toggleCard.addView(sw, LayoutHelper.createFrame(37, 20, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 16, 0));
        toggleCard.setOnClickListener(v -> {
            boolean ns = !sw.isChecked();
            sw.setChecked(ns, true);
            SharedConfig.lyrxShowDeleted = ns;
            MessagesController.getGlobalMainSettings().edit().putBoolean("lyrxShowDeleted", ns).apply();
        });
        root.addView(toggleCard, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 58));

        FrameLayout clearCard = new FrameLayout(context);
        GradientDrawable ccBg = new GradientDrawable();
        ccBg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        ccBg.setCornerRadius(AndroidUtilities.dp(18));
        clearCard.setBackground(ccBg);

        clearText = new TextView(context);
        clearText.setTextColor(0xFFFF3B30);
        clearText.setTextSize(16);
        clearText.setTypeface(AndroidUtilities.bold());
        updateClearText();
        clearCard.addView(clearText, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,
                Gravity.LEFT | Gravity.CENTER_VERTICAL, 16, 0, 16, 0));

        clearCard.setOnClickListener(v -> {
            LyrxDeletedStorage.clearAll();
            updateClearText();
            BulletinFactory.of(this).createSimpleBulletin(R.raw.chats_infotip, "Successfully cleared").show();
        });

        LinearLayout.LayoutParams clearParams = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 58);
        clearParams.topMargin = AndroidUtilities.dp(12);
        root.addView(clearCard, clearParams);

        fragmentView = root;
        return fragmentView;
    }

    private void updateClearText() {
        long size = LyrxDeletedStorage.totalSize();
        clearText.setText("Clear Deleted Storage (" + LyrxDeletedStorage.formatSize(size) + ")");
    }
}
