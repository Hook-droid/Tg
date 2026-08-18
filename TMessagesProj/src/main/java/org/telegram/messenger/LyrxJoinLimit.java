package org.telegram.messenger;

import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayDeque;
import java.util.HashMap;

public class LyrxJoinLimit {

    private static final int MAX_WAIT_SECONDS = 600;
    private static final int MAX_ATTEMPTS = 3;

    private static final long GAP_MIN = 8000L;
    private static final long GAP_MAX = 60000L;

    private static long currentGap = GAP_MIN;
    private static int cleanStreak;

    private static final ArrayDeque<Runnable> queue = new ArrayDeque<>();
    private static boolean running;
    private static long lastRunTime;

    private static int queuedTotal;
    private static int queuedDone;

    private static final HashMap<String, Integer> attempts = new HashMap<>();

    public static boolean isEnabled() {
        return SharedConfig.lyrxBypassJoinLimit;
    }

    public static void enqueue(Runnable joinAction) {
        if (joinAction == null) {
            return;
        }
        if (!isEnabled()) {
            joinAction.run();
            return;
        }
        synchronized (queue) {
            queue.add(joinAction);
            queuedTotal++;
        }
        scheduleNext();
    }

    private static void scheduleNext() {
        AndroidUtilities.runOnUIThread(() -> {
            if (running) {
                return;
            }
            final Runnable next;
            synchronized (queue) {
                next = queue.poll();
            }
            if (next == null) {
                queuedTotal = 0;
                queuedDone = 0;
                return;
            }
            running = true;
            long since = SystemClock_elapsed() - lastRunTime;
            long delay = lastRunTime == 0 ? 0 : Math.max(0, currentGap - since);
            AndroidUtilities.runOnUIThread(() -> {
                lastRunTime = SystemClock_elapsed();
                try {
                    next.run();
                } catch (Throwable e) {
                    FileLog.e(e);
                }
                running = false;
                queuedDone++;
                showProgress();
                scheduleNext();
            }, delay);
        });
    }

    private static long SystemClock_elapsed() {
        return android.os.SystemClock.elapsedRealtime();
    }

    private static void showProgress() {
        final int total = queuedTotal;
        final int done = queuedDone;
        if (total < 2) {
            return;
        }
        AndroidUtilities.runOnUIThread(() -> {
            try {
                org.telegram.ui.Components.BulletinFactory.global()
                        .createSimpleBulletin(R.raw.forward, "Joining channels: " + done + "/" + total)
                        .show();
            } catch (Throwable e) {
                FileLog.e(e);
            }
        });
    }

    private static void slowDown() {
        currentGap = Math.min(GAP_MAX, (long) (currentGap * 1.6f));
        cleanStreak = 0;
    }

    public static void reportSuccess() {
        cleanStreak++;
        if (cleanStreak >= 3 && currentGap > GAP_MIN) {
            currentGap = Math.max(GAP_MIN, (long) (currentGap / 1.4f));
            cleanStreak = 0;
        }
    }

    public static int parseWait(TLRPC.TL_error error) {
        if (error == null || error.text == null || !error.text.contains("FLOOD_WAIT")) {
            return -1;
        }
        try {
            int value = Integer.parseInt(error.text.substring(error.text.lastIndexOf("_") + 1).trim());
            if (value > 0 && value <= MAX_WAIT_SECONDS) {
                return value;
            }
        } catch (Exception ignore) {
        }
        return -1;
    }

    public static boolean retryImportInvite(int account, String hash, TLRPC.TL_error error) {
        if (!isEnabled() || hash == null) {
            return false;
        }
        int wait = parseWait(error);
        if (wait < 0) {
            attempts.remove(hash);
            return false;
        }
        slowDown();

        Integer done = attempts.get(hash);
        int count = done == null ? 0 : done;
        if (count >= MAX_ATTEMPTS) {
            attempts.remove(hash);
            return false;
        }
        attempts.put(hash, count + 1);

        AndroidUtilities.runOnUIThread(() -> {
            TLRPC.TL_messages_importChatInvite req = new TLRPC.TL_messages_importChatInvite();
            req.hash = hash;
            ConnectionsManager.getInstance(account).sendRequest(req, (response, retryError) -> {
                if (retryError == null) {
                    attempts.remove(hash);
                    reportSuccess();
                    if (response instanceof TLRPC.TL_chatInviteJoinResultOk) {
                        TLRPC.Updates updates = ((TLRPC.TL_chatInviteJoinResultOk) response).updates;
                        MessagesController.getInstance(account).processUpdates(updates, false);
                    }
                } else {
                    retryImportInvite(account, hash, retryError);
                }
            }, ConnectionsManager.RequestFlagFailOnServerErrors);
        }, wait * 1000L + 1000L);

        return true;
    }
}
