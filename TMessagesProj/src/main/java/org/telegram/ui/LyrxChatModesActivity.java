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

    private TextView voiceValue;
    private android.widget.FrameLayout voiceRow;

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
        limitCell.setTextAndValueAndCheck("Remove Join Limit", "Auto-retry channel joins when Telegram rate limits you", SharedConfig.lyrxBypassJoinLimit, true, false);
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

        TextView voiceHeader = new TextView(context);
        voiceHeader.setText("Sound Editor");
        voiceHeader.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        voiceHeader.setTextSize(15);
        voiceHeader.setTypeface(AndroidUtilities.bold());
        voiceHeader.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(8));
        root.addView(voiceHeader, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        LinearLayout voiceGroup = new LinearLayout(context);
        voiceGroup.setOrientation(LinearLayout.VERTICAL);
        android.graphics.drawable.GradientDrawable voiceBg = new android.graphics.drawable.GradientDrawable();
        voiceBg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        voiceBg.setCornerRadius(AndroidUtilities.dp(16));
        voiceGroup.setBackground(voiceBg);

        voiceRow = new android.widget.FrameLayout(context);

        android.widget.ImageView voiceIcon = new android.widget.ImageView(context);
        voiceIcon.setImageResource(R.drawable.msg_voice_unmuted);
        voiceIcon.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
        voiceIcon.setColorFilter(new android.graphics.PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), android.graphics.PorterDuff.Mode.SRC_IN));
        voiceRow.addView(voiceIcon, LayoutHelper.createFrame(24, 24, android.view.Gravity.LEFT | android.view.Gravity.CENTER_VERTICAL, 19, 0, 0, 0));

        TextView voiceTitle = new TextView(context);
        voiceTitle.setText("Voice Changer");
        voiceTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        voiceTitle.setTextSize(16);
        voiceRow.addView(voiceTitle, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, android.view.Gravity.LEFT | android.view.Gravity.CENTER_VERTICAL, 65, 0, 0, 0));

        android.widget.ImageView voiceArrow = new android.widget.ImageView(context);
        voiceArrow.setImageDrawable(new UpDownArrows(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon)));
        voiceRow.addView(voiceArrow, LayoutHelper.createFrame(14, 20, android.view.Gravity.RIGHT | android.view.Gravity.CENTER_VERTICAL, 0, 0, 20, 0));

        voiceValue = new TextView(context);
        voiceValue.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteValueText));
        voiceValue.setTextSize(15);
        voiceRow.addView(voiceValue, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, android.view.Gravity.RIGHT | android.view.Gravity.CENTER_VERTICAL, 0, 0, 44, 0));

        voiceRow.setOnClickListener(v -> showVoiceMenu());
        voiceGroup.addView(voiceRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 52));

        root.addView(voiceGroup, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        TextView voiceInfo = new TextView(context);
        voiceInfo.setText("The effect is applied while you record, so everyone hears it - not only you. Pick Off for your normal voice.");
        voiceInfo.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        voiceInfo.setTextSize(13);
        voiceInfo.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(10), AndroidUtilities.dp(16), AndroidUtilities.dp(6));
        root.addView(voiceInfo, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        updateVoiceSelection();

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

    private void updateVoiceSelection() {
        int index = SharedConfig.lyrxVoiceEffect;
        if (index < 0 || index >= org.telegram.messenger.LyrxVoiceChanger.NAMES.length) {
            index = 0;
        }
        if (voiceValue != null) {
            voiceValue.setText(org.telegram.messenger.LyrxVoiceChanger.NAMES[index]);
        }
    }

    private void showVoiceMenu() {
        if (getParentActivity() == null || voiceRow == null || fragmentView == null) {
            return;
        }
        org.telegram.ui.ActionBar.ActionBarPopupWindow.ActionBarPopupWindowLayout layout =
                new org.telegram.ui.ActionBar.ActionBarPopupWindow.ActionBarPopupWindowLayout(getParentActivity());

        final org.telegram.ui.ActionBar.ActionBarPopupWindow[] window = new org.telegram.ui.ActionBar.ActionBarPopupWindow[1];
        int count = org.telegram.messenger.LyrxVoiceChanger.NAMES.length;
        for (int i = 0; i < count; i++) {
            final int index = i;
            org.telegram.ui.ActionBar.ActionBarMenuSubItem item =
                    new org.telegram.ui.ActionBar.ActionBarMenuSubItem(getParentActivity(), i == 0, i == count - 1);
            item.setText(org.telegram.messenger.LyrxVoiceChanger.NAMES[i]);
            item.setChecked(SharedConfig.lyrxVoiceEffect == i);
            item.setOnClickListener(v -> {
                SharedConfig.lyrxVoiceEffect = index;
                MessagesController.getGlobalMainSettings().edit().putInt("lyrxVoiceEffect", index).apply();
                org.telegram.messenger.LyrxVoiceChanger.reset();
                updateVoiceSelection();
                if (window[0] != null) {
                    window[0].dismiss();
                }
            });
            layout.addView(item, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
        }

        window[0] = new org.telegram.ui.ActionBar.ActionBarPopupWindow(layout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
        window[0].setOutsideTouchable(true);
        window[0].setFocusable(true);
        window[0].setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        window[0].setAnimationStyle(R.style.PopupContextAnimation);
        window[0].setInputMethodMode(org.telegram.ui.ActionBar.ActionBarPopupWindow.INPUT_METHOD_NOT_NEEDED);
        window[0].setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED);
        layout.setDispatchKeyEventListener(keyEvent -> {
            if (keyEvent.getKeyCode() == android.view.KeyEvent.KEYCODE_BACK && keyEvent.getRepeatCount() == 0 && window[0] != null && window[0].isShowing()) {
                window[0].dismiss();
            }
        });

        layout.measure(
                android.view.View.MeasureSpec.makeMeasureSpec(AndroidUtilities.displaySize.x, android.view.View.MeasureSpec.AT_MOST),
                android.view.View.MeasureSpec.makeMeasureSpec(AndroidUtilities.displaySize.y, android.view.View.MeasureSpec.AT_MOST));

        int[] location = new int[2];
        voiceRow.getLocationInWindow(location);
        int menuHeight = layout.getMeasuredHeight();
        int menuWidth = layout.getMeasuredWidth();

        int spaceBelow = AndroidUtilities.displaySize.y - (location[1] + voiceRow.getHeight());
        int y;
        if (menuHeight + AndroidUtilities.dp(16) <= spaceBelow) {
            y = location[1] + voiceRow.getHeight() - AndroidUtilities.dp(4);
        } else if (menuHeight + AndroidUtilities.dp(16) <= location[1]) {
            y = location[1] - menuHeight + AndroidUtilities.dp(4);
        } else {
            y = Math.max(AndroidUtilities.dp(8), (AndroidUtilities.displaySize.y - menuHeight) / 2);
        }

        int x = location[0] + voiceRow.getWidth() - menuWidth - AndroidUtilities.dp(12);
        if (x < AndroidUtilities.dp(8)) {
            x = AndroidUtilities.dp(8);
        }

        window[0].showAtLocation(fragmentView, android.view.Gravity.TOP | android.view.Gravity.LEFT, x, y);
    }

    private static class UpDownArrows extends android.graphics.drawable.Drawable {

        private final android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        private final android.graphics.Path path = new android.graphics.Path();

        UpDownArrows(int color) {
            paint.setColor(color);
            paint.setStyle(android.graphics.Paint.Style.STROKE);
            paint.setStrokeWidth(AndroidUtilities.dp(1.6f));
            paint.setStrokeCap(android.graphics.Paint.Cap.ROUND);
            paint.setStrokeJoin(android.graphics.Paint.Join.ROUND);
        }

        @Override
        public void draw(android.graphics.Canvas canvas) {
            android.graphics.Rect bounds = getBounds();
            float cx = bounds.centerX();
            float cy = bounds.centerY();
            float w = AndroidUtilities.dp(4.5f);
            float gap = AndroidUtilities.dp(3f);
            float h = AndroidUtilities.dp(3.5f);

            path.reset();
            path.moveTo(cx - w, cy - gap);
            path.lineTo(cx, cy - gap - h);
            path.lineTo(cx + w, cy - gap);

            path.moveTo(cx - w, cy + gap);
            path.lineTo(cx, cy + gap + h);
            path.lineTo(cx + w, cy + gap);
            canvas.drawPath(path, paint);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(android.graphics.ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return android.graphics.PixelFormat.TRANSLUCENT;
        }

        @Override
        public int getIntrinsicWidth() {
            return AndroidUtilities.dp(14);
        }

        @Override
        public int getIntrinsicHeight() {
            return AndroidUtilities.dp(20);
        }
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
