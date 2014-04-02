package com.clyng.mobile;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by IntelliJ IDEA.
 *
 * @author alximik
 * @since 7/31/12 11:17 PM
 */
public abstract class SwipeDetector implements View.OnTouchListener {

    static final String logTag = "SwipeDetector";
    static final int MIN_DISTANCE = 80;
    private float downX, downY, upX, upY;

    public abstract boolean onRightToLeftSwipe();
    public abstract boolean onLeftToRightSwipe();

    public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                downY = event.getY();
                return false;
            }
            case MotionEvent.ACTION_UP: {
                upX = event.getX();
                upY = event.getY();

                float deltaX = downX - upX;
                @SuppressWarnings("unused")
                float deltaY = downY - upY;

                // swipe horizontal?
                if(Math.abs(deltaX) > MIN_DISTANCE){
                    // left or right
                    if(deltaX < 0) { return  this.onLeftToRightSwipe();  }
                    if(deltaX > 0) { return  this.onRightToLeftSwipe();  }
                    return false;
                }
                else {
                    Log.i(logTag, "Swipe was only " + Math.abs(deltaX) + " long, need at least " + MIN_DISTANCE);
                    return false; // We don't consume the event
                }

            }
        }
        return false;
    }

}
