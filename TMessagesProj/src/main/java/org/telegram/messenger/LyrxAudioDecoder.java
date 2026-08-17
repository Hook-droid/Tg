package org.telegram.messenger;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class LyrxAudioDecoder {

    public static final int TARGET_RATE = 16000;
    public static final int MAX_SECONDS = 300;

    public static float[] decodeToWhisperPcm(String path) {
        MediaExtractor extractor = null;
        MediaCodec codec = null;
        try {
            extractor = new MediaExtractor();
            extractor.setDataSource(path);

            int trackIndex = -1;
            MediaFormat format = null;
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat f = extractor.getTrackFormat(i);
                String mime = f.getString(MediaFormat.KEY_MIME);
                if (mime != null && mime.startsWith("audio/")) {
                    trackIndex = i;
                    format = f;
                    break;
                }
            }
            if (trackIndex < 0 || format == null) {
                return null;
            }
            extractor.selectTrack(trackIndex);

            String mime = format.getString(MediaFormat.KEY_MIME);
            codec = MediaCodec.createDecoderByType(mime);
            codec.configure(format, null, null, 0);
            codec.start();

            int sampleRate = format.containsKey(MediaFormat.KEY_SAMPLE_RATE) ? format.getInteger(MediaFormat.KEY_SAMPLE_RATE) : 48000;
            int channels = format.containsKey(MediaFormat.KEY_CHANNEL_COUNT) ? format.getInteger(MediaFormat.KEY_CHANNEL_COUNT) : 1;

            ByteArrayOutputStream out = new ByteArrayOutputStream(1024 * 256);
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            boolean inputDone = false;
            boolean outputDone = false;
            long maxBytes = (long) MAX_SECONDS * sampleRate * channels * 2L;

            while (!outputDone) {
                if (!inputDone) {
                    int inIndex = codec.dequeueInputBuffer(10000);
                    if (inIndex >= 0) {
                        ByteBuffer inBuf = codec.getInputBuffer(inIndex);
                        int size = inBuf == null ? -1 : extractor.readSampleData(inBuf, 0);
                        if (size < 0) {
                            codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            inputDone = true;
                        } else {
                            codec.queueInputBuffer(inIndex, 0, size, extractor.getSampleTime(), 0);
                            extractor.advance();
                        }
                    }
                }

                int outIndex = codec.dequeueOutputBuffer(info, 10000);
                if (outIndex >= 0) {
                    if (info.size > 0) {
                        ByteBuffer outBuf = codec.getOutputBuffer(outIndex);
                        if (outBuf != null) {
                            byte[] chunk = new byte[info.size];
                            outBuf.position(info.offset);
                            outBuf.get(chunk, 0, info.size);
                            out.write(chunk);
                        }
                    }
                    codec.releaseOutputBuffer(outIndex, false);
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        outputDone = true;
                    }
                    if (out.size() > maxBytes) {
                        outputDone = true;
                    }
                } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat outFormat = codec.getOutputFormat();
                    if (outFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                        sampleRate = outFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                    }
                    if (outFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                        channels = outFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                    }
                    maxBytes = (long) MAX_SECONDS * sampleRate * channels * 2L;
                }
            }

            byte[] raw = out.toByteArray();
            if (raw.length < 4) {
                return null;
            }

            ShortBuffer shorts = ByteBuffer.wrap(raw).order(ByteOrder.nativeOrder()).asShortBuffer();
            int totalFrames = shorts.remaining() / Math.max(1, channels);
            if (totalFrames <= 0) {
                return null;
            }

            float[] mono = new float[totalFrames];
            for (int i = 0; i < totalFrames; i++) {
                int sum = 0;
                for (int c = 0; c < channels; c++) {
                    sum += shorts.get(i * channels + c);
                }
                mono[i] = (sum / (float) channels) / 32768f;
            }

            if (sampleRate == TARGET_RATE) {
                return mono;
            }
            return resample(mono, sampleRate, TARGET_RATE);
        } catch (Throwable e) {
            FileLog.e(e);
            return null;
        } finally {
            try {
                if (codec != null) {
                    codec.stop();
                    codec.release();
                }
            } catch (Throwable ignore) {
            }
            try {
                if (extractor != null) {
                    extractor.release();
                }
            } catch (Throwable ignore) {
            }
        }
    }

    private static float[] resample(float[] input, int inRate, int outRate) {
        if (input == null || input.length == 0 || inRate <= 0) {
            return input;
        }
        double ratio = (double) inRate / (double) outRate;
        int outLength = (int) (input.length / ratio);
        if (outLength <= 0) {
            return null;
        }
        float[] output = new float[outLength];
        for (int i = 0; i < outLength; i++) {
            double pos = i * ratio;
            int idx = (int) pos;
            double frac = pos - idx;
            float a = input[Math.min(idx, input.length - 1)];
            float b = input[Math.min(idx + 1, input.length - 1)];
            output[i] = (float) (a + (b - a) * frac);
        }
        return output;
    }
}
