package org.telegram.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LineProgressView;
import org.telegram.ui.Components.LayoutHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.Deflater;

public class LyrxChatBackup {

    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;

    public static void showBackupDialog(BaseFragment fragment, long dialogId, String peerName) {
        if (fragment == null || fragment.getParentActivity() == null) return;
        Context context = fragment.getParentActivity();

        if (!hasStoragePermission(context)) {
            requestStoragePermission(fragment);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Chat Backup");
        builder.setMessage("Do you want to fully back up this chat? If you back it up, everything in the chat will be saved to the LyrxGram folder.");
        builder.setPositiveButton("Backup", (dialog, which) -> startBackup(fragment, dialogId, peerName));
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        AlertDialog dialog = builder.create();
        dialog.show();

        View posView = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (posView instanceof TextView) {
            TextView positive = (TextView) posView;
            positive.setEnabled(false);
            positive.setAlpha(0.5f);
            final int[] seconds = {10};
            positive.setText("Backup " + seconds[0]);
            final Runnable[] tick = new Runnable[1];
            tick[0] = () -> {
                seconds[0]--;
                if (seconds[0] <= 0) {
                    positive.setEnabled(true);
                    positive.setAlpha(1f);
                    positive.setTextColor(0xFFFFB020);
                    positive.setText("Backup");
                } else {
                    positive.setText("Backup " + seconds[0]);
                    AndroidUtilities.runOnUIThread(tick[0], 1000);
                }
            };
            AndroidUtilities.runOnUIThread(tick[0], 1000);
        }
    }

    private static void startBackup(BaseFragment fragment, long dialogId, String peerName) {
        if (fragment == null || fragment.getParentActivity() == null) return;
        Context context = fragment.getParentActivity();
        final int currentAccount = fragment.getCurrentAccount();

        final ArrayList<MessageObject> messagesSnapshot;
        if (fragment instanceof ChatActivity) {
            messagesSnapshot = new ArrayList<>(((ChatActivity) fragment).messages);
        } else {
            messagesSnapshot = new ArrayList<>();
        }

        FrameLayout container = new FrameLayout(context);
        TextView titleView = new TextView(context);
        titleView.setText("Chat Is Being Backed Up, Please Wait.");
        titleView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        titleView.setTextSize(16);
        container.addView(titleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 24, 24, 24, 0));

        LineProgressView progressView = new LineProgressView(context);
        progressView.setProgressColor(0xFF3390EC);
        progressView.setBackColor(0x33FFFFFF);
        container.addView(progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 4, 0, 24, 64, 24, 24));

        AlertDialog.Builder progressBuilder = new AlertDialog.Builder(context);
        progressBuilder.setView(container);
        AlertDialog progressDialog = progressBuilder.create();
        progressDialog.setCanCancel(false);
        progressDialog.show();

        final String safeName = (peerName == null || peerName.trim().length() == 0) ? "Chat" : peerName.trim();

        Thread worker = new Thread(() -> {
            boolean success = false;
            try {
                File lyrxDir = new File(Environment.getExternalStorageDirectory(), "LyrxGram");
                if (!lyrxDir.exists()) lyrxDir.mkdirs();

                File tmpDir = new File(context.getCacheDir(), "lyrx_backup_tmp");
                if (tmpDir.exists()) deleteRecursive(tmpDir);
                tmpDir.mkdirs();

                SimpleDateFormat dateFmt = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.US);
                long selfId = UserConfig.getInstance(currentAccount).getClientUserId();

                ArrayList<MessageObject> ordered = new ArrayList<>(messagesSnapshot);
                try {
                    ordered.sort((a, b) -> Integer.compare(a.messageOwner.date, b.messageOwner.date));
                } catch (Exception ignore) {}

                StringBuilder html = new StringBuilder();
                html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">");
                html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
                html.append("<title>").append(escape(safeName)).append("</title><style>");
                html.append("*{margin:0;padding:0;box-sizing:border-box;font-family:-apple-system,Segoe UI,Roboto,sans-serif}");
                html.append("body{background:#e5ddd5;padding-bottom:40px}");
                html.append(".header{position:sticky;top:0;background:#075e54;color:#fff;padding:14px 18px;box-shadow:0 2px 6px rgba(0,0,0,.2);z-index:10}");
                html.append(".header h1{font-size:17px;font-weight:600}");
                html.append(".header .sub{font-size:12px;opacity:.8;margin-top:2px}");
                html.append(".chat{max-width:760px;margin:0 auto;padding:14px}");
                html.append(".date-sep{text-align:center;margin:14px 0}");
                html.append(".date-sep span{background:#d7f8c8;color:#4a4a4a;font-size:12px;padding:5px 12px;border-radius:8px;box-shadow:0 1px 1px rgba(0,0,0,.1)}");
                html.append(".msg{max-width:75%;margin:4px 0;padding:7px 10px;border-radius:9px;position:relative;box-shadow:0 1px 1px rgba(0,0,0,.13);word-wrap:break-word;clear:both}");
                html.append(".in{background:#fff;float:left;border-top-left-radius:2px}");
                html.append(".out{background:#dcf8c6;float:right;border-top-right-radius:2px}");
                html.append(".txt{font-size:14.5px;color:#303030;white-space:pre-wrap;line-height:1.35}");
                html.append(".time{font-size:11px;color:#8a8a8a;float:right;margin:4px 0 -2px 10px}");
                html.append(".msg img{max-width:100%;border-radius:6px;margin-top:4px;cursor:pointer;display:block}");
                html.append(".file{display:flex;align-items:center;gap:8px;margin-top:4px;padding:8px;background:rgba(0,0,0,.05);border-radius:6px;text-decoration:none;color:#075e54;font-size:13px}");
                html.append(".file .ic{width:30px;height:30px;border-radius:50%;background:#075e54;color:#fff;display:flex;align-items:center;justify-content:center;font-size:15px;flex-shrink:0}");
                html.append("</style></head><body>");
                html.append("<div class=\"header\"><h1>").append(escape(safeName)).append("</h1>");
                html.append("<div class=\"sub\">Backed up ").append(dateFmt.format(new Date())).append(" &middot; LyrxGram</div></div>");
                html.append("<div class=\"chat\">");

                int total = ordered.size();
                String lastDate = null;

                for (int i = 0; i < total; i++) {
                    MessageObject mo = ordered.get(i);
                    if (mo == null || mo.messageOwner == null) continue;
                    if (mo.messageOwner.action != null) continue;

                    long fromId = mo.getFromChatId();
                    boolean isSelf = fromId == selfId || mo.isOutOwner();

                    String date = dateFmt.format(new Date(mo.messageOwner.date * 1000L));
                    String time = timeFmt.format(new Date(mo.messageOwner.date * 1000L));
                    String text = mo.messageText != null ? mo.messageText.toString() : "";

                    String mediaHtml = "";
                    try {
                        if (mo.getDocument() != null || mo.getPhoto() != null) {
                            File path = FileLoader.getInstance(currentAccount).getPathToMessage(mo.messageOwner);
                            if (path != null && path.exists() && path.length() > 0 && path.length() <= MAX_FILE_SIZE) {
                                String fileName = getBackupFileName(mo, path);
                                boolean isImage = mo.getPhoto() != null || isImageName(fileName);
                                String b64 = fileToBase64(path);
                                if (b64 != null) {
                                    if (isImage) {
                                        mediaHtml = "<img src=\"data:image/*;base64," + b64 + "\" onclick=\"window.open(this.src)\">";
                                    } else {
                                        String mime = mo.getDocument() != null && mo.getDocument().mime_type != null ? mo.getDocument().mime_type : "application/octet-stream";
                                        mediaHtml = "<a class=\"file\" href=\"data:" + mime + ";base64," + b64 + "\" download=\"" + escape(fileName) + "\"><span class=\"ic\">&#128196;</span>" + escape(fileName) + "</a>";
                                    }
                                }
                            }
                        }
                    } catch (Exception ignore) {}

                    if (text.length() == 0 && mediaHtml.length() == 0) continue;

                    if (!date.equals(lastDate)) {
                        html.append("<div class=\"date-sep\"><span>").append(date).append("</span></div>");
                        lastDate = date;
                    }

                    html.append("<div class=\"msg ").append(isSelf ? "out" : "in").append("\">");
                    if (text.length() > 0) {
                        html.append("<div class=\"txt\">").append(escape(text)).append("</div>");
                    }
                    if (mediaHtml.length() > 0) {
                        html.append(mediaHtml);
                    }
                    html.append("<span class=\"time\">").append(time).append("</span>");
                    html.append("<div style=\"clear:both\"></div></div>");

                    final int prog = (int) (((i + 1) / (float) Math.max(1, total)) * 95);
                    AndroidUtilities.runOnUIThread(() -> progressView.setProgress(prog / 100f, true));
                }

                html.append("</div></body></html>");

                File htmlFile = new File(tmpDir, "chat.html");
                FileOutputStream fos = new FileOutputStream(htmlFile);
                fos.write(html.toString().getBytes("UTF-8"));
                fos.close();

                File zipFile = new File(lyrxDir, safeName + "-" + dateFmt.format(new Date()) + ".zip");
                if (zipFile.exists()) zipFile.delete();

                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
                zos.setLevel(Deflater.BEST_COMPRESSION);
                addFileToZip(zos, htmlFile, "chat.html");
                AndroidUtilities.runOnUIThread(() -> progressView.setProgress(1f, true));

                zos.close();
                deleteRecursive(tmpDir);
                success = true;
            } catch (Exception e) {
                success = false;
            }

            final boolean ok = success;
            AndroidUtilities.runOnUIThread(() -> {
                try {
                    progressDialog.dismiss();
                } catch (Exception ignore) {}
                if (fragment.getParentActivity() == null) return;
                if (ok) {
                    BulletinFactory.of(fragment).createSimpleBulletin(R.raw.ic_download, "Chat Has Been Backed Up").show();
                } else {
                    BulletinFactory.of(fragment).createSimpleBulletin(R.raw.error, "The Chat Could Not Be Backed Up Due To An Error. Please Try Again.").show();
                }
            });
        });
        worker.start();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static boolean isImageName(String name) {
        if (name == null) return false;
        String n = name.toLowerCase();
        return n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png") || n.endsWith(".webp") || n.endsWith(".gif") || n.endsWith(".bmp");
    }

    private static String fileToBase64(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            fis.close();
            return android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }
    }

    private static String getBackupFileName(MessageObject mo, File path) {
        try {
            TLRPC.Document doc = mo.getDocument();
            if (doc != null) {
                String name = FileLoader.getDocumentFileName(doc);
                if (name != null && name.trim().length() > 0) {
                    return sanitize(name);
                }
                String ext = "";
                if (doc.mime_type != null) {
                    if (doc.mime_type.equals("video/mp4")) ext = ".mp4";
                    else if (doc.mime_type.equals("audio/ogg")) ext = ".ogg";
                    else if (doc.mime_type.equals("audio/mpeg")) ext = ".mp3";
                }
                return "file_" + mo.getId() + ext;
            }
            if (mo.getPhoto() != null) {
                return "photo_" + mo.getId() + ".jpg";
            }
        } catch (Exception ignore) {}
        String fallback = path.getName();
        if (fallback.indexOf('.') < 0) fallback = fallback + ".dat";
        return fallback;
    }

    private static String sanitize(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private static boolean hasStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private static void requestStoragePermission(BaseFragment fragment) {
        Activity activity = fragment.getParentActivity();
        if (activity == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                AlertDialog.Builder b = new AlertDialog.Builder(activity);
                b.setTitle("Chat Backup");
                b.setMessage("Storage permission is needed to save the backup to the LyrxGram folder. Please enable \"Allow access to manage all files\".");
                b.setPositiveButton("Open Settings", (d, w) -> {
                    try {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        intent.setData(Uri.parse("package:" + activity.getPackageName()));
                        activity.startActivity(intent);
                    } catch (Exception e) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            activity.startActivity(intent);
                        } catch (Exception ignore) {}
                    }
                });
                b.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
                b.show();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 9911);
            }
        } catch (Exception ignore) {}
    }

    private static void addFileToZip(ZipOutputStream zos, File file, String entryName) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        byte[] buffer = new byte[8192];
        int len;
        while ((len = fis.read(buffer)) > 0) {
            zos.write(buffer, 0, len);
        }
        zos.closeEntry();
        fis.close();
    }

    private static void deleteRecursive(File file) {
        if (file == null || !file.exists()) return;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File c : children) deleteRecursive(c);
            }
        }
        file.delete();
    }
}
