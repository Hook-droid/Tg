package org.telegram.ui;

import android.content.Context;
import android.view.View;
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
        LinearLayout.LayoutParams sgp = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
        sgp.bottomMargin = AndroidUtilities.dp(16);
        root.addView(speedGroup, sgp);

        LinearLayout group = new LinearLayout(context);
        group.setOrientation(LinearLayout.VERTICAL);
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        bg.setCornerRadius(AndroidUtilities.dp(16));
        group.setBackground(bg);

        TextCheckCell showDeleted = new TextCheckCell(context);
        showDeleted.setTextAndValueAndCheck("Show Deleted", "Show deleted messages inside LyrxGram", SharedConfig.lyrxShowDeleted, true, false);
        showDeleted.setOnClickListener(v -> presentFragment(new LyrxDeletedSettingsActivity()));
        group.addView(showDeleted);

        group.addView(divider(context));

        TextCell searchId = new TextCell(context);
        searchId.setTextAndIcon("Searching ID", R.drawable.msg_search, false);
        searchId.setOnClickListener(v -> presentFragment(new LyrxSearchIdActivity()));
        group.addView(searchId);

        root.addView(group, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

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
            names[i] = codes[i].equals("app") ? "Follow App" : org.telegram.ui.Components.TranslateAlert2.capitalFirst(org.telegram.ui.Components.TranslateAlert2.languageName(codes[i]));
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

    private View divider(Context context) {
        View div = new View(context);
        div.setBackgroundColor(Theme.getColor(Theme.key_divider));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LayoutHelper.MATCH_PARENT, 1);
        p.leftMargin = AndroidUtilities.dp(20);
        div.setLayoutParams(p);
        return div;
    }
}
