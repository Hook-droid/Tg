package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.MessageObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;

public class LyrxMusicCard extends View {

    private final ImageReceiver coverImage;
    private final RectF cardRect = new RectF();
    private final RectF coverRect = new RectF();
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint titlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint authorPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint albumPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private String titleText = "";
    private String authorText = "";
    private String albumText = "";

    private int cardColor = 0xFF2B2B33;
    private int textColor = Color.WHITE;
    private int subTextColor = 0xB3FFFFFF;

    private Runnable onCardClick;
    private float downX, downY;

    public void setOnCardClick(Runnable r) {
        this.onCardClick = r;
    }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        switch (event.getAction()) {
            case android.view.MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                return true;
            case android.view.MotionEvent.ACTION_UP:
                float dx = Math.abs(event.getX() - downX);
                float dy = Math.abs(event.getY() - downY);
                if (dx < AndroidUtilities.dp(10) && dy < AndroidUtilities.dp(10)) {
                    if (onCardClick != null) {
                        onCardClick.run();
                    }
                }
                return true;
        }
        return true;
    }

    public LyrxMusicCard(Context context) {
        super(context);
        coverImage = new ImageReceiver(this);
        coverImage.setRoundRadius(AndroidUtilities.dp(10));
        setClickable(true);
        setFocusable(true);

        titlePaint.setTypeface(AndroidUtilities.bold());
        titlePaint.setTextSize(AndroidUtilities.dp(16));
        authorPaint.setTextSize(AndroidUtilities.dp(14));
        albumPaint.setTextSize(AndroidUtilities.dp(13));

        coverImage.setDelegate((imageReceiver, set, thumb, memCache) -> {
            if (set) {
                extractColor();
            }
        });
    }

    public void setMusicDocument(TLRPC.Document document) {
        if (document == null) {
            setVisibility(GONE);
            return;
        }
        setVisibility(VISIBLE);
        CharSequence a = getAuthor(document);
        CharSequence t = getTitle(document);
        authorText = a == null ? "" : a.toString();
        titleText = t == null ? "" : t.toString();
        albumText = getAlbum(document);

        String artworkUrl = MessageObject.getArtworkUrl(document, false);
        if (!TextUtils.isEmpty(artworkUrl)) {
            coverImage.setImage(artworkUrl, null, null, "jpg", 0);
        } else {
            coverImage.setImageBitmap((android.graphics.drawable.Drawable) null);
            extractColor();
        }
        invalidate();
    }

    private void extractColor() {
        try {
            Bitmap bmp = coverImage.getBitmap();
            if (bmp != null && !bmp.isRecycled()) {
                int dominant = AndroidUtilities.getDominantColor(bmp);
                float[] hsv = new float[3];
                Color.colorToHSV(dominant, hsv);
                hsv[1] = Math.min(1f, hsv[1] * 1.1f);
                hsv[2] = Math.min(0.55f, hsv[2] * 0.7f);
                cardColor = Color.HSVToColor(hsv);
            } else {
                cardColor = 0xFF2B2B33;
            }
        } catch (Exception e) {
            cardColor = 0xFF2B2B33;
        }
        boolean darkBg = AndroidUtilities.computePerceivedBrightness(cardColor) < 0.6f;
        textColor = darkBg ? Color.WHITE : Color.BLACK;
        subTextColor = ColorUtils.setAlphaComponent(textColor, 0xB3);
        invalidate();
    }

    private String getAlbum(TLRPC.Document document) {
        if (document == null) return "";
        for (int a = 0; a < document.attributes.size(); a++) {
            TLRPC.DocumentAttribute attribute = document.attributes.get(a);
            if (attribute instanceof TLRPC.TL_documentAttributeAudio) {
                return "";
            }
        }
        return "";
    }

    private CharSequence getAuthor(TLRPC.Document document) {
        return ProfileMusicView.getAuthor(document);
    }

    private CharSequence getTitle(TLRPC.Document document) {
        return ProfileMusicView.getTitle(document);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(88), MeasureSpec.EXACTLY)
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
        int pad = AndroidUtilities.dp(12);
        cardRect.set(pad, AndroidUtilities.dp(8), getWidth() - pad, getHeight() - AndroidUtilities.dp(18));
        bgPaint.setColor(cardColor);
        float radius = AndroidUtilities.dp(16);
        canvas.drawRoundRect(cardRect, radius, radius, bgPaint);

        int coverSize = AndroidUtilities.dp(68);
        int coverLeft = pad + AndroidUtilities.dp(10);
        int coverTop = (getHeight() - coverSize) / 2;
        coverRect.set(coverLeft, coverTop, coverLeft + coverSize, coverTop + coverSize);
        coverImage.setImageCoords(coverRect.left, coverRect.top, coverRect.width(), coverRect.height());
        coverImage.draw(canvas);

        int textLeft = coverLeft + coverSize + AndroidUtilities.dp(14);
        int textRight = (int) cardRect.right - AndroidUtilities.dp(14);
        int availWidth = textRight - textLeft;
        if (availWidth < AndroidUtilities.dp(20)) return;

        titlePaint.setColor(textColor);
        authorPaint.setColor(subTextColor);

        CharSequence title = TextUtils.ellipsize(titleText, titlePaint, availWidth, TextUtils.TruncateAt.END);
        CharSequence author = TextUtils.ellipsize(authorText, authorPaint, availWidth, TextUtils.TruncateAt.END);

        float titleY = getHeight() / 2f - AndroidUtilities.dp(4);
        float authorY = getHeight() / 2f + AndroidUtilities.dp(16);

        canvas.drawText(title, 0, title.length(), textLeft, titleY, titlePaint);
        canvas.drawText(author, 0, author.length(), textLeft, authorY, authorPaint);
    }
}
