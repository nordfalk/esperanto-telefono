/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Ændret/ŝanĝta de: Jacob Nordfalk
 */

package dk.nordfalk.esperanto.murpapero;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import static java.lang.Math.*;

public class SteloMurpapero extends WallpaperService {

    Handler guiTråd = new Handler();

    Paint stjernePaint = new Paint();
    Paint tekstPaint = new Paint();
    Path stjernePath;
    String TAG = "Esperanto";
    String[] ordliste;

    public SteloMurpapero() {
        stjernePaint.setColor(0xff80ff80);
        //stjernePaint.setTextSize(36);
        stjernePaint.setAntiAlias(true);
        //stjernePaint.setStrokeWidth(2);
        stjernePaint.setStrokeCap(Paint.Cap.ROUND);
        stjernePaint.setStyle(Paint.Style.FILL);

        tekstPaint.setColor(0xff00a000);
        tekstPaint.setTextSize(24);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate()");
        Reader br = new InputStreamReader(getResources().openRawResource(R.raw.vortlisto));

        char[] cha = new char[10000];
        int n = 0;
        StringBuilder sb = new StringBuilder(cha.length);
        try {
          while ((n=br.read(cha))>0) {
            sb.append(cha, 0, n);
          }
        } catch (IOException ex) {
          Log.e(TAG, "læse ordliste", ex);
        }
                
        ordliste = sb.toString().replaceAll("\t"," = ").split("\n");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onCreate()");
    }

    @Override
    public Engine onCreateEngine() {
        Log.d(TAG,"onCreateEngine()");
        return new EsperantoEngine();
    }

    class EsperantoEngine extends Engine {

        private float offset;
        private float mTouchX = -1;
        private float mTouchY = -1;
        private float mCenterX;
        private float mCenterY;

        private final Runnable gentegnRunnable = new Runnable() {
            public void run() {
                gentegn();
            }
        };
        private boolean mVisible;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

            // By default we don't get touch events, so enable them.
            setTouchEventsEnabled(true);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            guiTråd.removeCallbacks(gentegnRunnable);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            mVisible = visible;
            if (visible) {
                gentegn();
            } else {
                guiTråd.removeCallbacks(gentegnRunnable);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            // store the center of the surface, so we can draw the cube in the right spot
            mCenterX = width/2.0f;
            mCenterY = height/2.0f;
            if (stjernePath==null) {
              float m = Math.min(width, height)/2.0f;
              stjernePath = new Path();
              
              double v = 2*PI/5;
              stjernePath.moveTo(0,m);
              stjernePath.lineTo( (float) (m*sin(v*3)),(float) (m*cos(v*3)));
              stjernePath.lineTo( (float) (m*sin(v*1)),(float) (m*cos(v*1)));
              stjernePath.lineTo( (float) (m*sin(v*4)),(float) (m*cos(v*4)));
              stjernePath.lineTo( (float) (m*sin(v*2)),(float) (m*cos(v*2)));
/*
              stjernePath.moveTo(0,-200*m);
              stjernePath.lineTo(200*m,300*m);
              stjernePath.lineTo(-300*m,0);
              stjernePath.lineTo(300*m,0);
              stjernePath.lineTo(-200*m,300*m);
*/
              stjernePath.close();
            }
            gentegn();
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mVisible = false;
            guiTråd.removeCallbacks(gentegnRunnable);
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xStep, float yStep, int xPixels, int yPixels) {
            offset = xOffset;
            Log.d(TAG, "onOffsetsChanged() "+offset );
            gentegn();
        }

        /*
         * Store the position of the touch event so we can use it for drawing later
         */
        @Override
        public void onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                mTouchX = event.getX();
                mTouchY = event.getY();
            } else {
                mTouchX = -1;
                mTouchY = -1;
            }
            super.onTouchEvent(event);
        }

        /*
         * Draw one frame of the animation. This method gets called repeatedly
         * by posting a delayed Runnable. You can do any drawing you want in
         * here. This example draws a wireframe cube.
         */
        void gentegn() {
            SurfaceHolder holder = getSurfaceHolder();

            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) {
                    // draw something
                    tegnStjerne(c);
                    tegnTrykpunkt(c);
                }
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c);
            }

            // Reschedule the next redraw
            guiTråd.removeCallbacks(gentegnRunnable);
            if (mVisible) {
                guiTråd.postDelayed(gentegnRunnable, 1000);
            }
        }

        /*
         * Tegn en stjerne
         */
        void tegnStjerne(Canvas c) {
            c.save();
            c.translate(mCenterX, mCenterY);
            c.drawColor(0xff000000);

            float vinkel = System.currentTimeMillis()/1000%36000 +offset*360*2;
            //Log.d(TAG, "vinkel="+vinkel);
            c.rotate( vinkel );
            c.drawPath(stjernePath, stjernePaint);
            c.drawTextOnPath(ordliste[ (int) (vinkel/20) % ordliste.length ], stjernePath, 0, -20, tekstPaint);

            c.restore();
        }


        /*
         * Draw a circle around the current touch point, if any.
         */
        void tegnTrykpunkt(Canvas c) {
            if (mTouchX >=0 && mTouchY >= 0) {
                c.drawCircle(mTouchX, mTouchY, 80, tekstPaint);
            }
        }

    }
}
