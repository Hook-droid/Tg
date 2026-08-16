package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.MessageObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;

public class LyrxMusicCard extends View {

    private static final int CARD_HEIGHT = 92;
    private static final int CARD_TOP = 6;
    private static final int CARD_BOTTOM_GAP = 10;
    private static final int SIDE_PADDING = 12;
    private static final int COVER_SIZE = 60;

    private final ImageReceiver coverImage;
    private final RectF cardRect = new RectF();
    private final RectF coverRect = new RectF();
    private final RectF playRect = new RectF();
    private final Path playPath = new Path();
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint playBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint playIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint titlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint authorPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private String titleText = "";
    private String authorText = "";

    private int cardColor;
    private int textColor = Color.WHITE;
    private int subTextColor = 0xB3FFFFFF;

    private Runnable onCardClick;
    private float downX, downY;

    public LyrxMusicCard(Context context) {
        super(context);
        coverImage = new ImageReceiver(this);
        coverImage.setRoundRadius(AndroidUtilities.dp(10));
        setClickable(true);
        setFocusable(true);

        titlePaint.setTypeface(AndroidUtilities.bold());
        titlePaint.setTextSize(AndroidUtilities.dp(15));
        authorPaint.setTextSize(AndroidUtilities.dp(13));
        playIconPaint.setStyle(Paint.Style.FILL);

        applyColors(0, false);

        coverImage.setDelegate((imageReceiver, set, thumb, memCache) -> {
            if (set) {
                extractColor();
            }
        });
    }

    public void setOnCardClick(Runnable r) {
        this.onCardClick = r;
    }

    public void setMusicDocument(TLRPC.Document document) {
        if (document == null) {
            setVisibility(GONE);
            return;
        }
        setVisibility(VISIBLE);
        CharSequence a = ProfileMusicView.getAuthor(document);
        CharSequence t = ProfileMusicView.getTitle(document);
        authorText = a == null ? "" : a.toString();
        titleText = t == null ? "" : t.toString();

        String artworkUrl = MessageObject.getArtworkUrl(document, false);
        if (!TextUtils.isEmpty(artworkUrl)) {
            coverImage.setImage(artworkUrl, null, null, "jpg", 0);
        } else {
            coverImage.setImageBitmap((android.graphics.drawable.Drawable) null);
            applyColors(0, false);
        }
        invalidate();
    }

    private void extractColor() {
        int dominant = 0;
        boolean has = false;
        try {
            Bitmap bmp = coverImage.getBitmap();
            if (bmp != null && !bmp.isRecycled()) {
                dominant = AndroidUtilities.getDominantColor(bmp);
                has = true;
            }
        } catch (Exception ignore) {
        }
        applyColors(dominant, has);
        invalidate();
    }

    private void applyColors(int dominant, boolean hasCover) {
        int base = Theme.getColor(Theme.key_windowBackgroundWhite);
        boolean darkTheme = AndroidUtilities.computePerceivedBrightness(base) < 0.5f;

        if (hasCover) {
            float[] hsv = new float[3];
            Color.colorToHSV(dominant, hsv);
            hsv[1] = Math.min(hsv[1], 0.5f);
            hsv[2] = darkTheme ? 0.34f : 0.9f;
            int tint = Color.HSVToColor(hsv);
            cardColor = ColorUtils.blendARGB(base, tint, darkTheme ? 0.75f : 0.55f);
        } else {
            cardColor = ColorUtils.blendARGB(base, Theme.getColor(Theme.key_windowBackgroundWhiteBlueText), darkTheme ? 0.14f : 0.1f);
        }

        boolean darkCard = AndroidUtilities.computePerceivedBrightness(cardColor) < 0.6f;
        textColor = darkCard ? Color.WHITE : 0xFF14161A;
        subTextColor = ColorUtils.setAlphaComponent(textColor, 0xA8);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                setAlpha(0.75f);
                return true;
            case MotionEvent.ACTION_UP:
                setAlpha(1f);
                float dx = Math.abs(event.getX() - downX);
                float dy = Math.abs(event.getY() - downY);
                if (dx < AndroidUtilities.dp(10) && dy < AndroidUtilities.dp(10) && onCardClick != null) {
                    onCardClick.run();
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                setAlpha(1f);
                return true;
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(CARD_HEIGHT), MeasureSpec.EXACTLY)
        );
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        coverImage.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        coverImage.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        final int pad = AndroidUtilities.dp(SIDE_PADDING);
        cardRect.set(pad, AndroidUtilities.dp(CARD_TOP), getWidth() - pad, getHeight() - AndroidUtilities.dp(CARD_BOTTOM_GAP));
        final float centerY = cardRect.centerY();
        final float radius = AndroidUtilities.dp(14);

        bgPaint.setColor(cardColor);
        canvas.drawRoundRect(cardRect, radius, radius, bgPaint);

        final float coverSize = AndroidUtilities.dp(COVER_SIZE);
        final float coverLeft = cardRect.left + AndroidUtilities.dp(10);
        final float coverTop = centerY - coverSize / 2f;
        coverRect.set(coverLeft, coverTop, coverLeft + coverSize, coverTop + coverSize);
        coverImage.setImageCoords(coverRect.left, coverRect.top, coverRect.width(), coverRect.height());
        coverImage.draw(canvas);

        final float playRadius = AndroidUtilities.dp(16);
        final float playCx = cardRect.right - AndroidUtilities.dp(14) - playRadius;
        playRect.set(playCx - playRadius, centerY - playRadius, playCx + playRadius, centerY + playRadius);
        playBgPaint.setColor(ColorUtils.setAlphaComponent(textColor, 0x24));
        canvas.drawCircle(playCx, centerY, playRadius, playBgPaint);

        final float triSize = AndroidUtilities.dp(11);
        playPath.reset();
        playPath.moveTo(playCx - triSize * 0.35f, centerY - triSize / 2f);
        playPath.lineTo(playCx + triSize * 0.55f, centerY);
        playPath.lineTo(playCx - triSize * 0.35f, centerY + triSize / 2f);
        playPath.close();
        playIconPaint.setColor(textColor);
        canvas.drawPath(playPath, playIconPaint);

        final float textLeft = coverRect.right + AndroidUtilities.dp(13);
        final float textRight = playRect.left - AndroidUtilities.dp(10);
        final float availWidth = textRight - textLeft;
        if (availWidth < AndroidUtilities.dp(20)) {
            return;
        }

        titlePaint.setColor(textColor);
        authorPaint.setColor(subTextColor);

        CharSequence title = TextUtils.ellipsize(titleText, titlePaint, availWidth, TextUtils.TruncateAt.END);
        CharSequence author = TextUtils.ellipsize(authorText, authorPaint, availWidth, TextUtils.TruncateAt.END);

        canvas.drawText(title, 0, title.length(), textLeft, centerY - AndroidUtilities.dp(3), titlePaint);
        canvas.drawText(author, 0, author.length(), textLeft, centerY + AndroidUtilities.dp(17), authorPaint);
    }
}
