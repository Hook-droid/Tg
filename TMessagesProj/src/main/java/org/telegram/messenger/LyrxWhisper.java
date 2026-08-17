package org.telegram.messenger;

import java.io.File;

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
