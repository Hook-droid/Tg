package org.telegram.messenger;

import android.util.Base64;

import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.TLRPC;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class LyrxDeletedStorage {

    private static File baseDir() {
        File dir = new File(ApplicationLoader.getFilesDirFixed(), "lyrx_deleted");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private static File fileForDialog(int account, long dialogId) {
        return new File(baseDir(), account + "_" + dialogId + ".txt");
    }

    public static synchronized void saveDeleted(int account, long dialogId, ArrayList<TLRPC.Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messages.size(); i++) {
            TLRPC.Message m = messages.get(i);
            if (m == null) {
                continue;
            }
            try {
                NativeByteBuffer buffer = new NativeByteBuffer(m.getObjectSize());
                m.serializeToStream(buffer);
                int len = buffer.position();
                byte[] bytes = new byte[len];
                buffer.buffer.position(0);
                buffer.buffer.get(bytes, 0, len);
                buffer.reuse();
                sb.append(Base64.encodeToString(bytes, Base64.NO_WRAP));
                sb.append("\n");
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        if (sb.length() == 0) {
            return;
        }
        try {
            File f = fileForDialog(account, dialogId);
            java.io.FileWriter fw = new java.io.FileWriter(f, true);
            fw.write(sb.toString());
            fw.close();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public static synchronized ArrayList<TLRPC.Message> loadDeleted(int account, long dialogId) {
        ArrayList<TLRPC.Message> result = new ArrayList<>();
        File f = fileForDialog(account, dialogId);
        if (!f.exists()) {
            return result;
        }
        try {
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(f));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                try {
                    byte[] bytes = Base64.decode(line, Base64.NO_WRAP);
                    NativeByteBuffer buffer = new NativeByteBuffer(bytes.length);
                    buffer.writeBytes(bytes);
                    buffer.position(0);
                    TLRPC.Message m = TLRPC.Message.TLdeserialize(buffer, buffer.readInt32(false), false);
                    buffer.reuse();
                    if (m != null) {
                        result.add(m);
                    }
                } catch (Exception ignore) {
                }
            }
            br.close();
        } catch (Exception e) {
            FileLog.e(e);
        }
        return result;
    }

    public static synchronized long totalSize() {
        long total = 0;
        try {
            File dir = baseDir();
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    total += f.length();
                }
            }
        } catch (Exception ignore) {
        }
        return total;
    }

    public static synchronized void clearAll() {
        try {
            File dir = baseDir();
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
        } catch (Exception ignore) {
        }
    }

    public static String formatSize(long bytes) {
        if (bytes <= 0) {
            return "0 B";
        }
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        if (digitGroups >= units.length) {
            digitGroups = units.length - 1;
        }
        return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
