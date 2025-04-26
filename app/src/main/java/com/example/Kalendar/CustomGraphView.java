package com.example.Kalendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import java.util.List;

public class CustomGraphView extends View {

    private List<Integer> taskData; // Список количества задач на каждый день
    private Paint linePaint;
    private Paint pointPaint;

    public CustomGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(0xFF1E88E5); // Цвет графика
        linePaint.setStrokeWidth(6f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);

        pointPaint = new Paint();
        pointPaint.setColor(0xFF1E88E5);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setAntiAlias(true);
    }

    public void setTaskData(List<Integer> data) {
        this.taskData = data;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (taskData == null || taskData.size() < 2) return;

        float width = getWidth();
        float height = getHeight();
        int max = 1;
        for (int val : taskData) {
            if (val > max) max = val;
        }

        float dx = width / (taskData.size() - 1);
        float dy = height / max;

        Path path = new Path();
        for (int i = 0; i < taskData.size(); i++) {
            float x = i * dx;
            float y = height - (taskData.get(i) * dy);

            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);

            // Рисуем точки
            canvas.drawCircle(x, y, 8f, pointPaint);
        }

        canvas.drawPath(path, linePaint);
    }
}
