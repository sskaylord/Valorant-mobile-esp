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
    private boolean memoryAttached = false;
    private int gamePid = -1;
    private volatile boolean running = true;
    private String debugText = "Baslatiliyor...";
    
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
        
        AntiDetect.startFullProtection();
        startMemoryReader();
        
        return START_STICKY;
    }
    
    private void startMemoryReader() {
        new Thread(() -> {
            try {
                debugText = "Process araniyor...";
                Thread.sleep(5000);
                
                gamePid = MemoryReader.findGameProcess("com.tencent.tmgp.codev");
                debugText = "PID: " + gamePid;
                
                if (gamePid > 0) {
                    MemoryReader.attachToProcess(gamePid);
                    memoryAttached = true;
                    debugText = "Baglandi PID: " + gamePid;
                    
                    Thread.sleep(2000);
                    
                    long gworld = MemoryReader.findGWorld();
                    debugText = "GWorld: " + gworld;
                    
                    if (gworld != 0) {
                        long actorArray = MemoryReader.getActorArray(gworld);
                        debugText = "ActorArray: " + actorArray;
                        
                        int actorCount = MemoryReader.getActorCount(gworld);
                        debugText = "ActorCount: " + actorCount;
                    }
                }
            } catch (Exception e) {
                debugText = "Hata: " + e.getMessage();
            }
        }).start();
    }
    
    private class ESPView extends View {
        private Paint paint;
        private Random random;
        private float[][] testPlayers;
        private Vector3[] realPlayers;
        private boolean useRealData = false;
        
        public ESPView(Context context) {
            super(context);
            paint = new Paint();
            random = new Random();
            testPlayers = new float[5][3];
            
            for (int i = 0; i < 5; i++) {
                testPlayers[i][0] = random.nextFloat() * 1080;
                testPlayers[i][1] = random.nextFloat() * 2400;
                testPlayers[i][2] = random.nextBoolean() ? 1 : 0;
            }
            
            startLoop();
        }
        
        private void startLoop() {
            new Thread(() -> {
                while (running) {
                    try {
                        if (memoryAttached && gamePid > 0) {
                            readRealPlayerData();
                        }
                        Thread.sleep(100);
                        postInvalidate();
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }).start();
        }
        
        private void readRealPlayerData() {
            try {
                long gworld = MemoryReader.findGWorld();
                if (gworld != 0) {
                    debugText = "GWorld OK: " + gworld;
                    long actorArray = MemoryReader.getActorArray(gworld);
                    int actorCount = MemoryReader.getActorCount(gworld);
                    
                    if (actorArray != 0 && actorCount > 0 && actorCount < 100) {
                        realPlayers = MemoryReader.getPlayerPositions(actorArray, actorCount);
                        useRealData = true;
                        debugText = "Oyuncu: " + realPlayers.length;
                    } else {
                        debugText = "Actor yok: " + actorArray + " count:" + actorCount;
                        useRealData = false;
                    }
                } else {
                    debugText = "GWorld YOK!";
                    useRealData = false;
                }
            } catch (Exception e) {
                debugText = "Hata: " + e.getMessage();
                useRealData = false;
            }
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            
            if (!esp || !running) return;
            
            paint.reset();
            paint.setAntiAlias(true);
            
            // DEBUG YAZISI
            paint.setColor(Color.YELLOW);
            paint.setTextSize(40);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(debugText, 50, 150, paint);
            
            if (useRealData && realPlayers != null) {
                drawRealPlayers(canvas);
            } else {
                drawTestPlayers(canvas);
            }
        }
        
        private void drawRealPlayers(Canvas canvas) {
            for (int i = 0; i < realPlayers.length; i++) {
                Vector3 pos = realPlayers[i];
                if (pos == null) continue;
                
                float screenX = (pos.X % canvas.getWidth());
                float screenY = (pos.Y % canvas.getHeight());
                
                if (screenX < 0) screenX += canvas.getWidth();
                if (screenY < 0) screenY += canvas.getHeight();
                
                float bw = 100;
                float bh = 200;
                float left = screenX - bw/2;
                float top = screenY - bh;
                float right = screenX + bw/2;
                float bottom = screenY;
                
                if (box) {
                    paint.setColor(Color.GREEN);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(2);
                    canvas.drawRect(left, top, right, bottom, paint);
                }
                
                if (lines) {
                    paint.setColor(Color.GREEN);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(1);
                    paint.setAlpha(150);
                    canvas.drawLine(canvas.getWidth()/2, canvas.getHeight(), screenX, top, paint);
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
                    
                    canvas.drawCircle(screenX, top + 25, 12, paint);
                    canvas.drawLine(screenX, top + 37, screenX, top + 50, paint);
                    canvas.drawLine(screenX, top + 50, screenX, top + 85, paint);
                    canvas.drawLine(screenX, top + 85, screenX, top + 130, paint);
                    canvas.drawLine(screenX, top + 85, screenX - 35, top + 110, paint);
                    canvas.drawLine(screenX, top + 85, screenX + 35, top + 110, paint);
                    canvas.drawLine(screenX, top + 130, screenX - 25, bottom - 30, paint);
                    canvas.drawLine(screenX, top + 130, screenX + 25, bottom - 30, paint);
                    
                    paint.setAlpha(255);
                }
            }
        }
        
        private void drawTestPlayers(Canvas canvas) {
            for (int i = 0; i < 5; i++) {
                float x = testPlayers[i][0];
                float y = testPlayers[i][1];
                boolean visible = testPlayers[i][2] == 1;
                
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
        running = false;
        if (espView != null) {
            windowManager.removeView(espView);
            espView = null;
        }
        AntiDetect.stopProtection();
        MemoryReader.detachProcess();
        stopSelf();
        super.onDestroy();
    }
                                    }
