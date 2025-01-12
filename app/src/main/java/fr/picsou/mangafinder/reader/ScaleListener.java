package fr.picsou.mangafinder.reader;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    private static View view = null;
    private static float scaleFactor = 1.0f;
    private GestureDetector gestureDetector;

    public ScaleListener(View view, GestureDetector gestureDetector) {
        this.view = view;
        this.gestureDetector = gestureDetector;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        scaleFactor *= detector.getScaleFactor();
        scaleFactor = Math.max(1.0f, Math.min(scaleFactor, 5.0f));
        view.setScaleX(scaleFactor);
        view.setScaleY(scaleFactor);
        return true;
    }

    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    public static class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (scaleFactor > 1.0f) {
                view.setTranslationX(view.getTranslationX() - distanceX);
                view.setTranslationY(view.getTranslationY() - distanceY);
            }
            return true;
        }
    }
}