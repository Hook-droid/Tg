package org.telegram.messenger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LyrxWhisper {

    public static final String MODEL_NAME = "ggml-tiny-q5_1.bin";
    public static final String MODEL_URL = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny-q5_1.bin";

    private static boolean libraryLoaded;
    private static boolean libraryFailed;
    private static String loadError;

    private static long contextPtr;

    private static native String nativeSystemInfo();

    private static native long nativeInit(String modelPath);

    private static native void nativeFree(long ptr);

    private static native String nativeTranscribe(long ptr, float[] samples, int threads, String language);

    public static synchronized boolean loadLibrary() {
        if (libraryLoaded) {
            return true;
        }
        if (libraryFailed) {
            return false;
        }
        try {
            System.loadLibrary("whisper_lyrx");
            libraryLoaded = true;
        } catch (Throwable e) {
            libraryFailed = true;
            loadError = e.getMessage();
            FileLog.e(e);
        }
        return libraryLoaded;
    }

    public static String getLoadError() {
        return loadError;
    }

    public static String systemInfo() {
        if (!loadLibrary()) {
            return null;
        }
        try {
            return nativeSystemInfo();
        } catch (Throwable e) {
            FileLog.e(e);
            return null;
        }
    }

    public static File getModelFile() {
        return new File(ApplicationLoader.getFilesDirFixed(), MODEL_NAME);
    }

    public static boolean isModelReady() {
        File f = getModelFile();
        return f.exists() && f.length() > 1024 * 1024;
    }

    public static synchronized boolean ensureContext() {
        if (contextPtr != 0) {
            return true;
        }
        if (!loadLibrary() || !isModelReady()) {
            return false;
        }
        try {
            contextPtr = nativeInit(getModelFile().getAbsolutePath());
        } catch (Throwable e) {
            FileLog.e(e);
            contextPtr = 0;
        }
        return contextPtr != 0;
    }

    public static synchronized void release() {
        if (contextPtr != 0) {
            try {
                nativeFree(contextPtr);
            } catch (Throwable e) {
                FileLog.e(e);
            }
            contextPtr = 0;
        }
    }

    public interface DownloadCallback {
        void onProgress(int percent);
        void onDone(boolean success);
    }

    private static volatile boolean downloading;

    public static boolean isDownloading() {
        return downloading;
    }

    public static void downloadModel(DownloadCallback callback) {
        if (downloading) {
            return;
        }
        downloading = true;
        new Thread(() -> {
            boolean ok = false;
            File target = getModelFile();
            File temp = new File(target.getAbsolutePath() + ".part");
            HttpURLConnection conn = null;
            InputStream in = null;
            FileOutputStream fos = null;
            try {
                if (temp.exists()) {
                    temp.delete();
                }
                URL url = new URL(MODEL_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                conn.setInstanceFollowRedirects(true);
                conn.connect();
                int total = conn.getContentLength();
                in = conn.getInputStream();
                fos = new FileOutputStream(temp);
                byte[] buffer = new byte[65536];
                long written = 0;
                int lastPercent = -1;
                int read;
                while ((read = in.read(buffer)) > 0) {
                    fos.write(buffer, 0, read);
                    written += read;
                    if (total > 0 && callback != null) {
                        int percent = (int) (written * 100 / total);
                        if (percent != lastPercent) {
                            lastPercent = percent;
                            final int p = percent;
                            AndroidUtilities.runOnUIThread(() -> callback.onProgress(p));
                        }
                    }
                }
                fos.flush();
                fos.close();
                fos = null;
                if (written > 1024 * 1024) {
                    if (target.exists()) {
                        target.delete();
                    }
                    ok = temp.renameTo(target);
                }
            } catch (Throwable e) {
                FileLog.e(e);
            } finally {
                try { if (fos != null) fos.close(); } catch (Throwable ignore) {}
                try { if (in != null) in.close(); } catch (Throwable ignore) {}
                try { if (conn != null) conn.disconnect(); } catch (Throwable ignore) {}
                if (temp.exists()) {
                    temp.delete();
                }
            }
            downloading = false;
            final boolean result = ok;
            if (callback != null) {
                AndroidUtilities.runOnUIThread(() -> callback.onDone(result));
            }
        }).start();
    }

    public static boolean isAvailable() {
        return SharedConfig.lyrxWhisperEnabled && isModelReady() && loadLibrary();
    }

    public static synchronized String transcribe(float[] samples, String language) {
        if (!ensureContext() || samples == null || samples.length == 0) {
            return null;
        }
        int threads = Math.max(2, Math.min(4, Runtime.getRuntime().availableProcessors() - 2));
        try {
            return nativeTranscribe(contextPtr, samples, threads, language);
        } catch (Throwable e) {
            FileLog.e(e);
            return null;
        }
    }
}
