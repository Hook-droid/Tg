package org.telegram.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class LyrxGramActivity extends BaseFragment {

    @Override
    public View createView(Context context) {
        if (actionBar != null) {
            actionBar.setVisibility(View.GONE);
        }

        FrameLayout root = new FrameLayout(context);
        root.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        TextView header = new TextView(context);
        header.setText("LyrxGram");
        header.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        header.setTextSize(24);
        header.setTypeface(AndroidUtilities.bold());
        root.addView(header, LayoutHelper.createFrame(
                LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,
                Gravity.TOP | Gravity.LEFT, 20, 24, 20, 0));

        LinearLayout center = new LinearLayout(context);
        center.setOrientation(LinearLayout.VERTICAL);
        center.setGravity(Gravity.CENTER_HORIZONTAL);

        FrameLayout logoWrap = new FrameLayout(context);

        View glow = new View(context) {
            private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            {
                setWillNotDraw(false);
            }
            @Override
            protected void onDraw(Canvas canvas) {
                float cx = getWidth() / 2f;
                float cy = getHeight() / 2f;
                float r = Math.min(cx, cy);
                glowPaint.setShader(new RadialGradient(cx, cy, r,
                        new int[]{0x551D1330, 0x00000000},
                        new float[]{0f, 1f}, Shader.TileMode.CLAMP));
                canvas.drawCircle(cx, cy, r, glowPaint);
            }
        };
        logoWrap.addView(glow, LayoutHelper.createFrame(180, 180, Gravity.CENTER));

        FrameLayout logoCircle = new FrameLayout(context);
        GradientDrawable circleBg = new GradientDrawable();
        circleBg.setShape(GradientDrawable.OVAL);
        circleBg.setColor(0xFF1D1330);
        logoCircle.setBackground(circleBg);

        ImageView plane = new ImageView(context);
        plane.setImageResource(R.drawable.intro_tg_plane);
        plane.setColorFilter(0xFFFFFFFF);
        logoCircle.addView(plane, LayoutHelper.createFrame(60, 54, Gravity.CENTER, 2, 0, 0, 2));

        logoWrap.addView(logoCircle, LayoutHelper.createFrame(120, 120, Gravity.CENTER));

        center.addView(logoWrap, LayoutHelper.createLinear(180, 180, Gravity.CENTER_HORIZONTAL));

        TextView title = new TextView(context);
        title.setText("LyrxGram");
        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        title.setTextSize(26);
        title.setTypeface(AndroidUtilities.bold());
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = LayoutHelper.createLinear(
                LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
        titleParams.topMargin = AndroidUtilities.dp(4);
        center.addView(title, titleParams);

        TextView subtitle = new TextView(context);
        subtitle.setText("Modification");
        subtitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        subtitle.setTextSize(15);
        subtitle.setAlpha(0.6f);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subParams = LayoutHelper.createLinear(
                LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
        subParams.topMargin = AndroidUtilities.dp(6);
        center.addView(subtitle, subParams);

        center.addView(createProxyCard(context), menuParams(28));

        root.addView(center, LayoutHelper.createFrame(
                LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 40, 0, 0));

        fragmentView = root;
        return fragmentView;
    }

    private FrameLayout createProxyCard(Context context) {
        FrameLayout card = new FrameLayout(context);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(0xFF1C1C1E);
        cardBg.setCornerRadius(AndroidUtilities.dp(24));
        cardBg.setStroke(AndroidUtilities.dp(1), 0x40FFFFFF);
        card.setBackground(cardBg);

        FrameLayout iconBox = new FrameLayout(context);
        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setColor(0xFF2C2C2E);
        iconBg.setCornerRadius(AndroidUtilities.dp(16));
        iconBg.setStroke(AndroidUtilities.dp(1), 0x40FFFFFF);
        iconBox.setBackground(iconBg);
        ImageView icon = new ImageView(context);
        icon.setImageResource(R.drawable.menu_privacy_policy);
        icon.setColorFilter(0xFFFFFFFF);
        iconBox.addView(icon, LayoutHelper.createFrame(28, 28, Gravity.CENTER));
        card.addView(iconBox, LayoutHelper.createFrame(48, 48, Gravity.LEFT | Gravity.CENTER_VERTICAL, 16, 0, 0, 0));

        LinearLayout textCol = new LinearLayout(context);
        textCol.setOrientation(LinearLayout.VERTICAL);
        TextView t = new TextView(context);
        t.setText("Free Proxy");
        t.setTextColor(0xFFFFFFFF);
        t.setTextSize(17);
        t.setTypeface(AndroidUtilities.bold());
        TextView d = new TextView(context);
        d.setText("Wait 5-10 Seconds To Connect");
        d.setTextColor(0xFF9E9E9E);
        d.setTextSize(13);
        TextView s = new TextView(context);
        s.setText("Inactive");
        s.setTextColor(0xFF9E9E9E);
        s.setTextSize(13);
        textCol.addView(t);
        textCol.addView(d);
        textCol.addView(s);
        card.addView(textCol, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,
                Gravity.LEFT | Gravity.CENTER_VERTICAL, 76, 10, 66, 10));

        org.telegram.ui.Components.Switch proxySwitch = new org.telegram.ui.Components.Switch(context);
        proxySwitch.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_switchTrackBlueThumb, Theme.key_switchTrackBlueThumbChecked);
        proxySwitch.setChecked(false, false);
        card.addView(proxySwitch, LayoutHelper.createFrame(37, 20, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 16, 0));

        card.setMinimumHeight(AndroidUtilities.dp(80));
        return card;
    }

    private LinearLayout.LayoutParams menuParams(int topMarginDp) {
        LinearLayout.LayoutParams p = LayoutHelper.createLinear(
                LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
        p.leftMargin = AndroidUtilities.dp(20);
        p.rightMargin = AndroidUtilities.dp(20);
        p.topMargin = AndroidUtilities.dp(topMarginDp);
        return p;
    }

    private FrameLayout createMenuCard(Context context, int iconRes, String titleText, String descText) {
        FrameLayout card = new FrameLayout(context);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(0x0DFFFFFF);
        cardBg.setCornerRadius(AndroidUtilities.dp(20));
        cardBg.setStroke(AndroidUtilities.dp(1), 0x20FFFFFF);
        card.setBackground(cardBg);

        FrameLayout iconBox = new FrameLayout(context);
        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setColor(0x14FFFFFF);
        iconBg.setCornerRadius(AndroidUtilities.dp(14));
        iconBox.setBackground(iconBg);
        ImageView icon = new ImageView(context);
        icon.setImageResource(iconRes);
        icon.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        iconBox.addView(icon, LayoutHelper.createFrame(22, 22, Gravity.CENTER));
        card.addView(iconBox, LayoutHelper.createFrame(44, 44, Gravity.LEFT | Gravity.CENTER_VERTICAL, 14, 0, 0, 0));

        LinearLayout textCol = new LinearLayout(context);
        textCol.setOrientation(LinearLayout.VERTICAL);
        TextView t = new TextView(context);
        t.setText(titleText);
        t.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        t.setTextSize(16);
        t.setTypeface(AndroidUtilities.bold());
        TextView d = new TextView(context);
        d.setText(descText);
        d.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        d.setTextSize(13);
        textCol.addView(t);
        textCol.addView(d);
        card.addView(textCol, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,
                Gravity.LEFT | Gravity.CENTER_VERTICAL, 72, 12, 44, 12));

        ImageView arrow = new ImageView(context);
        arrow.setImageResource(R.drawable.msg_arrowright);
        arrow.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        card.addView(arrow, LayoutHelper.createFrame(18, 18, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 16, 0));

        card.setMinimumHeight(AndroidUtilities.dp(68));
        return card;
    }
}
