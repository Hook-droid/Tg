package org.telegram.messenger;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

import java.io.File;

public class LyrxUpdater {

    private static final String CHANNEL = "selamkerizadam";
    private static final String PREF_KEY = "lyrxSeenApkVersion";

    private static int baseline() {
        return MessagesController.getGlobalMainSettings().getInt(PREF_KEY, 0);
    }

    private static boolean firstRun() {
        return !MessagesController.getGlobalMainSettings().contains(PREF_KEY);
    }

    public static void markSeen(int version) {
        MessagesController.getGlobalMainSettings().edit().putInt(PREF_KEY, version).apply();
    }

    private static boolean checkedThisSession;

    public static int latestVersion;
    public static TLRPC.Document latestDocument;
    public static String latestFileName;

    public static final String[] CHANGES = {
            "Enjoy The New LyrxGram Features",
            "Bugs And Crashes Fixed",
            "New Tools Added To The Mod Menu",
            "Faster And More Stable Than Before",
            "Update Now To Get Everything"
    };

    public static String getFileName(TLRPC.Document document) {
        if (document == null || document.attributes == null) {
            return "";
        }
        for (int i = 0; i < document.attributes.size(); i++) {
            TLRPC.DocumentAttribute attribute = document.attributes.get(i);
            if (attribute instanceof TLRPC.TL_documentAttributeFilename) {
                return attribute.file_name == null ? "" : attribute.file_name;
            }
        }
        return "";
    }

    private static int parseVersion(String fileName) {
        try {
            String digits = fileName.replaceAll("\\.apk$", "").replaceAll("[^0-9]", "");
            if (digits.length() == 0) {
                return 0;
            }
            if (digits.length() > 6) {
                digits = digits.substring(0, 6);
            }
            return Integer.parseInt(digits);
        } catch (Exception ignore) {
            return 0;
        }
    }

    public static void check(Runnable onUpdateAvailable) {
        if (checkedThisSession) {
            return;
        }
        checkedThisSession = true;

        final int account = UserConfig.selectedAccount;
        if (!UserConfig.getInstance(account).isClientActivated()) {
            return;
        }

        TLRPC.TL_contacts_resolveUsername resolve = new TLRPC.TL_contacts_resolveUsername();
        resolve.username = CHANNEL;
        ConnectionsManager.getInstance(account).sendRequest(resolve, (response, error) -> {
            if (error != null || !(response instanceof TLRPC.TL_contacts_resolvedPeer)) {
                return;
            }
            TLRPC.TL_contacts_resolvedPeer resolved = (TLRPC.TL_contacts_resolvedPeer) response;
            if (resolved.chats == null || resolved.chats.isEmpty()) {
                return;
            }
            MessagesController.getInstance(account).putChats(resolved.chats, false);
            MessagesController.getInstance(account).putUsers(resolved.users, false);

            TLRPC.Chat channel = resolved.chats.get(0);
            TLRPC.TL_inputPeerChannel peer = new TLRPC.TL_inputPeerChannel();
            peer.channel_id = channel.id;
            peer.access_hash = channel.access_hash;

            TLRPC.TL_messages_getHistory history = new TLRPC.TL_messages_getHistory();
            history.peer = peer;
            history.limit = 30;

            ConnectionsManager.getInstance(account).sendRequest(history, (historyResponse, historyError) -> {
                if (historyError != null || !(historyResponse instanceof TLRPC.messages_Messages)) {
                    return;
                }
                TLRPC.messages_Messages messages = (TLRPC.messages_Messages) historyResponse;
                int bestVersion = 0;
                TLRPC.Document bestDocument = null;
                String bestName = "";

                for (int i = 0; i < messages.messages.size(); i++) {
                    TLRPC.Message message = messages.messages.get(i);
                    if (message == null || message.media == null || message.media.document == null) {
                        continue;
                    }
                    TLRPC.Document document = message.media.document;
                    String name = getFileName(document);
                    if (!name.toLowerCase().endsWith(".apk")) {
                        continue;
                    }
                    int version = parseVersion(name);
                    if (version > bestVersion) {
                        bestVersion = version;
                        bestDocument = document;
                        bestName = name;
                    }
                }

                if (bestDocument == null) {
                    return;
                }
                if (firstRun()) {
                    markSeen(bestVersion);
                    return;
                }
                if (bestVersion > baseline()) {
                    latestVersion = bestVersion;
                    latestDocument = bestDocument;
                    latestFileName = bestName;
                    AndroidUtilities.runOnUIThread(onUpdateAvailable);
                }
            });
        });
    }

    public static void startDownload() {
        if (latestDocument == null) {
            return;
        }
        markSeen(latestVersion);
        int account = UserConfig.selectedAccount;
        FileLoader.getInstance(account).loadFile(latestDocument, "lyrxupdate", FileLoader.PRIORITY_HIGH, 0);
    }

    public static File getDownloadedFile() {
        if (latestDocument == null) {
            return null;
        }
        try {
            File file = FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(latestDocument, true);
            if (file != null && file.exists() && file.length() > 0) {
                return file;
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
        return null;
    }

    public static void install(Activity activity, File file) {
        try {
            File target = file;
            if (!file.getName().toLowerCase().endsWith(".apk")) {
                File copy = new File(activity.getExternalCacheDir(), "LyrxGram-update.apk");
                if (copy.exists()) {
                    copy.delete();
                }
                AndroidUtilities.copyFile(file, copy);
                target = copy;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = androidx.core.content.FileProvider.getUriForFile(
                        activity, ApplicationLoader.getApplicationId() + ".provider", target);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(target);
            }
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }
}
