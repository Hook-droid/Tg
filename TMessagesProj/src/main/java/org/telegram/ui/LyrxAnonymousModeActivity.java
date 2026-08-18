package org.telegram.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
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
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;

public class LyrxAnonymousModeActivity extends BaseFragment {

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

        TextView proxyHeader = new TextView(context);
        proxyHeader.setText("Free Smart Proxy");
        proxyHeader.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        proxyHeader.setTextSize(15);
        proxyHeader.setTypeface(AndroidUtilities.bold());
        proxyHeader.setPadding(AndroidUtilities.dp(16), 0, AndroidUtilities.dp(16), AndroidUtilities.dp(8));
        root.addView(proxyHeader, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        LinearLayout.LayoutParams proxyParams = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
        proxyParams.bottomMargin = AndroidUtilities.dp(16);
        root.addView(createProxyCard(context), proxyParams);

        TextView ghostHeader = new TextView(context);
        ghostHeader.setText("Ghost Mode");
        ghostHeader.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        ghostHeader.setTextSize(15);
        ghostHeader.setTypeface(AndroidUtilities.bold());
        ghostHeader.setPadding(AndroidUtilities.dp(16), 0, AndroidUtilities.dp(16), AndroidUtilities.dp(8));
        root.addView(ghostHeader, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

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
            if (checked) {
                MessagesController.getInstance(currentAccount).lyrxSendOfflineNow();
            }
        }));
        group.addView(divider(context));
        group.addView(makeCheck(context, "Unread Messages", "Don't send read receipts", SharedConfig.lyrxDontSendRead, checked -> {
            SharedConfig.lyrxDontSendRead = checked;
            save("lyrxDontSendRead", checked);
        }));
        group.addView(divider(context));
        group.addView(makeCheck(context, "Hide Last Seen", "Nobody can see your last seen", SharedConfig.lyrxHideLastSeen, checked -> {
            SharedConfig.lyrxHideLastSeen = checked;
            save("lyrxHideLastSeen", checked);
            applyLastSeenPrivacy(checked);
        }));

        root.addView(group, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        TextView muteHeader = new TextView(context);
        muteHeader.setText("Automatically Mute The User");
        muteHeader.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        muteHeader.setTextSize(15);
        muteHeader.setTypeface(AndroidUtilities.bold());
        muteHeader.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(8));
        root.addView(muteHeader, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

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
        mp.topMargin = 0;
        root.addView(muteGroup, mp);

        TextView storyHeader = new TextView(context);
        storyHeader.setText("Secretly Watch the Stories");
        storyHeader.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        storyHeader.setTextSize(15);
        storyHeader.setTypeface(AndroidUtilities.bold());
        storyHeader.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(8));
        root.addView(storyHeader, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        LinearLayout storyGroup = new LinearLayout(context);
        storyGroup.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable storyBg = new GradientDrawable();
        storyBg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        storyBg.setCornerRadius(AndroidUtilities.dp(16));
        storyGroup.setBackground(storyBg);

        TextCheckCell storyCell = new TextCheckCell(context);
        storyCell.setTextAndValueAndCheck("Ghost Story View", "Watch Stories Without Sending \"Seen\" Notification", SharedConfig.lyrxGhostStories, true, false);
        storyCell.setPlainIcon(R.drawable.msg_stories_myhide);
        storyCell.setOnClickListener(v -> {
            boolean ns = !storyCell.isChecked();
            storyCell.setChecked(ns);
            SharedConfig.lyrxGhostStories = ns;
            save("lyrxGhostStories", ns);
        });
        storyGroup.addView(storyCell);
        root.addView(storyGroup, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        TextView mediaHeader = new TextView(context);
        mediaHeader.setText("Media Freedom");
        mediaHeader.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        mediaHeader.setTextSize(15);
        mediaHeader.setTypeface(AndroidUtilities.bold());
        mediaHeader.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(8));
        root.addView(mediaHeader, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        LinearLayout mediaGroup = new LinearLayout(context);
        mediaGroup.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable mediaBg = new GradientDrawable();
        mediaBg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        mediaBg.setCornerRadius(AndroidUtilities.dp(16));
        mediaGroup.setBackground(mediaBg);

        mediaGroup.addView(makeCheck(context, "Save Restricted Content", "Download and forward from protected chats", SharedConfig.lyrxSaveRestricted, checked -> {
            SharedConfig.lyrxSaveRestricted = checked;
            save("lyrxSaveRestricted", checked);
        }));
        mediaGroup.addView(divider(context));
        mediaGroup.addView(makeCheck(context, "Disable One-Time Media", "Show self-destructing photos, voices and rounds as normal", SharedConfig.lyrxRevealOnce, checked -> {
            SharedConfig.lyrxRevealOnce = checked;
            save("lyrxRevealOnce", checked);
        }));

        root.addView(mediaGroup, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        scroll.addView(root, new android.widget.FrameLayout.LayoutParams(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        fragmentView = scroll;
        return fragmentView;
    }

    private void applyLastSeenPrivacy(boolean hide) {
        try {
            org.telegram.tgnet.tl.TL_account.setPrivacy req = new org.telegram.tgnet.tl.TL_account.setPrivacy();
            req.key = new org.telegram.tgnet.TLRPC.TL_inputPrivacyKeyStatusTimestamp();
            if (hide) {
                req.rules.add(new org.telegram.tgnet.TLRPC.TL_inputPrivacyValueDisallowAll());
            } else {
                req.rules.add(new org.telegram.tgnet.TLRPC.TL_inputPrivacyValueAllowAll());
            }
            org.telegram.tgnet.ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {});
        } catch (Exception ignore) {}
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

    private FrameLayout createProxyCard(Context context) {
        FrameLayout card = new FrameLayout(context);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        cardBg.setCornerRadius(AndroidUtilities.dp(16));
        card.setBackground(cardBg);

        FrameLayout iconBox = new FrameLayout(context);
        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setColor(Theme.getColor(Theme.key_windowBackgroundGray));
        iconBg.setCornerRadius(AndroidUtilities.dp(12));
        iconBox.setBackground(iconBg);
        ImageView icon = new ImageView(context);
        icon.setImageResource(R.drawable.menu_privacy_policy);
        icon.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
        iconBox.addView(icon, LayoutHelper.createFrame(24, 24, Gravity.CENTER));
        card.addView(iconBox, LayoutHelper.createFrame(42, 42, Gravity.LEFT | Gravity.CENTER_VERTICAL, 14, 0, 0, 0));

        LinearLayout textCol = new LinearLayout(context);
        textCol.setOrientation(LinearLayout.VERTICAL);
        TextView d = new TextView(context);
        d.setText("Free Proxy");
        d.setTypeface(AndroidUtilities.bold());
        d.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        d.setTextSize(13);
        proxyStatusView = new TextView(context);
        proxyStatusView.setText("Inactive");
        proxyStatusView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        proxyStatusView.setTextSize(13);
        textCol.addView(d);
        textCol.addView(proxyStatusView);
        card.addView(textCol, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,
                Gravity.LEFT | Gravity.CENTER_VERTICAL, 68, 8, 62, 8));

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

        card.setMinimumHeight(AndroidUtilities.dp(72));
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
        proxyStatusView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
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

    private void save(String key, boolean value) {
        MessagesController.getGlobalMainSettings().edit().putBoolean(key, value).apply();
    }
}
