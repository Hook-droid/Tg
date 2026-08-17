package org.telegram.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;

public class LyrxChatModesActivity extends BaseFragment {

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle("Chat Modes");
        actionBar.setAllowOverlayTitle(true);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) finishFragment();
            }
        });

        android.widget.ScrollView scroll = new android.widget.ScrollView(context);
        scroll.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(12), AndroidUtilities.dp(12), AndroidUtilities.dp(12));

        TextView limitHeader = new TextView(context);
        limitHeader.setText("Channel Join Limit");
        limitHeader.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        limitHeader.setTextSize(15);
        limitHeader.setTypeface(AndroidUtilities.bold());
        limitHeader.setPadding(AndroidUtilities.dp(16), 0, AndroidUtilities.dp(16), AndroidUtilities.dp(8));
        root.addView(limitHeader, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        LinearLayout limitGroup = new LinearLayout(context);
        limitGroup.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable limitBg = new GradientDrawable();
        limitBg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        limitBg.setCornerRadius(AndroidUtilities.dp(16));
        limitGroup.setBackground(limitBg);
        limitGroup.setClipToOutline(true);
        limitGroup.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(View view, android.graphics.Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), AndroidUtilities.dp(16));
            }
        });

        int limitImageRes = 0;
        try {
            limitImageRes = context.getResources().getIdentifier("lyrx_join_limit", "drawable", context.getPackageName());
        } catch (Exception ignore) {}
        if (limitImageRes != 0) {
            ImageView limitImage = new ImageView(context);
            limitImage.setImageResource(limitImageRes);
            limitImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            limitImage.setAdjustViewBounds(false);
            limitGroup.addView(limitImage, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 150));
        }

        TextCheckCell limitCell = new TextCheckCell(context);
        limitCell.setTextAndValueAndCheck("Limit Sinirini Kaldirin", "Auto-retry channel joins when Telegram rate limits you", SharedConfig.lyrxBypassJoinLimit, true, false);
        limitCell.setOnClickListener(v -> {
            boolean ns = !limitCell.isChecked();
            limitCell.setChecked(ns);
            SharedConfig.lyrxBypassJoinLimit = ns;
            MessagesController.getGlobalMainSettings().edit().putBoolean("lyrxBypassJoinLimit", ns).apply();
        });
        limitGroup.addView(limitCell);

        LinearLayout.LayoutParams limitParams = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
        limitParams.bottomMargin = AndroidUtilities.dp(16);
        root.addView(limitGroup, limitParams);

        TextView speedHeader = new TextView(context);
        speedHeader.setText("Increasing Download Speed");
        speedHeader.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        speedHeader.setTextSize(15);
        speedHeader.setTypeface(AndroidUtilities.bold());
        speedHeader.setPadding(AndroidUtilities.dp(16), 0, AndroidUtilities.dp(16), AndroidUtilities.dp(8));
        root.addView(speedHeader, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        LinearLayout speedGroup = new LinearLayout(context);
        speedGroup.setOrientation(LinearLayout.VERTICAL);
        android.graphics.drawable.GradientDrawable speedBg = new android.graphics.drawable.GradientDrawable();
        speedBg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        speedBg.setCornerRadius(AndroidUtilities.dp(16));
        speedGroup.setBackground(speedBg);

        org.telegram.ui.Components.SlideChooseView speedSlider = new org.telegram.ui.Components.SlideChooseView(context);
        speedSlider.setOptions(SharedConfig.lyrxDownloadBoost, "Off", "Fast", "Ultra");
        speedSlider.setCallback(new org.telegram.ui.Components.SlideChooseView.Callback() {
            @Override
            public void onOptionSelected(int index) {
                SharedConfig.lyrxDownloadBoost = index;
                MessagesController.getGlobalMainSettings().edit().putInt("lyrxDownloadBoost", index).apply();
            }
        });
        speedGroup.addView(speedSlider, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 6, 0, 6));

        speedGroup.addView(divider(context));

        TextCheckCell uploadCell = new TextCheckCell(context);
        uploadCell.setTextAndCheck("Increasing Upload Speed", SharedConfig.lyrxUploadBoost, false);
        uploadCell.setOnClickListener(v -> {
            boolean ns = !uploadCell.isChecked();
            uploadCell.setChecked(ns);
            SharedConfig.lyrxUploadBoost = ns;
            MessagesController.getGlobalMainSettings().edit().putBoolean("lyrxUploadBoost", ns).apply();
        });
        speedGroup.addView(uploadCell);

        LinearLayout.LayoutParams sgp = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
        sgp.bottomMargin = AndroidUtilities.dp(16);
        root.addView(speedGroup, sgp);

        TextView delHeader = new TextView(context);
        delHeader.setText("View Deleted Messages");
        delHeader.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        delHeader.setTextSize(15);
        delHeader.setTypeface(AndroidUtilities.bold());
        delHeader.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(8));
        root.addView(delHeader, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        LinearLayout delGroup = new LinearLayout(context);
        delGroup.setOrientation(LinearLayout.VERTICAL);
        android.graphics.drawable.GradientDrawable delBg = new android.graphics.drawable.GradientDrawable();
        delBg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        delBg.setCornerRadius(AndroidUtilities.dp(16));
        delGroup.setBackground(delBg);

        TextCheckCell showDeleted = new TextCheckCell(context);
        showDeleted.setTextAndValueAndCheck("Show Deleted", "Show deleted messages inside LyrxGram", SharedConfig.lyrxShowDeleted, true, true);
        showDeleted.setOnClickListener(v -> {
            boolean ns = !showDeleted.isChecked();
            showDeleted.setChecked(ns);
            SharedConfig.lyrxShowDeleted = ns;
            MessagesController.getGlobalMainSettings().edit().putBoolean("lyrxShowDeleted", ns).apply();
        });
        delGroup.addView(showDeleted);

        delGroup.addView(divider(context));

        TextCell clearCell = new TextCell(context);
        clearCell.setTextAndIcon(clearStorageText(), R.drawable.msg_clear, false);
        clearCell.setColors(Theme.key_text_RedRegular, Theme.key_text_RedRegular);
        clearCell.setOnClickListener(v -> {
            org.telegram.messenger.LyrxDeletedStorage.clearAll();
            clearCell.setTextAndIcon(clearStorageText(), R.drawable.msg_clear, false);
            clearCell.setColors(Theme.key_text_RedRegular, Theme.key_text_RedRegular);
        });
        delGroup.addView(clearCell);

        root.addView(delGroup, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        TextView sidHeader = new TextView(context);
        sidHeader.setText("Open Profile from ID");
        sidHeader.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        sidHeader.setTextSize(15);
        sidHeader.setTypeface(AndroidUtilities.bold());
        sidHeader.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(8));
        root.addView(sidHeader, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        LinearLayout sidGroup = new LinearLayout(context);
        sidGroup.setOrientation(LinearLayout.VERTICAL);
        android.graphics.drawable.GradientDrawable sidBg = new android.graphics.drawable.GradientDrawable();
        sidBg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        sidBg.setCornerRadius(AndroidUtilities.dp(16));
        sidGroup.setBackground(sidBg);

        TextCell searchId = new TextCell(context);
        searchId.setTextAndIcon("Searching ID", R.drawable.msg_search, false);
        searchId.setOnClickListener(v -> presentFragment(new LyrxSearchIdActivity()));
        sidGroup.addView(searchId);

        root.addView(sidGroup, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        TextView trHeader = new TextView(context);
        trHeader.setText("Translate Messages");
        trHeader.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        trHeader.setTextSize(15);
        trHeader.setTypeface(AndroidUtilities.bold());
        trHeader.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(8));
        root.addView(trHeader, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        LinearLayout trGroup = new LinearLayout(context);
        trGroup.setOrientation(LinearLayout.VERTICAL);
        android.graphics.drawable.GradientDrawable trBg = new android.graphics.drawable.GradientDrawable();
        trBg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        trBg.setCornerRadius(AndroidUtilities.dp(16));
        trGroup.setBackground(trBg);

        TextCheckCell showButton = new TextCheckCell(context);
        showButton.setTextAndCheck("Show Translate Button", getTranslateController().isContextTranslateEnabled(), true);
        showButton.setOnClickListener(v -> {
            boolean ns = !showButton.isChecked();
            showButton.setChecked(ns);
            getTranslateController().setContextTranslateEnabled(ns);
        });
        trGroup.addView(showButton);

        trGroup.addView(divider(context));

        TextCell targetCell = new TextCell(context);
        targetCell.setTextAndValue("Target Language", targetLangName(), true);
        targetCell.setOnClickListener(v -> showTargetLanguagePicker(targetCell));
        trGroup.addView(targetCell);

        trGroup.addView(divider(context));

        TextCell doNotCell = new TextCell(context);
        doNotCell.setTextAndValue("Do Not Translate", restrictedSummary(), false);
        doNotCell.setOnClickListener(v -> presentFragment(new RestrictedLanguagesSelectActivity()));
        trGroup.addView(doNotCell);

        LinearLayout.LayoutParams trgp = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
        root.addView(trGroup, trgp);

        TextView whHeader = new TextView(context);
        whHeader.setText("Voice To Text (Beta)");
        whHeader.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        whHeader.setTextSize(15);
        whHeader.setTypeface(AndroidUtilities.bold());
        whHeader.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(8));
        root.addView(whHeader, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        LinearLayout whGroup = new LinearLayout(context);
        whGroup.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable whBg = new GradientDrawable();
        whBg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        whBg.setCornerRadius(AndroidUtilities.dp(16));
        whGroup.setBackground(whBg);

        TextCell whCell = new TextCell(context);
        whCell.setTextAndValue("Engine Status", "Tap to check", false);
        whCell.setOnClickListener(v -> {
            String info = org.telegram.messenger.LyrxWhisper.systemInfo();
            if (info == null) {
                String err = org.telegram.messenger.LyrxWhisper.getLoadError();
                whCell.setTextAndValue("Engine Status", err == null ? "Not loaded" : "Failed", false);
            } else {
                whCell.setTextAndValue("Engine Status", "OK", false);
                if (getParentActivity() != null) {
                    org.telegram.ui.ActionBar.AlertDialog.Builder b = new org.telegram.ui.ActionBar.AlertDialog.Builder(getParentActivity());
                    b.setTitle("Whisper Engine");
                    b.setMessage(info);
                    b.setPositiveButton("OK", null);
                    b.show();
                }
            }
        });
        whGroup.addView(whCell);

        LinearLayout.LayoutParams whp = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
        whp.bottomMargin = AndroidUtilities.dp(16);
        root.addView(whGroup, whp);

        scroll.addView(root, new android.widget.FrameLayout.LayoutParams(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        fragmentView = scroll;
        return fragmentView;
    }

    private org.telegram.messenger.TranslateController getTranslateController() {
        return getMessagesController().getTranslateController();
    }

    private String targetLangName() {
        String lang = org.telegram.ui.Components.TranslateAlert2.getToLanguage();
        if (lang == null || lang.equals("app")) {
            return "Follow App";
        }
        return org.telegram.ui.Components.TranslateAlert2.capitalFirst(org.telegram.ui.Components.TranslateAlert2.languageName(lang));
    }

    private String restrictedSummary() {
        java.util.HashSet<String> langs = RestrictedLanguagesSelectActivity.getRestrictedLanguages();
        if (langs == null || langs.isEmpty()) {
            return "None";
        }
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String l : langs) {
            if (count >= 3) { sb.append(", ..."); break; }
            if (sb.length() > 0) sb.append(", ");
            sb.append(org.telegram.ui.Components.TranslateAlert2.capitalFirst(org.telegram.ui.Components.TranslateAlert2.languageName(l)));
            count++;
        }
        return sb.toString();
    }

    private void showTargetLanguagePicker(TextCell cell) {
        if (getParentActivity() == null) return;
        final String[] codes = {"app", "en", "tr", "ru", "de", "fr", "es", "ar", "it", "pt", "zh", "ja", "ko"};
        final String[] names = new String[codes.length];
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals("app")) {
                names[i] = "Follow App";
            } else {
                java.util.Locale loc = new java.util.Locale(codes[i]);
                String n = loc.getDisplayLanguage(loc);
                if (n == null || n.length() == 0) n = codes[i];
                names[i] = n.substring(0, 1).toUpperCase(loc) + (n.length() > 1 ? n.substring(1) : "");
            }
        }
        org.telegram.ui.ActionBar.AlertDialog.Builder builder = new org.telegram.ui.ActionBar.AlertDialog.Builder(getParentActivity());
        builder.setTitle("Target Language");
        builder.setItems(names, (dialog, which) -> {
            String code = codes[which];
            if (code.equals("app")) {
                org.telegram.ui.Components.TranslateAlert2.resetToLanguage();
            } else {
                org.telegram.ui.Components.TranslateAlert2.setToLanguage(code);
            }
            cell.setTextAndValue("Target Language", targetLangName(), true);
        });
        builder.setNegativeButton(org.telegram.messenger.LocaleController.getString(R.string.Cancel), null);
        builder.show();
    }

    private String clearStorageText() {
        long size = org.telegram.messenger.LyrxDeletedStorage.totalSize();
        return "Clear Deleted Storage (" + org.telegram.messenger.LyrxDeletedStorage.formatSize(size) + ")";
    }

    private View divider(Context context) {
        View div = new View(context);
        div.setBackgroundColor(Theme.getColor(Theme.key_divider));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LayoutHelper.MATCH_PARENT, 1);
        p.leftMargin = AndroidUtilities.dp(20);
        div.setLayoutParams(p);
        return div;
    }
}
