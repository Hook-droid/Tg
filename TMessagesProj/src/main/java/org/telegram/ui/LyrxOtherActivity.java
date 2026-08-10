package org.telegram.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Components.LayoutHelper;

public class LyrxOtherActivity extends BaseFragment {

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle("Other");
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

        LinearLayout supportGroup = group(context);
        TextCell stars = new TextCell(context);
        stars.setTextAndIcon("Stars", R.drawable.msg_premium_liststar, false);
        stars.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putLong("user_id", 8760170705L);
            presentFragment(new ProfileActivity(args));
        });
        supportGroup.addView(stars);
        root.addView(supportGroup, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        LinearLayout accountGroup = group(context);
        LinearLayout.LayoutParams agp = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
        agp.topMargin = AndroidUtilities.dp(12);

        TextCell deleteCell = new TextCell(context);
        deleteCell.setTextAndIcon("Delete Account", R.drawable.msg_delete, false);
        deleteCell.setColors(Theme.key_text_RedRegular, Theme.key_text_RedRegular);
        deleteCell.setOnClickListener(v -> showDeleteDialog());
        accountGroup.addView(deleteCell);

        root.addView(accountGroup, agp);

        scroll.addView(root, new android.widget.FrameLayout.LayoutParams(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        fragmentView = scroll;
        return fragmentView;
    }

    private void showDeleteDialog() {
        if (getParentActivity() == null) return;
        org.telegram.ui.ActionBar.AlertDialog.Builder builder = new org.telegram.ui.ActionBar.AlertDialog.Builder(getParentActivity());
        builder.setTitle("Delete Account");
        builder.setMessage("Warning: This will irreversibly delete your Telegram account and all data stored in the Telegram cloud.");
        builder.setPositiveButton("DELETE NOW", (dialog, which) -> {
            org.telegram.tgnet.tl.TL_account.deleteAccount req = new org.telegram.tgnet.tl.TL_account.deleteAccount();
            req.reason = "";
            org.telegram.tgnet.ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                if (error == null) {
                    org.telegram.messenger.MessagesController.getInstance(currentAccount).performLogout(0);
                } else {
                    if (getParentActivity() != null) {
                        org.telegram.ui.Components.BulletinFactory.of(this).createErrorBulletin(error.text).show();
                    }
                }
            }));
        });
        builder.setNegativeButton(org.telegram.messenger.LocaleController.getString(R.string.Cancel), null);
        org.telegram.ui.ActionBar.AlertDialog dialog = builder.create();
        dialog.show();
        View posView = dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE);
        if (posView instanceof android.widget.TextView) {
            android.widget.TextView positive = (android.widget.TextView) posView;
            positive.setEnabled(false);
            positive.setAlpha(0.5f);
            final int[] seconds = {30};
            final String baseText = "DELETE NOW";
            positive.setText(baseText + " " + seconds[0]);
            final Runnable[] tick = new Runnable[1];
            tick[0] = () -> {
                seconds[0]--;
                if (seconds[0] <= 0) {
                    positive.setEnabled(true);
                    positive.setAlpha(1f);
                    positive.setText(baseText);
                } else {
                    positive.setText(baseText + " " + seconds[0]);
                    AndroidUtilities.runOnUIThread(tick[0], 1000);
                }
            };
            AndroidUtilities.runOnUIThread(tick[0], 1000);
        }
    }

    private LinearLayout group(Context context) {
        LinearLayout g = new LinearLayout(context);
        g.setOrientation(LinearLayout.VERTICAL);
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        bg.setCornerRadius(AndroidUtilities.dp(16));
        g.setBackground(bg);
        return g;
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
