package com.example.hydrationv2r;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class WaveView extends View {
    private Path wavePath = new Path();
    private Paint wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float waveOffset = 0;
    private float waterLevel = 1.0f; // 1.0 is empty, 0.0 is full screen

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        wavePaint.setColor(Color.parseColor("#8042A5F5"));
        wavePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        wavePath.reset();

        float width = getWidth();
        float height = getHeight();
        float currentHeight = height * waterLevel;

        wavePath.moveTo(0, currentHeight);

        // Draw the Sine Wave
        for (float x = 0; x <= width; x++) {
            float y = (float) (Math.sin((x / width * 2 * Math.PI) + waveOffset) * 20) + currentHeight;
            wavePath.lineTo(x, y);
        }

        wavePath.lineTo(width, height);
        wavePath.lineTo(0, height);
        wavePath.close();

        canvas.drawPath(wavePath, wavePaint);

        // Animate the "slosh" horizontally
        waveOffset += 0.1f;
        postInvalidateOnAnimation();
    }

    public void setProgress(float progress) { // 0.0 to 1.0
        this.waterLevel = 1f - progress;
        invalidate();
    }

    public float getProgress() {
        return 1f - this.waterLevel;
    }
}