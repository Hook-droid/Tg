package org.telegram.messenger;

import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

public class LyrxTextToVoice {

    public static final String[] NAMES = {
            "Turkish Male",
            "Turkish Female",
            "Russian Male",
            "Russian Female",
            "English Male",
            "English Female"
    };

    private static final String[] LANGUAGES = {"tr", "tr", "ru", "ru", "en", "en"};
    private static final boolean[] IS_MALE = {true, false, true, false, true, false};

    public interface Callback {
        void onReady(File oggFile, int durationSeconds);

        void onError(String reason);
    }

    private static TextToSpeech engine;
    private static boolean engineReady;

    public static void prepare() {
        if (engine != null) {
            return;
        }
        engine = new TextToSpeech(ApplicationLoader.applicationContext, status -> {
            engineReady = status == TextToSpeech.SUCCESS;
        });
    }

    private static Voice pickVoice(int index) {
        if (engine == null) {
            return null;
        }
        String language = LANGUAGES[index];
        boolean male = IS_MALE[index];
        try {
            Set<Voice> voices = engine.getVoices();
            if (voices == null) {
                return null;
            }
            Voice fallback = null;
            for (Voice voice : voices) {
                Locale locale = voice.getLocale();
                if (locale == null || !locale.getLanguage().equals(language)) {
                    continue;
                }
                if (voice.isNetworkConnectionRequired()) {
                    continue;
                }
                String name = voice.getName() == null ? "" : voice.getName().toLowerCase();
                boolean voiceIsMale = name.contains("male") && !name.contains("female");
                if (voiceIsMale == male) {
                    return voice;
                }
                if (fallback == null) {
                    fallback = voice;
                }
            }
            return fallback;
        } catch (Throwable e) {
            FileLog.e(e);
            return null;
        }
    }

    public static void synthesize(String text, int voiceIndex, Callback callback) {
        if (text == null || text.trim().length() == 0) {
            callback.onError("Type something first");
            return;
        }
        if (voiceIndex < 0 || voiceIndex >= NAMES.length) {
            voiceIndex = 0;
        }
        final int index = voiceIndex;
        final String content = text.trim();

        if (engine == null) {
            engine = new TextToSpeech(ApplicationLoader.applicationContext, status -> {
                engineReady = status == TextToSpeech.SUCCESS;
                if (engineReady) {
                    AndroidUtilities.runOnUIThread(() -> run(content, index, callback));
                } else {
                    AndroidUtilities.runOnUIThread(() -> callback.onError("No text to speech engine on this device"));
                }
            });
            return;
        }
        run(content, index, callback);
    }

    private static void run(String text, int voiceIndex, Callback callback) {
        try {
            Voice voice = pickVoice(voiceIndex);
            if (voice == null) {
                Locale locale = new Locale(LANGUAGES[voiceIndex]);
                int result = engine.setLanguage(locale);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    callback.onError("This language is not installed on your device");
                    return;
                }
            } else {
                engine.setVoice(voice);
            }

            File directory = FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE);
            final File wav = new File(directory, "lyrx_tts_" + System.currentTimeMillis() + ".wav");
            final File ogg = new File(directory, "lyrx_tts_" + System.currentTimeMillis() + ".ogg");
            final String utteranceId = "lyrxtts";

            engine.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String id) {
                }

                @Override
                public void onDone(String id) {
                    Utilities.globalQueue.postRunnable(() -> {
                        boolean converted = MediaController.getInstance()
                                .lyrxWavToOpus(wav.getAbsolutePath(), ogg.getAbsolutePath());
                        int duration = estimateDuration(wav);
                        wav.delete();
                        AndroidUtilities.runOnUIThread(() -> {
                            if (converted) {
                                callback.onReady(ogg, duration);
                            } else {
                                callback.onError("Could not build the voice message");
                            }
                        });
                    });
                }

                @Override
                public void onError(String id) {
                    AndroidUtilities.runOnUIThread(() -> callback.onError("Text to speech failed"));
                }
            });

            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
            int result = engine.synthesizeToFile(text, params, wav.getAbsolutePath());
            if (result != TextToSpeech.SUCCESS) {
                callback.onError("Text to speech failed");
            }
        } catch (Throwable e) {
            FileLog.e(e);
            callback.onError("Text to speech failed");
        }
    }

    private static int estimateDuration(File wav) {
        try {
            long dataSize = wav.length() - 44;
            if (dataSize <= 0) {
                return 1;
            }
            int seconds = (int) (dataSize / (22050 * 2));
            return Math.max(1, seconds);
        } catch (Throwable e) {
            return 1;
        }
    }
}
