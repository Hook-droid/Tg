package org.telegram.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;

public class LyrxChatModesActivity extends BaseFragment {

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

        TextView proxyHeader = new TextView(context);
        proxyHeader.setText("Free Unlimited Proxy");
        proxyHeader.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        proxyHeader.setTextSize(15);
        proxyHeader.setTypeface(AndroidUtilities.bold());
        proxyHeader.setPadding(AndroidUtilities.dp(16), 0, AndroidUtilities.dp(16), AndroidUtilities.dp(8));
        root.addView(proxyHeader, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        LinearLayout.LayoutParams proxyParams = LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
        proxyParams.bottomMargin = AndroidUtilities.dp(16);
        root.addView(createProxyCard(context), proxyParams);

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

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        stopLivePing();
        stopProxyDots();
    }

    private FrameLayout createProxyCard(Context context) {        FrameLayout card = new FrameLayout(context);
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
        d.setText("Wait 5-10 Seconds To Connect");
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
