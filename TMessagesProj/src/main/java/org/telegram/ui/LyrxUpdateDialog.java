package org.telegram.ui;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LyrxUpdater;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class LyrxUpdateDialog extends Dialog implements org.telegram.messenger.NotificationCenter.NotificationCenterDelegate {

    private static final int ACCENT = 0xFFFFC240;

    private final Activity parentActivity;

    private LinearLayout contentColumn;
    private TextView actionButton;
    private TextView statusText;
    private ProgressBarView progressBar;
    private boolean downloading;

    public LyrxUpdateDialog(Activity activity) {
        super(activity, R_style());
        parentActivity = activity;
    }

    private static int R_style() {
        return android.R.style.Theme_Translucent_NoTitleBar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(0xB3000000));
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(params);
        }
        setCanceledOnTouchOutside(false);

        FrameLayout root = new FrameLayout(getContext());

        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Theme.getColor(Theme.key_dialogBackground));
        cardBg.setCornerRadius(AndroidUtilities.dp(22));
        cardBg.setStroke(AndroidUtilities.dp(1), 0x33FFC240);
        card.setBackground(cardBg);
        card.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(22), AndroidUtilities.dp(22), AndroidUtilities.dp(18));

        View accentBar = new View(getContext());
        GradientDrawable accentBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{ACCENT, 0xFFFF8A3D});
        accentBg.setCornerRadius(AndroidUtilities.dp(3));
        accentBar.setBackground(accentBg);
        card.addView(accentBar, LayoutHelper.createLinear(46, 5));

        TextView title = new TextView(getContext());
        title.setText("Update LyrxGram");
        title.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        title.setTextSize(22);
        title.setTypeface(AndroidUtilities.bold());
        LinearLayout.LayoutParams titleParams = LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
        titleParams.topMargin = AndroidUtilities.dp(14);
        card.addView(title, titleParams);

        TextView version = new TextView(getContext());
        version.setText("Version " + LyrxUpdater.latestVersion);
        version.setTextColor(ACCENT);
        version.setTextSize(13);
        version.setTypeface(AndroidUtilities.bold());
        LinearLayout.LayoutParams versionParams = LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
        versionParams.topMargin = AndroidUtilities.dp(3);
        card.addView(version, versionParams);

        contentColumn = new LinearLayout(getContext());
        contentColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams columnParams = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
        columnParams.topMargin = AndroidUtilities.dp(16);
        card.addView(contentColumn, columnParams);

        for (String change : LyrxUpdater.CHANGES) {
            addLine(change);
        }

        progressBar = new ProgressBarView(getContext());
        progressBar.setVisibility(View.GONE);
        LinearLayout.LayoutParams progressParams = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 6);
        progressParams.topMargin = AndroidUtilities.dp(18);
        card.addView(progressBar, progressParams);

        statusText = new TextView(getContext());
        statusText.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
        statusText.setTextSize(13);
        statusText.setVisibility(View.GONE);
        LinearLayout.LayoutParams statusParams = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
        statusParams.topMargin = AndroidUtilities.dp(10);
        card.addView(statusText, statusParams);

        FrameLayout buttonRow = new FrameLayout(getContext());

        TextView laterButton = new TextView(getContext());
        laterButton.setText("Later");
        laterButton.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
        laterButton.setTextSize(15);
        laterButton.setPadding(AndroidUtilities.dp(10), AndroidUtilities.dp(10), AndroidUtilities.dp(10), AndroidUtilities.dp(10));
        laterButton.setOnClickListener(v -> {
            if (!downloading) {
                dismiss();
            }
        });
        buttonRow.addView(laterButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL));

        actionButton = new TextView(getContext());
        actionButton.setText("Update Now");
        actionButton.setTextColor(0xFF1B1B1B);
        actionButton.setTextSize(15);
        actionButton.setTypeface(AndroidUtilities.bold());
        actionButton.setGravity(Gravity.CENTER);
        GradientDrawable buttonBg = new GradientDrawable();
        buttonBg.setColor(ACCENT);
        buttonBg.setCornerRadius(AndroidUtilities.dp(12));
        actionButton.setBackground(buttonBg);
        actionButton.setPadding(AndroidUtilities.dp(22), 0, AndroidUtilities.dp(22), 0);
        actionButton.setOnClickListener(v -> startDownload());
        buttonRow.addView(actionButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 44, Gravity.RIGHT | Gravity.CENTER_VERTICAL));

        LinearLayout.LayoutParams rowParams = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44);
        rowParams.topMargin = AndroidUtilities.dp(20);
        card.addView(buttonRow, rowParams);

        root.addView(card, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 22, 0, 22, 0));
        setContentView(root);
    }

    private void addLine(String text) {
        LinearLayout line = new LinearLayout(getContext());
        line.setOrientation(LinearLayout.HORIZONTAL);

        TextView dash = new TextView(getContext());
        dash.setText("–");
        dash.setTextColor(ACCENT);
        dash.setTextSize(14);
        dash.setTypeface(AndroidUtilities.bold());
        line.addView(dash, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        TextView body = new TextView(getContext());
        body.setText(text);
        body.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        body.setTextSize(14);
        line.addView(body, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 8, 0, 0, 0));

        LinearLayout.LayoutParams params = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
        params.topMargin = AndroidUtilities.dp(7);
        contentColumn.addView(line, params);
    }

    private void startDownload() {
        if (downloading) {
            return;
        }
        downloading = true;
        actionButton.setAlpha(0.5f);
        actionButton.setText("Updating");
        contentColumn.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        statusText.setVisibility(View.VISIBLE);
        statusText.setText("LyrxGram Is Updating, Please Wait   0%");

        int account = org.telegram.messenger.UserConfig.selectedAccount;
        org.telegram.messenger.NotificationCenter.getInstance(account).addObserver(this, org.telegram.messenger.NotificationCenter.fileLoadProgressChanged);
        org.telegram.messenger.NotificationCenter.getInstance(account).addObserver(this, org.telegram.messenger.NotificationCenter.fileLoaded);
        org.telegram.messenger.NotificationCenter.getInstance(account).addObserver(this, org.telegram.messenger.NotificationCenter.fileLoadFailed);

        java.io.File ready = LyrxUpdater.getDownloadedFile();
        if (ready != null) {
            finishDownload(ready);
            return;
        }
        LyrxUpdater.startDownload();
    }

    private void finishDownload(java.io.File file) {
        statusText.setText("Download Complete, Starting Installer");
        progressBar.setProgress(1f);
        LyrxUpdater.install(parentActivity, file);
        downloading = false;
        removeObservers();
        dismiss();
    }

    private void removeObservers() {
        int account = org.telegram.messenger.UserConfig.selectedAccount;
        org.telegram.messenger.NotificationCenter.getInstance(account).removeObserver(this, org.telegram.messenger.NotificationCenter.fileLoadProgressChanged);
        org.telegram.messenger.NotificationCenter.getInstance(account).removeObserver(this, org.telegram.messenger.NotificationCenter.fileLoaded);
        org.telegram.messenger.NotificationCenter.getInstance(account).removeObserver(this, org.telegram.messenger.NotificationCenter.fileLoadFailed);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (!downloading) {
            return;
        }
        if (id == org.telegram.messenger.NotificationCenter.fileLoadProgressChanged) {
            try {
                Long loaded = (Long) args[1];
                Long total = (Long) args[2];
                if (total != null && total > 0) {
                    int percent = (int) (loaded * 100 / total);
                    progressBar.setProgress(percent / 100f);
                    statusText.setText("LyrxGram Is Updating, Please Wait   " + percent + "%");
                }
            } catch (Throwable ignore) {
            }
        } else if (id == org.telegram.messenger.NotificationCenter.fileLoaded) {
            java.io.File file = LyrxUpdater.getDownloadedFile();
            if (file != null) {
                finishDownload(file);
            }
        } else if (id == org.telegram.messenger.NotificationCenter.fileLoadFailed) {
            downloading = false;
            removeObservers();
            actionButton.setAlpha(1f);
            actionButton.setText("Try Again");
            progressBar.setVisibility(View.GONE);
            statusText.setText("Download Failed, Check Your Connection");
        }
    }

    @Override
    public void dismiss() {
        removeObservers();
        super.dismiss();
    }

    private static class ProgressBarView extends View {

        private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private float progress;
        private float shown;

        ProgressBarView(android.content.Context context) {
            super(context);
            trackPaint.setColor(0x22FFFFFF);
            fillPaint.setColor(ACCENT);
        }

        void setProgress(float value) {
            progress = value;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float radius = getHeight() / 2f;
            rect.set(0, 0, getWidth(), getHeight());
            canvas.drawRoundRect(rect, radius, radius, trackPaint);

            shown += (progress - shown) * 0.18f;
            if (Math.abs(progress - shown) < 0.002f) {
                shown = progress;
            }

            float width = getWidth() * shown;
            if (width > 0) {
                if (width < getHeight()) {
                    width = getHeight();
                }
                fillPaint.setShader(new LinearGradient(0, 0, width, 0,
                        new int[]{ACCENT, 0xFFFF8A3D}, null, Shader.TileMode.CLAMP));
                rect.set(0, 0, width, getHeight());
                canvas.drawRoundRect(rect, radius, radius, fillPaint);
            }

            if (shown != progress) {
                invalidate();
            }
        }
    }
}
