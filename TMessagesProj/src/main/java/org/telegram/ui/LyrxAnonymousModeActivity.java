package org.telegram.ui;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;

public class LyrxAnonymousModeActivity extends BaseFragment {

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle("Anonymous Mode");
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

        LinearLayout group = new LinearLayout(context);
        group.setOrientation(LinearLayout.VERTICAL);
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        bg.setCornerRadius(AndroidUtilities.dp(16));
        group.setBackground(bg);

        group.addView(makeCheck(context, "No Typing Indicator", "Don't show your typing status", SharedConfig.lyrxHideTyping, checked -> {
            SharedConfig.lyrxHideTyping = checked;
            save("lyrxHideTyping", checked);
        }));
        group.addView(divider(context));
        group.addView(makeCheck(context, "Invisible Mode", "Don't show your online status", SharedConfig.lyrxInvisibleMode, checked -> {
            SharedConfig.lyrxInvisibleMode = checked;
            save("lyrxInvisibleMode", checked);
        }));
        group.addView(divider(context));
        group.addView(makeCheck(context, "Unread Messages", "Don't send read receipts", SharedConfig.lyrxDontSendRead, checked -> {
            SharedConfig.lyrxDontSendRead = checked;
            save("lyrxDontSendRead", checked);
        }));
        group.addView(divider(context));
        group.addView(makeCheck(context, "Full Anonymous", "Block screenshots and recording", SharedConfig.lyrxAnonymousMode, checked -> {
            SharedConfig.lyrxAnonymousMode = checked;
            save("lyrxAnonymousMode", checked);
        }));

        root.addView(group, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        LinearLayout muteGroup = new LinearLayout(context);
        muteGroup.setOrientation(LinearLayout.VERTICAL);
        android.graphics.drawable.GradientDrawable mbg = new android.graphics.drawable.GradientDrawable();
        mbg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        mbg.setCornerRadius(AndroidUtilities.dp(16));
        muteGroup.setBackground(mbg);
        TextCheckCell muteCell = new TextCheckCell(context);
        muteCell.setTextAndValueAndCheck("Mute", "Auto-delete messages from blacklisted users", SharedConfig.lyrxMuteEnabled, true, false);
        muteCell.setOnClickListener(v -> presentFragment(new LyrxMuteActivity()));
        muteGroup.addView(muteCell);
        LinearLayout.LayoutParams mp = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
        mp.topMargin = AndroidUtilities.dp(12);
        root.addView(muteGroup, mp);

        scroll.addView(root, new android.widget.FrameLayout.LayoutParams(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        fragmentView = scroll;
        return fragmentView;
    }

    private interface OnCheck { void run(boolean checked); }

    private TextCheckCell makeCheck(Context context, String title, String subtitle, boolean checked, OnCheck listener) {
        TextCheckCell cell = new TextCheckCell(context);
        cell.setTextAndValueAndCheck(title, subtitle, checked, true, false);
        cell.setOnClickListener(v -> {
            boolean ns = !cell.isChecked();
            cell.setChecked(ns);
            if (listener != null) listener.run(ns);
        });
        return cell;
    }

    private View divider(Context context) {
        View div = new View(context);
        div.setBackgroundColor(Theme.getColor(Theme.key_divider));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LayoutHelper.MATCH_PARENT, 1);
        p.leftMargin = AndroidUtilities.dp(20);
        div.setLayoutParams(p);
        return div;
    }

    private void save(String key, boolean value) {
        MessagesController.getGlobalMainSettings().edit().putBoolean(key, value).apply();
    }
}
