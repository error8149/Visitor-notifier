package com.error.dhlvisitornotification;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;

public class SignatureView extends View {
    private static final String TAG = "SignatureView";
    private final Paint paint = new Paint();
    private final Path path = new Path();
    private final Rect dirtyRect = new Rect();
    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private float lastX, lastY;

    public SignatureView(Context context) {
        this(context, null);
    }

    public SignatureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
        setContentDescription("Signature area - draw your signature here");
    }

    public SignatureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
        setContentDescription("Signature area - draw your signature here");
    }

    private void initPaint() {
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(5f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        bitmapCanvas.drawColor(Color.WHITE);
        Log.d(TAG, "SignatureView initialized with size " + w + "x" + h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                lastX = x;
                lastY = y;
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                if (bitmapCanvas != null) {
                    bitmapCanvas.drawPath(path, paint);
                }
                updateDirtyRect(x, y);
                invalidate(dirtyRect);
                lastX = x;
                lastY = y;
                break;
            case MotionEvent.ACTION_UP:
                path.reset();
                invalidate();
                performClick(); // For accessibility
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        // Announce to accessibility services that signature was added
        announceForAccessibility("Signature stroke added");
        return true;
    }

    private void updateDirtyRect(float x, float y) {
        int strokeWidth = (int) (paint.getStrokeWidth() / 2);
        dirtyRect.left = (int) Math.min(lastX - strokeWidth, x - strokeWidth);
        dirtyRect.right = (int) Math.max(lastX + strokeWidth, x + strokeWidth);
        dirtyRect.top = (int) Math.min(lastY - strokeWidth, y - strokeWidth);
        dirtyRect.bottom = (int) Math.max(lastY + strokeWidth, y + strokeWidth);
    }

    public boolean isEmpty() {
        if (bitmap == null) {
            Log.d(TAG, "Bitmap is null, signature is empty");
            return true;
        }
        Bitmap emptyBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas emptyCanvas = new Canvas(emptyBitmap);
        emptyCanvas.drawColor(Color.WHITE);
        boolean isEmpty = bitmap.sameAs(emptyBitmap);
        emptyBitmap.recycle();
        Log.d(TAG, "Signature is empty: " + isEmpty);
        return isEmpty;
    }

    public Bitmap getSignatureBitmap() {
        return bitmap;
    }

    public void clear() {
        path.reset();
        if (bitmapCanvas != null) {
            bitmapCanvas.drawColor(Color.WHITE);
        }
        invalidate();
        announceForAccessibility("Signature cleared");
        Log.d(TAG, "Signature cleared");
    }
}