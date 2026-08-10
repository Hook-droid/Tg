package org.telegram.ui;

import android.content.Context;
import android.content.SharedPreferences;
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
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class LyrxGramActivity extends BaseFragment {

    private TextView proxyStatusView;
    private org.telegram.ui.Components.Switch proxySwitch;
    private boolean proxyConnecting;
    private java.util.ArrayList<String[]> proxyCandidates;
    private int proxyTryIndex;
    private SharedConfig.ProxyInfo activeProxyInfo;
    private Runnable proxyPingRunnable;
    private String liveProxyServer;
    private int liveProxyPort;
    private String liveProxySecret;
    private Runnable proxyDotRunnable;
    private int proxyDotCount;

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
        header.setTextSize(22);
        header.setTypeface(AndroidUtilities.bold());
        FrameLayout.LayoutParams headerParams = LayoutHelper.createFrame(
                LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT);
        headerParams.leftMargin = AndroidUtilities.dp(18);
        headerParams.topMargin = AndroidUtilities.dp(14) + AndroidUtilities.statusBarHeight;
        root.addView(header, headerParams);

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
                        new int[]{0xAA6A3DBF, 0x552D1B4E, 0x00000000},
                        new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
                canvas.drawCircle(cx, cy, r, glowPaint);
            }
        };
        logoWrap.addView(glow, LayoutHelper.createFrame(210, 210, Gravity.CENTER));

        FrameLayout logoCircle = new FrameLayout(context);
        GradientDrawable circleBg = new GradientDrawable();
        circleBg.setShape(GradientDrawable.OVAL);
        circleBg.setColor(0xFF1D1330);
        logoCircle.setBackground(circleBg);

        ImageView plane = new ImageView(context);
        plane.setImageResource(R.drawable.intro_tg_plane);
        plane.setColorFilter(0xFFFFFFFF);
        plane.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logoCircle.addView(plane, LayoutHelper.createFrame(56, 50, Gravity.CENTER, 4, 0, 0, 0));

        logoWrap.addView(logoCircle, LayoutHelper.createFrame(120, 120, Gravity.CENTER));

        center.addView(logoWrap, LayoutHelper.createLinear(210, 210, Gravity.CENTER_HORIZONTAL));

        TextView title = new TextView(context);
        title.setText("LyrxGram");
        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        title.setTextSize(26);
        title.setTypeface(AndroidUtilities.bold());
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = LayoutHelper.createLinear(
                LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
        titleParams.topMargin = AndroidUtilities.dp(2);
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

        center.addView(createProxyCard(context), menuParams(24));

        center.addView(createSectionHeader(context, "Categories"), sectionHeaderParams());
        LinearLayout catGroup = createGroupContainer(context);
        catGroup.addView(createRow(context, R.drawable.menu_privacy_policy, "Anonymous Mode", true, () -> presentFragment(new LyrxAnonymousModeActivity())));
        catGroup.addView(createDivider(context));
        catGroup.addView(createRow(context, R.drawable.msg_message, "Chat Modes", true, () -> presentFragment(new LyrxChatModesActivity())));
        catGroup.addView(createDivider(context));
        catGroup.addView(createRow(context, R.drawable.msg_fave, "Other", true, () -> presentFragment(new LyrxOtherActivity())));
        center.addView(catGroup, groupParams());

        center.addView(createSectionHeader(context, "Links"), sectionHeaderParams());
        LinearLayout linkGroup = createGroupContainer(context);
        linkGroup.addView(createLinkRow(context, R.drawable.msg_channel, "Channel", "@LyroxHacksOfficial", () -> Browser.openUrl(getContext(), "https://t.me/LyroxHacksOfficial")));
        linkGroup.addView(createDivider(context));
        linkGroup.addView(createLinkRow(context, R.drawable.msg_openprofile, "Owner", "@LyroxPy", () -> Browser.openUrl(getContext(), "https://t.me/LyroxPy")));
        center.addView(linkGroup, groupParams());

        android.widget.ScrollView scroll = new android.widget.ScrollView(context);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setClipToPadding(false);
        scroll.setPadding(0, 0, 0, AndroidUtilities.dp(140));
        center.setPadding(0, AndroidUtilities.dp(70) + AndroidUtilities.statusBarHeight, 0, 0);
        scroll.addView(center, new FrameLayout.LayoutParams(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        root.addView(scroll, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        fragmentView = root;
        return fragmentView;
    }

    private TextView createSectionHeader(Context context, String text) {
        TextView header = new TextView(context);
        header.setText(text);
        header.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        header.setTextSize(15);
        header.setTypeface(AndroidUtilities.bold());
        header.setPadding(AndroidUtilities.dp(28), 0, AndroidUtilities.dp(28), 0);
        return header;
    }

    private LinearLayout.LayoutParams sectionHeaderParams() {
        LinearLayout.LayoutParams p = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
        p.topMargin = AndroidUtilities.dp(22);
        p.bottomMargin = AndroidUtilities.dp(8);
        return p;
    }

    private LinearLayout createGroupContainer(Context context) {
        LinearLayout group = new LinearLayout(context);
        group.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        bg.setCornerRadius(AndroidUtilities.dp(16));
        group.setBackground(bg);
        return group;
    }

    private LinearLayout.LayoutParams groupParams() {
        LinearLayout.LayoutParams p = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
        p.leftMargin = AndroidUtilities.dp(12);
        p.rightMargin = AndroidUtilities.dp(12);
        return p;
    }

    private View createDivider(Context context) {
        View div = new View(context);
        div.setBackgroundColor(Theme.getColor(Theme.key_divider));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LayoutHelper.MATCH_PARENT, 1);
        p.leftMargin = AndroidUtilities.dp(60);
        div.setLayoutParams(p);
        return div;
    }

    private FrameLayout createRow(Context context, int iconRes, String title, boolean showArrow, Runnable onClick) {
        FrameLayout row = new FrameLayout(context);
        row.setBackground(Theme.getSelectorDrawable(false));

        ImageView icon = new ImageView(context);
        icon.setImageResource(iconRes);
        icon.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon));
        row.addView(icon, LayoutHelper.createFrame(24, 24, Gravity.LEFT | Gravity.CENTER_VERTICAL, 18, 0, 0, 0));

        TextView t = new TextView(context);
        t.setText(title);
        t.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        t.setTextSize(16);
        row.addView(t, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 60, 0, 50, 0));

        if (showArrow) {
            ImageView arrow = new ImageView(context);
            arrow.setImageResource(R.drawable.msg_arrowright);
            arrow.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon));
            arrow.setAlpha(0.5f);
            row.addView(arrow, LayoutHelper.createFrame(16, 16, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 18, 0));
        }

        row.setOnClickListener(v -> {
            if (onClick != null) onClick.run();
        });
        row.setMinimumHeight(AndroidUtilities.dp(52));
        return row;
    }

    private FrameLayout createLinkRow(Context context, int iconRes, String title, String value, Runnable onClick) {
        FrameLayout row = new FrameLayout(context);
        row.setBackground(Theme.getSelectorDrawable(false));

        ImageView icon = new ImageView(context);
        icon.setImageResource(iconRes);
        icon.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon));
        row.addView(icon, LayoutHelper.createFrame(24, 24, Gravity.LEFT | Gravity.CENTER_VERTICAL, 18, 0, 0, 0));

        TextView t = new TextView(context);
        t.setText(title);
        t.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        t.setTextSize(16);
        row.addView(t, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 60, 0, 0, 0));

        TextView v = new TextView(context);
        v.setText(value);
        v.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteValueText));
        v.setTextSize(16);
        row.addView(v, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 18, 0));

        row.setOnClickListener(view -> {
            if (onClick != null) onClick.run();
        });
        row.setMinimumHeight(AndroidUtilities.dp(52));
        return row;
    }

    private void saveFlag(String key, boolean value) {
        MessagesController.getGlobalMainSettings().edit().putBoolean(key, value).apply();
    }

    private FrameLayout createToggleCard(Context context, int iconRes, String titleText, String descText, boolean initialChecked, boolean deleteStyle, String flagKey, OnToggle listener) {
        FrameLayout card = new FrameLayout(context);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(0xFF1C1C1E);
        cardBg.setCornerRadius(AndroidUtilities.dp(24));
        cardBg.setStroke(Math.round(AndroidUtilities.dpf2(0.7f)), 0x22FFFFFF);
        card.setBackground(cardBg);

        FrameLayout iconBox = new FrameLayout(context);
        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setColor(0xFF2C2C2E);
        iconBg.setCornerRadius(AndroidUtilities.dp(16));
        iconBg.setStroke(Math.round(AndroidUtilities.dpf2(0.7f)), 0x22FFFFFF);
        iconBox.setBackground(iconBg);
        ImageView icon = new ImageView(context);
        icon.setImageResource(iconRes);
        icon.setColorFilter(0xFFFFFFFF);
        iconBox.addView(icon, LayoutHelper.createFrame(26, 26, Gravity.CENTER));
        card.addView(iconBox, LayoutHelper.createFrame(48, 48, Gravity.LEFT | Gravity.CENTER_VERTICAL, 16, 0, 0, 0));

        LinearLayout textCol = new LinearLayout(context);
        textCol.setOrientation(LinearLayout.VERTICAL);
        TextView t = new TextView(context);
        t.setText(titleText);
        t.setTextColor(0xFFFFFFFF);
        t.setTextSize(16);
        t.setTypeface(AndroidUtilities.bold());
        TextView d = new TextView(context);
        d.setText(descText);
        d.setTextColor(0xFF9E9E9E);
        d.setTextSize(13);
        textCol.addView(t);
        textCol.addView(d);
        card.addView(textCol, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,
                Gravity.LEFT | Gravity.CENTER_VERTICAL, 76, 10, 66, 10));

        org.telegram.ui.Components.Switch sw = new org.telegram.ui.Components.Switch(context);
        sw.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_switchTrackBlueThumb, Theme.key_switchTrackBlueThumbChecked);
        sw.setChecked(initialChecked, false);
        if (flagKey != null) {
            lyrxSwitches.put(flagKey, sw);
        }
        card.addView(sw, LayoutHelper.createFrame(37, 20, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 16, 0));

        card.setOnClickListener(v -> {
            boolean ns = !sw.isChecked();
            sw.setChecked(ns, true);
            if (listener != null) listener.onToggle(ns);
        });

        card.setMinimumHeight(AndroidUtilities.dp(80));
        return card;
    }

    private final java.util.HashMap<String, org.telegram.ui.Components.Switch> lyrxSwitches = new java.util.HashMap<>();

    @Override
    public void onResume() {
        super.onResume();
        try {
            org.telegram.ui.Components.Switch s;
            if ((s = lyrxSwitches.get("showDeleted")) != null) s.setChecked(SharedConfig.lyrxShowDeleted, true);
            if ((s = lyrxSwitches.get("hideTyping")) != null) s.setChecked(SharedConfig.lyrxHideTyping, true);
            if ((s = lyrxSwitches.get("invisible")) != null) s.setChecked(SharedConfig.lyrxInvisibleMode, true);
            if ((s = lyrxSwitches.get("dontSendRead")) != null) s.setChecked(SharedConfig.lyrxDontSendRead, true);
            if ((s = lyrxSwitches.get("mute")) != null) s.setChecked(SharedConfig.lyrxMuteEnabled, true);
        } catch (Exception ignore) {}
    }

    private interface OnToggle {
        void onToggle(boolean checked);
    }

    private FrameLayout createProxyCard(Context context) {
        FrameLayout card = new FrameLayout(context);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(0xFF1C1C1E);
        cardBg.setCornerRadius(AndroidUtilities.dp(24));
        cardBg.setStroke(Math.round(AndroidUtilities.dpf2(0.7f)), 0x22FFFFFF);
        card.setBackground(cardBg);

        FrameLayout iconBox = new FrameLayout(context);
        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setColor(0xFF2C2C2E);
        iconBg.setCornerRadius(AndroidUtilities.dp(16));
        iconBg.setStroke(Math.round(AndroidUtilities.dpf2(0.7f)), 0x22FFFFFF);
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
        proxyStatusView = new TextView(context);
        proxyStatusView.setText("Inactive");
        proxyStatusView.setTextColor(0xFF9E9E9E);
        proxyStatusView.setTextSize(13);
        textCol.addView(t);
        textCol.addView(d);
        textCol.addView(proxyStatusView);
        card.addView(textCol, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,
                Gravity.LEFT | Gravity.CENTER_VERTICAL, 78, 8, 68, 8));

        proxySwitch = new org.telegram.ui.Components.Switch(context);
        proxySwitch.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_switchTrackBlueThumb, Theme.key_switchTrackBlueThumbChecked);
        proxySwitch.setChecked(isProxyActive(), false);
        card.addView(proxySwitch, LayoutHelper.createFrame(37, 20, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 16, 0));

        if (isProxyActive()) {
            proxyStatusView.setTextColor(0xFF4CD964);
            proxyStatusView.setText("Active");
        }

        card.setOnClickListener(v -> {
            boolean newState = !proxySwitch.isChecked();
            proxySwitch.setChecked(newState, true);
            if (newState) {
                startProxyConnection();
            } else {
                stopProxyConnection();
            }
        });

        card.setMinimumHeight(AndroidUtilities.dp(80));
        return card;
    }

    private boolean isProxyActive() {
        SharedPreferences pref = MessagesController.getGlobalMainSettings();
        return pref.getBoolean("proxy_enabled", false);
    }

    private void startProxyConnection() {
        proxyConnecting = true;
        proxyStatusView.setTextColor(0xFFFFB020);
        startProxyDots();
        new Thread(() -> {
            java.util.ArrayList<String[]> list = new java.util.ArrayList<>();
            String[] urls = new String[]{
                "https://raw.githubusercontent.com/SoliSpirit/mtproto/master/all_proxies.txt",
                "https://raw.githubusercontent.com/SoliSpirit/mtproto/main/all_proxies.txt"
            };
            for (String u : urls) {
                if (!list.isEmpty()) break;
                try {
                    java.net.URL url = new java.net.URL(u);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(8000);
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (line.contains("server=") && line.contains("port=") && line.contains("secret=")) {
                            try {
                                String srv = line.substring(line.indexOf("server=") + 7);
                                srv = srv.substring(0, srv.indexOf("&"));
                                String portStr = line.substring(line.indexOf("port=") + 5);
                                portStr = portStr.substring(0, portStr.indexOf("&"));
                                String sec = line.substring(line.indexOf("secret=") + 7);
                                int amp = sec.indexOf("&");
                                if (amp > 0) sec = sec.substring(0, amp);
                                sec = sec.replace("%3D", "").replace("%3d", "").replace("=", "").trim();
                                srv = srv.trim();
                                if (srv.endsWith(".")) srv = srv.substring(0, srv.length() - 1);
                                int p = Integer.parseInt(portStr.trim());
                                if (srv.length() > 0 && sec.length() > 0) {
                                    list.add(new String[]{srv, String.valueOf(p), sec});
                                }
                            } catch (Exception ignore) {}
                        }
                    }
                    br.close();
                } catch (Exception ignore) {}
            }
            AndroidUtilities.runOnUIThread(() -> {
                if (list.isEmpty()) {
                    proxyFailed();
                } else {
                    proxyCandidates = list;
                    proxyTryIndex = 0;
                    tryNextProxy();
                }
            });
        }).start();
    }

    private void tryNextProxy() {
        if (!proxyConnecting) return;
        if (proxyCandidates == null || proxyTryIndex >= proxyCandidates.size() || proxyTryIndex >= 15) {
            proxyFailed();
            return;
        }
        String[] c = proxyCandidates.get(proxyTryIndex);
        proxyTryIndex++;
        final String server = c[0];
        final int port = Integer.parseInt(c[1]);
        final String secret = c[2];
        ConnectionsManager.getInstance(currentAccount).checkProxy(server, port, "", "", secret, time -> AndroidUtilities.runOnUIThread(() -> {
            if (!proxyConnecting) return;
            if (time < 0) {
                tryNextProxy();
            } else {
                activeProxyInfo = new SharedConfig.ProxyInfo(server, port, "", "", secret);
                SharedConfig.addProxy(activeProxyInfo);
                SharedConfig.currentProxy = activeProxyInfo;
                SharedPreferences pref = MessagesController.getGlobalMainSettings();
                pref.edit().putBoolean("proxy_enabled", true).commit();
                ConnectionsManager.setProxySettings(true, server, port, "", "", secret);
                stopProxyDots();
                proxyConnecting = false;
                proxyStatusView.setTextColor(0xFF4CD964);
                proxyStatusView.setText("Active (" + time + " ms)");
                liveProxyServer = server;
                liveProxyPort = port;
                liveProxySecret = secret;
                startLivePing();
            }
        }));
    }

    private void proxyFailed() {
        stopProxyDots();
        proxyConnecting = false;
        proxyStatusView.setTextColor(0xFFFF3B30);
        proxyStatusView.setText("Failed, try again");
        if (proxySwitch != null) proxySwitch.setChecked(false, true);
    }

    private void stopProxyConnection() {
        stopProxyDots();
        stopLivePing();
        activeProxyInfo = null;
        liveProxyServer = null;
        proxyConnecting = false;
        SharedPreferences pref = MessagesController.getGlobalMainSettings();
        pref.edit().putBoolean("proxy_enabled", false).commit();
        ConnectionsManager.setProxySettings(false, "", 0, "", "", "");
        proxyStatusView.setTextColor(0xFF9E9E9E);
        proxyStatusView.setText("Inactive");
    }

    private void startProxyDots() {
        proxyDotCount = 0;
        proxyDotRunnable = new Runnable() {
            @Override
            public void run() {
                if (!proxyConnecting) return;
                proxyDotCount = (proxyDotCount % 3) + 1;
                StringBuilder dots = new StringBuilder("Connecting");
                for (int i = 0; i < proxyDotCount; i++) dots.append(".");
                proxyStatusView.setText(dots.toString());
                AndroidUtilities.runOnUIThread(proxyDotRunnable, 400);
            }
        };
        AndroidUtilities.runOnUIThread(proxyDotRunnable, 400);
    }

    private void stopProxyDots() {
        if (proxyDotRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(proxyDotRunnable);
            proxyDotRunnable = null;
        }
    }

    private void startLivePing() {
        stopLivePing();
        proxyPingRunnable = new Runnable() {
            @Override
            public void run() {
                if (liveProxyServer == null) return;
                ConnectionsManager.getInstance(currentAccount).checkProxy(liveProxyServer, liveProxyPort, "", "", liveProxySecret, time -> AndroidUtilities.runOnUIThread(() -> {
                    if (liveProxyServer == null) return;
                    if (time >= 0) {
                        proxyStatusView.setTextColor(0xFF4CD964);
                        proxyStatusView.setText("Active (" + time + " ms)");
                    }
                    if (proxyPingRunnable != null) {
                        AndroidUtilities.runOnUIThread(proxyPingRunnable, 1000);
                    }
                }));
            }
        };
        AndroidUtilities.runOnUIThread(proxyPingRunnable, 1000);
    }

    private void stopLivePing() {
        if (proxyPingRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(proxyPingRunnable);
            proxyPingRunnable = null;
        }
    }

    private FrameLayout createArrowCard(Context context, int iconRes, String titleText, String descText, Runnable onClick) {
        FrameLayout card = new FrameLayout(context);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(0xFF1C1C1E);
        cardBg.setCornerRadius(AndroidUtilities.dp(24));
        cardBg.setStroke(Math.round(AndroidUtilities.dpf2(0.7f)), 0x22FFFFFF);
        card.setBackground(cardBg);

        FrameLayout iconBox = new FrameLayout(context);
        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setColor(0xFF2C2C2E);
        iconBg.setCornerRadius(AndroidUtilities.dp(16));
        iconBg.setStroke(Math.round(AndroidUtilities.dpf2(0.7f)), 0x22FFFFFF);
        iconBox.setBackground(iconBg);
        ImageView icon = new ImageView(context);
        icon.setImageResource(iconRes);
        icon.setColorFilter(0xFFFFFFFF);
        iconBox.addView(icon, LayoutHelper.createFrame(26, 26, Gravity.CENTER));
        card.addView(iconBox, LayoutHelper.createFrame(48, 48, Gravity.LEFT | Gravity.CENTER_VERTICAL, 16, 0, 0, 0));

        LinearLayout textCol = new LinearLayout(context);
        textCol.setOrientation(LinearLayout.VERTICAL);
        TextView t = new TextView(context);
        t.setText(titleText);
        t.setTextColor(0xFFFFFFFF);
        t.setTextSize(16);
        t.setTypeface(AndroidUtilities.bold());
        TextView d = new TextView(context);
        d.setText(descText);
        d.setTextColor(0xFF9E9E9E);
        d.setTextSize(13);
        textCol.addView(t);
        textCol.addView(d);
        card.addView(textCol, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,
                Gravity.LEFT | Gravity.CENTER_VERTICAL, 76, 10, 56, 10));

        ImageView arrow = new ImageView(context);
        arrow.setImageResource(R.drawable.msg_arrowright);
        arrow.setColorFilter(0xFF9E9E9E);
        card.addView(arrow, LayoutHelper.createFrame(18, 18, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 16, 0));

        card.setOnClickListener(v -> {
            if (onClick != null) onClick.run();
        });
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

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        stopLivePing();
        stopProxyDots();
    }
}
