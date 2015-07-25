package inderjeet.com.touchgesture;

import android.graphics.Rect;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;


public class MainActivity extends ActionBarActivity {
    FrameLayout layout;
    RelativeLayout main;
    View line;
    Rect rect = null;
    private int _xDelta;
    private float density = 0;
    private float scaleFactor;
    private static String TAG = "MainActivity";
    ScaleGestureDetector scaleGestureDetector;
    private GestureDetectorCompat mDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        main = (RelativeLayout) findViewById(R.id.main_panel);
        line = (View) findViewById(R.id.drag_line);

        mDetector = new GestureDetectorCompat(MainActivity.this, new swipeGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(MainActivity.this, new pinchOnScaleGestureListener());
        density = getResources().getDisplayMetrics().density;
        final View parent = (View) line.getParent();

        parent.post(new Runnable() {
            @Override
            public void run() {
                rect = new Rect();
                line.getHitRect(rect);
                rect.top -= density * 50;
                rect.left -= density * 100;
                rect.bottom += density * 50;
                rect.right += density * 100;
                parent.setTouchDelegate( new TouchDelegate( rect , line));
            }
        });

        /**
         * Handling pinch in/out on parent layout @main
         */

        main.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent ev) {
                if(ev.getPointerCount() > 1) {
                    scaleGestureDetector.onTouchEvent(ev);
                    return true;
                }   else {
                    mDetector.onTouchEvent(ev);
                    return true;
                }
            }
        });

        /**
         *OnTouch on libe view for handling drag of line over framelayout and extarcting coordinates
         */

        line.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                final int action = motionEvent.getAction();
                final int x = (int) motionEvent.getRawX();
                switch (action & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN: {
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
                        _xDelta = x - params.leftMargin;
                        return true;
                    }
                    case MotionEvent.ACTION_MOVE:{
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
                        params.leftMargin = x - _xDelta;
                        line.setLayoutParams(params);
                        if(line.getX() < layout.getWidth() && line.getX() > 0) {
                            line.invalidate();
                        }
                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        Log.d(TAG, "Line Touch UP");
                    }
                }
                return true;
            }
        });

    }
    /**
     * Definition of pinch class to handle both pinch IN/OUT
     */

    public class pinchOnScaleGestureListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            scaleFactor = detector.getScaleFactor();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if(scaleFactor > 1){
                Log.d(TAG, "ZOOM OUT");
            } else {
                Log.d(TAG, "ZOOM IN");
            }
        }
    }
    class swipeGestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_MIN_DISTANCE = 200;
        private static final int SWIPE_MAX_OFF_PATH = 250;
        private static final int SWIPE_THRESHOLD_VELOCITY = 1000;

        @Override
        public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                return false;
            }
            final float distance = e1.getX() - e2.getX();
            final boolean enoughSpeed = Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY;
            if (distance > SWIPE_MIN_DISTANCE && enoughSpeed) {
                Log.d(TAG, "SWIPE LEFT");
                return true;
            } else if (distance < -SWIPE_MIN_DISTANCE && enoughSpeed) {
                Log.d(TAG, "SWIPE RIGHT");
                return true;
            } else {
                return false;
            }
        }
    }
}
