package com.valorant.cheat;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import java.util.*;

public class OverlayService extends Service {
    private WindowManager windowManager;
    private ESPView espView;
    private boolean esp, box, skeleton, lines, health;
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        esp = intent.getBooleanExtra("esp", true);
        box = intent.getBooleanExtra("box", true);
        skeleton = intent.getBooleanExtra("skeleton", true);
        lines = intent.getBooleanExtra("lines", true);
        health = intent.getBooleanExtra("health", true);
        
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        espView = new ESPView(this);
        
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        );
        
        windowManager.addView(espView, params);
        
        return START_STICKY;
    }
    
    private class ESPView extends View {
        private Paint paint;
        private Random random;
        private float[][] players;
        
        public ESPView(Context context) {
            super(context);
            paint = new Paint();
            random = new Random();
            players = new float[5][3];
            
            for (int i = 0; i < 5; i++) {
                players[i][0] = random.nextFloat() * 1080;
                players[i][1] = random.nextFloat() * 2400;
                players[i][2] = random.nextBoolean() ? 1 : 0;
            }
            
            startLoop();
        }
        
        private void startLoop() {
            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(50);
                        postInvalidate();
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }).start();
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            
            if (!esp) return;
            
            paint.reset();
            paint.setAntiAlias(true);
            
            for (int i = 0; i < 5; i++) {
                float x = players[i][0];
                float y = players[i][1];
                boolean visible = players[i][2] == 1;
                
                float bw = 100;
                float bh = 200;
                float left = x - bw/2;
                float top = y - bh;
                float right = x + bw/2;
                float bottom = y;
                
                if (box) {
                    paint.setColor(visible ? Color.GREEN : Color.RED);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(2);
                    canvas.drawRect(left, top, right, bottom, paint);
                }
                
                if (lines) {
                    paint.setColor(visible ? Color.GREEN : Color.RED);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(1);
                    paint.setAlpha(150);
                    canvas.drawLine(canvas.getWidth()/2, canvas.getHeight(), x, top, paint);
                    paint.setAlpha(255);
                }
                
                if (health) {
                    paint.setColor(Color.GREEN);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawRect(left - 10, top, left - 5, bottom, paint);
                }
                
                if (skeleton) {
                    paint.setColor(Color.WHITE);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(1);
                    paint.setAlpha(200);
                    
                    canvas.drawCircle(x, top + 25, 12, paint);
                    canvas.drawLine(x, top + 37, x, top + 50, paint);
                    canvas.drawLine(x, top + 50, x, top + 85, paint);
                    canvas.drawLine(x, top + 85, x, top + 130, paint);
                    canvas.drawLine(x, top + 85, x - 35, top + 110, paint);
                    canvas.drawLine(x, top + 85, x + 35, top + 110, paint);
                    canvas.drawLine(x, top + 130, x - 25, bottom - 30, paint);
                    canvas.drawLine(x, top + 130, x + 25, bottom - 30, paint);
                    
                    paint.setAlpha(255);
                }
            }
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        if (espView != null) {
            windowManager.removeView(espView);
        }
        super.onDestroy();
    }
            }
