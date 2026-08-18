package org.telegram.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Components.LayoutHelper;

public class LyrxOtherActivity extends BaseFragment {

    private static final int COLOR_ON = 0xFF4CD964;
    private static final int COLOR_OFF = 0xFFFF3B30;

    private StatusCircle premiumStatus;

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

        LinearLayout premiumGroup = group(context);
        TextCell premiumCell = new TextCell(context);
        premiumCell.setTextAndIcon("Free Premium", R.drawable.msg_premium_liststar, false);
        premiumStatus = new StatusCircle(context);
        premiumStatus.setActive(SharedConfig.lyrxFreePremium);
        premiumCell.addView(premiumStatus, LayoutHelper.createFrame(26, 26, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 20, 0));
        premiumCell.setOnClickListener(v -> showPremiumDialog());
        premiumGroup.addView(premiumCell);
        LinearLayout.LayoutParams pgp = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
        pgp.bottomMargin = AndroidUtilities.dp(12);
        root.addView(premiumGroup, pgp);

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

    private void showPremiumDialog() {
        if (getParentActivity() == null) return;
        final boolean turningOn = !SharedConfig.lyrxFreePremium;
        org.telegram.ui.ActionBar.AlertDialog.Builder builder = new org.telegram.ui.ActionBar.AlertDialog.Builder(getParentActivity());
        builder.setTitle("Free Premium");
        if (turningOn) {
            builder.setMessage("This unlocks every Premium feature your phone draws by itself: unlimited folders and pinned chats, bigger animated stickers and effects, Premium-only interface settings, the Premium badge on your own screen, and no more subscribe popups.\n\nIt only changes what YOU see. Other people still see a normal account, because their app draws the badge from Telegram's server and the server does not know about this switch.\n\nAnything the server verifies stays locked: 4 GB uploads, voice to text, unique reactions and emoji status. Those will still fail with an error. No mod can change them.");
        } else {
            builder.setMessage("Free Premium will be turned off. Premium interface features go back to locked.");
        }
        builder.setNegativeButton(org.telegram.messenger.LocaleController.getString(R.string.Cancel), null);
        builder.setPositiveButton("OK", (dialog, which) -> {
            SharedConfig.lyrxFreePremium = turningOn;
            MessagesController.getGlobalMainSettings().edit().putBoolean("lyrxFreePremium", turningOn).apply();
            applyPremiumToAccounts(turningOn);
            if (premiumStatus != null) {
                premiumStatus.setActive(turningOn);
            }
        });
        org.telegram.ui.ActionBar.AlertDialog dialog = builder.create();
        dialog.show();
        View posView = dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE);
        if (posView instanceof android.widget.TextView) {
            android.widget.TextView positive = (android.widget.TextView) posView;
            positive.setEnabled(false);
            positive.setAlpha(0.5f);
            final int[] seconds = {3};
            positive.setText("OK " + seconds[0]);
            final Runnable[] tick = new Runnable[1];
            tick[0] = () -> {
                seconds[0]--;
                if (seconds[0] <= 0) {
                    positive.setEnabled(true);
                    positive.setAlpha(1f);
                    positive.setText("OK");
                    positive.setTextColor(COLOR_ON);
                } else {
                    positive.setText("OK " + seconds[0]);
                    AndroidUtilities.runOnUIThread(tick[0], 1000);
                }
            };
            AndroidUtilities.runOnUIThread(tick[0], 1000);
        }
    }

    private void applyPremiumToAccounts(boolean enabled) {
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            try {
                UserConfig config = UserConfig.getInstance(a);
                if (config == null || !config.isClientActivated()) {
                    continue;
                }
                TLRPC.User user = config.getCurrentUser();
                if (user != null) {
                    user.premium = enabled;
                }
            } catch (Throwable ignore) {
            }
        }
    }

    private static class StatusCircle extends View {

        private final Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint markPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private boolean active;

        StatusCircle(Context context) {
            super(context);
            circlePaint.setStyle(Paint.Style.FILL);
            markPaint.setStyle(Paint.Style.STROKE);
            markPaint.setColor(0xFFFFFFFF);
            markPaint.setStrokeCap(Paint.Cap.ROUND);
            markPaint.setStrokeJoin(Paint.Join.ROUND);
            markPaint.setStrokeWidth(AndroidUtilities.dp(2.2f));
        }

        void setActive(boolean value) {
            active = value;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float radius = Math.min(cx, cy);
            circlePaint.setColor(active ? COLOR_ON : COLOR_OFF);
            canvas.drawCircle(cx, cy, radius, circlePaint);

            path.reset();
            if (active) {
                float unit = radius * 0.5f;
                path.moveTo(cx - unit, cy);
                path.lineTo(cx - unit * 0.2f, cy + unit * 0.65f);
                path.lineTo(cx + unit, cy - unit * 0.55f);
            } else {
                float unit = radius * 0.42f;
                path.moveTo(cx - unit, cy - unit);
                path.lineTo(cx + unit, cy + unit);
                path.moveTo(cx + unit, cy - unit);
                path.lineTo(cx - unit, cy + unit);
            }
            canvas.drawPath(path, markPaint);
        }
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
