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

                StringBuilder txt = new StringBuilder();
                SimpleDateFormat dateFmt = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                long selfId = UserConfig.getInstance(currentAccount).getClientUserId();

                ArrayList<MessageObject> ordered = new ArrayList<>(messagesSnapshot);
                try {
                    ordered.sort((a, b) -> Integer.compare(a.messageOwner.date, b.messageOwner.date));
                } catch (Exception ignore) {}

                ArrayList<File> mediaFiles = new ArrayList<>();
                int total = ordered.size();
                String lastSender = null;

                for (int i = 0; i < total; i++) {
                    MessageObject mo = ordered.get(i);
                    if (mo == null || mo.messageOwner == null) continue;

                    long fromId = mo.getFromChatId();
                    boolean isSelf = fromId == selfId || mo.isOutOwner();
                    String senderName;
                    if (isSelf) {
                        TLRPC.User me = MessagesController.getInstance(currentAccount).getUser(selfId);
                        senderName = me != null ? UserObject.getUserName(me) : "Me";
                    } else {
                        senderName = safeName;
                    }

                    String date = dateFmt.format(new Date(mo.messageOwner.date * 1000L));
                    String text = mo.messageText != null ? mo.messageText.toString() : "";

                    if (!senderName.equals(lastSender)) {
                        if (lastSender != null) txt.append("\n");
                        txt.append("{\"").append(date).append("\"\n");
                        txt.append("\"").append(senderName).append("\"\n");
                        if (text.length() > 0) txt.append("\"").append(text).append("\"\n");
                        lastSender = senderName;
                    } else {
                        if (text.length() > 0) txt.append("\"").append(text).append("\"\n");
                    }

                    try {
                        if (mo.getDocument() != null || mo.getPhoto() != null) {
                            File path = FileLoader.getInstance(currentAccount).getPathToMessage(mo.messageOwner);
                            if (path != null && path.exists() && path.length() > 0 && path.length() <= MAX_FILE_SIZE) {
                                mediaFiles.add(path);
                            }
                        }
                    } catch (Exception ignore) {}

                    final int prog = (int) (((i + 1) / (float) Math.max(1, total)) * 70);
                    AndroidUtilities.runOnUIThread(() -> progressView.setProgress(prog / 100f, true));
                }

                File msgFile = new File(tmpDir, "Messages.txt");
                FileOutputStream fos = new FileOutputStream(msgFile);
                fos.write(txt.toString().getBytes("UTF-8"));
                fos.close();

                File zipFile = new File(lyrxDir, safeName + "-" + dateFmt.format(new Date()) + ".zip");
                if (zipFile.exists()) zipFile.delete();

                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
                zos.setLevel(Deflater.BEST_COMPRESSION);

                addFileToZip(zos, msgFile, "Messages.txt");

                int mediaCount = mediaFiles.size();
                for (int i = 0; i < mediaCount; i++) {
                    File mf = mediaFiles.get(i);
                    try {
                        addFileToZip(zos, mf, "media/" + mf.getName());
                    } catch (Exception ignore) {}
                    final int prog = 70 + (int) (((i + 1) / (float) Math.max(1, mediaCount)) * 30);
                    AndroidUtilities.runOnUIThread(() -> progressView.setProgress(prog / 100f, true));
                }

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
