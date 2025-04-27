package com.example.Kalendar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

public class CustomGraphView extends View {

    private List<Integer> totalTasks;
    private List<Integer> completedTasks;
    private Paint totalPaint;
    private Paint completedPaint;
    private float animationProgress = 0f;

    public CustomGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        totalPaint = new Paint();
        totalPaint.setColor(0xFF1E88E5); // Синий
        totalPaint.setStrokeWidth(6f);
        totalPaint.setStyle(Paint.Style.STROKE);
        totalPaint.setAntiAlias(true);

        completedPaint = new Paint();
        completedPaint.setColor(0xFFFFD700); // Золотой
        completedPaint.setStrokeWidth(6f);
        completedPaint.setStyle(Paint.Style.STROKE);
        completedPaint.setAntiAlias(true);
    }

    public void setData(List<Integer> totalTasks, List<Integer> completedTasks) {
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        startAnimation();
    }

    private void startAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1000);
        animator.addUpdateListener(animation -> {
            animationProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (totalTasks == null || totalTasks.isEmpty()) return;

        float width = getWidth();
        float height = getHeight();
        int maxTasks = Math.max(getMax(totalTasks), getMax(completedTasks));
        if (maxTasks == 0) maxTasks = 1;

        float dx = width / (totalTasks.size() - 1);
        float dy = height / maxTasks;

        drawLine(canvas, totalTasks, totalPaint, dx, dy);
        drawLine(canvas, completedTasks, completedPaint, dx, dy);
    }

    private void drawLine(Canvas canvas, List<Integer> tasks, Paint paint, float dx, float dy) {
        for (int i = 0; i < tasks.size() - 1; i++) {
            float x1 = i * dx;
            float y1 = getHeight() - (tasks.get(i) * dy * animationProgress);

            float x2 = (i + 1) * dx;
            float y2 = getHeight() - (tasks.get(i + 1) * dy * animationProgress);

            canvas.drawLine(x1, y1, x2, y2, paint);
        }
    }

    private int getMax(List<Integer> list) {
        int max = 0;
        for (int val : list) {
            if (val > max) max = val;
        }
        return max;
    }
}
