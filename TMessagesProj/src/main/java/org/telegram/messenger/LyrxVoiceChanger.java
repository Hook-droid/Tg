package org.telegram.messenger;

import java.nio.ByteBuffer;

public class LyrxVoiceChanger {

    public static final int OFF = 0;

    public static final String[] NAMES = {
            "Off",
            "Robot",
            "Deep Voice",
            "High Voice",
            "Female",
            "Male",
            "Echo",
            "Cave",
            "Telephone",
            "Underwater",
            "Alien"
    };

    private static final float[] PITCH = {
            1f,      // off
            1f,      // robot
            0.72f,   // deep
            1.45f,   // high
            1.32f,   // female
            0.80f,   // male
            1f,      // echo
            1f,      // cave
            1f,      // telephone
            0.90f,   // underwater
            1.20f    // alien
    };

    private static final int PITCH_WINDOW = 4096;
    private static final int DELAY_SIZE = 48000;

    private static float[] pitchBuffer;
    private static int pitchWrite;
    private static float pitchRead;

    private static float[] delayBuffer;
    private static int delayWrite;

    private static double phase;
    private static float lowpassState;
    private static float highpassState;
    private static float highpassPrev;

    public static synchronized void reset() {
        pitchWrite = 0;
        pitchRead = 0;
        delayWrite = 0;
        phase = 0;
        lowpassState = 0;
        highpassState = 0;
        highpassPrev = 0;
        if (pitchBuffer != null) {
            java.util.Arrays.fill(pitchBuffer, 0f);
        }
        if (delayBuffer != null) {
            java.util.Arrays.fill(delayBuffer, 0f);
        }
    }

    public static synchronized void process(ByteBuffer buffer, int length, int sampleRate) {
        int effect = SharedConfig.lyrxVoiceEffect;
        if (effect <= OFF || effect >= NAMES.length || buffer == null || length < 2) {
            return;
        }
        try {
            if (pitchBuffer == null) {
                pitchBuffer = new float[PITCH_WINDOW];
                delayBuffer = new float[DELAY_SIZE];
            }

            int count = length / 2;
            int startPosition = buffer.position();

            for (int i = 0; i < count; i++) {
                short raw = buffer.getShort(startPosition + i * 2);
                float sample = raw;

                float pitch = PITCH[effect];
                if (pitch != 1f) {
                    sample = pitchShift(sample, pitch);
                }

                switch (effect) {
                    case 1: { // Robot
                        phase += 2 * Math.PI * 70.0 / sampleRate;
                        if (phase > 2 * Math.PI) phase -= 2 * Math.PI;
                        sample *= (float) Math.cos(phase);
                        break;
                    }
                    case 6: { // Echo
                        int readIndex = delayWrite - (int) (sampleRate * 0.22f);
                        if (readIndex < 0) readIndex += DELAY_SIZE;
                        float echo = delayBuffer[readIndex] * 0.55f;
                        sample = sample + echo;
                        delayBuffer[delayWrite] = sample;
                        delayWrite = (delayWrite + 1) % DELAY_SIZE;
                        break;
                    }
                    case 7: { // Cave
                        float wet = 0;
                        float[] taps = {0.13f, 0.19f, 0.29f, 0.41f};
                        float[] gains = {0.42f, 0.32f, 0.24f, 0.16f};
                        for (int t = 0; t < taps.length; t++) {
                            int readIndex = delayWrite - (int) (sampleRate * taps[t]);
                            if (readIndex < 0) readIndex += DELAY_SIZE;
                            wet += delayBuffer[readIndex] * gains[t];
                        }
                        sample = sample * 0.7f + wet;
                        delayBuffer[delayWrite] = sample;
                        delayWrite = (delayWrite + 1) % DELAY_SIZE;
                        break;
                    }
                    case 8: { // Telephone
                        lowpassState += (sample - lowpassState) * 0.34f;
                        float band = lowpassState;
                        highpassState = 0.93f * (highpassState + band - highpassPrev);
                        highpassPrev = band;
                        sample = highpassState * 1.6f;
                        break;
                    }
                    case 9: { // Underwater
                        lowpassState += (sample - lowpassState) * 0.10f;
                        phase += 2 * Math.PI * 3.0 / sampleRate;
                        if (phase > 2 * Math.PI) phase -= 2 * Math.PI;
                        sample = lowpassState * (0.75f + 0.25f * (float) Math.sin(phase));
                        break;
                    }
                    case 10: { // Alien
                        phase += 2 * Math.PI * 180.0 / sampleRate;
                        if (phase > 2 * Math.PI) phase -= 2 * Math.PI;
                        sample = sample * (0.6f + 0.4f * (float) Math.cos(phase));
                        break;
                    }
                    default:
                        break;
                }

                if (sample > 32767f) sample = 32767f;
                if (sample < -32768f) sample = -32768f;
                buffer.putShort(startPosition + i * 2, (short) sample);
            }
            buffer.position(startPosition);
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    private static float pitchShift(float input, float ratio) {
        pitchBuffer[pitchWrite] = input;
        pitchWrite = (pitchWrite + 1) % PITCH_WINDOW;

        pitchRead += ratio;
        while (pitchRead >= PITCH_WINDOW) {
            pitchRead -= PITCH_WINDOW;
        }

        int index = (int) pitchRead;
        int next = (index + 1) % PITCH_WINDOW;
        float fraction = pitchRead - index;
        float first = pitchBuffer[index] * (1 - fraction) + pitchBuffer[next] * fraction;

        int offsetIndex = (index + PITCH_WINDOW / 2) % PITCH_WINDOW;
        int offsetNext = (offsetIndex + 1) % PITCH_WINDOW;
        float second = pitchBuffer[offsetIndex] * (1 - fraction) + pitchBuffer[offsetNext] * fraction;

        float distance = Math.abs(pitchRead - pitchWrite);
        float blend = distance / PITCH_WINDOW;
        if (blend > 1f) blend = 1f;
        if (blend < 0f) blend = 0f;

        return first * (1 - blend) + second * blend;
    }
}
