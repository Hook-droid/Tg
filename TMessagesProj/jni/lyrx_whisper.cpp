#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include "whisper.h"

#define LOG_TAG "LyrxWhisper"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jstring JNICALL
Java_org_telegram_messenger_LyrxWhisper_nativeSystemInfo(JNIEnv *env, jclass clazz) {
    const char *info = whisper_print_system_info();
    return env->NewStringUTF(info == nullptr ? "unknown" : info);
}

JNIEXPORT jlong JNICALL
Java_org_telegram_messenger_LyrxWhisper_nativeInit(JNIEnv *env, jclass clazz, jstring modelPath) {
    const char *path = env->GetStringUTFChars(modelPath, nullptr);
    if (path == nullptr) {
        return 0;
    }
    struct whisper_context_params cparams = whisper_context_default_params();
    struct whisper_context *ctx = whisper_init_from_file_with_params(path, cparams);
    env->ReleaseStringUTFChars(modelPath, path);
    if (ctx == nullptr) {
        LOGE("model load failed");
        return 0;
    }
    LOGI("model loaded");
    return reinterpret_cast<jlong>(ctx);
}

JNIEXPORT void JNICALL
Java_org_telegram_messenger_LyrxWhisper_nativeFree(JNIEnv *env, jclass clazz, jlong ptr) {
    if (ptr == 0) {
        return;
    }
    whisper_free(reinterpret_cast<struct whisper_context *>(ptr));
}

JNIEXPORT jstring JNICALL
Java_org_telegram_messenger_LyrxWhisper_nativeTranscribe(JNIEnv *env, jclass clazz, jlong ptr,
                                                         jfloatArray samples, jint threads,
                                                         jstring language) {
    if (ptr == 0 || samples == nullptr) {
        return env->NewStringUTF("");
    }
    struct whisper_context *ctx = reinterpret_cast<struct whisper_context *>(ptr);

    jsize count = env->GetArrayLength(samples);
    if (count <= 0) {
        return env->NewStringUTF("");
    }
    std::vector<float> pcm(static_cast<size_t>(count));
    env->GetFloatArrayRegion(samples, 0, count, pcm.data());

    struct whisper_full_params params = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    params.print_realtime = false;
    params.print_progress = false;
    params.print_timestamps = false;
    params.print_special = false;
    params.translate = false;
    params.single_segment = false;
    params.no_context = true;
    params.suppress_blank = true;
    params.n_threads = threads < 1 ? 1 : threads;

    std::string lang;
    if (language != nullptr) {
        const char *l = env->GetStringUTFChars(language, nullptr);
        if (l != nullptr) {
            lang = l;
            env->ReleaseStringUTFChars(language, l);
        }
    }
    if (lang.empty() || lang == "auto") {
        params.language = nullptr;
        params.detect_language = true;
    } else {
        params.language = lang.c_str();
        params.detect_language = false;
    }

    if (whisper_full(ctx, params, pcm.data(), (int) pcm.size()) != 0) {
        LOGE("whisper_full failed");
        return env->NewStringUTF("");
    }

    std::string result;
    const int segments = whisper_full_n_segments(ctx);
    for (int i = 0; i < segments; i++) {
        const char *text = whisper_full_get_segment_text(ctx, i);
        if (text != nullptr) {
            result += text;
        }
    }

    size_t start = result.find_first_not_of(" \t\n\r");
    if (start == std::string::npos) {
        return env->NewStringUTF("");
    }
    size_t end = result.find_last_not_of(" \t\n\r");
    result = result.substr(start, end - start + 1);

    return env->NewStringUTF(result.c_str());
}

}
