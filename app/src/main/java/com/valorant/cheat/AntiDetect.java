package com.valorant.cheat;

import android.os.*;
import java.io.*;
import java.util.*;

public class AntiDetect {
    static {
        System.loadLibrary("native_esp");
    }
    
    // Native metodlar
    public static native void hideThreads();
    public static native void cleanProcMaps();
    public static native void hideProcessName();
    public static native void slowRead();
    public static native boolean checkDebugger();
    public static native long safeReadLong(int pid, long address);
    public static native long decryptOffset(long encryptedOffset);
    public static native boolean isEmulator();
    public static native boolean isRooted();
    public static native void hideMemoryTrace();
    
    // Gelişmiş anti-cheat koruması
    private static boolean protectionActive = false;
    private static Thread protectionThread;
    
    public static void startProtection() {
        if (protectionActive) return;
        protectionActive = true;
        
        protectionThread = new Thread(() -> {
            while (protectionActive) {
                try {
                    // 1. Debugger kontrolü
                    if (checkDebugger()) {
                        emergencyShutdown();
                        break;
                    }
                    
                    // 2. Şüpheli process kontrolü
                    if (detectSuspiciousProcess()) {
                        emergencyShutdown();
                        break;
                    }
                    
                    // 3. Vanguard tespiti
                    if (detectVanguard()) {
                        emergencyShutdown();
                        break;
                    }
                    
                    // 4. Memory taraması tespiti
                    if (detectMemoryScan()) {
                        slowDown();
                    }
                    
                    // 5. Rastgele bekleme (tespit edilmemek için)
                    Thread.sleep(200 + new Random().nextInt(300));
                    
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        protectionThread.setName("Worker-" + UUID.randomUUID().toString().substring(0, 8));
        protectionThread.start();
    }
    
    public static void stopProtection() {
        protectionActive = false;
        if (protectionThread != null) {
            protectionThread.interrupt();
        }
    }
    
    private static boolean detectVanguard() {
        // Vanguard'ın process'lerini kontrol et
        String[] vanguardNames = {
            "vanguard", "vgk", "vg", "riot", "anticheat"
        };
        
        try {
            File procDir = new File("/proc");
            File[] processes = procDir.listFiles();
            
            if (processes != null) {
                for (File proc : processes) {
                    if (proc.isDirectory() && proc.getName().matches("\\d+")) {
                        File cmdlineFile = new File(proc, "cmdline");
                        if (cmdlineFile.exists()) {
                            BufferedReader reader = new BufferedReader(new FileReader(cmdlineFile));
                            String cmdline = reader.readLine();
                            reader.close();
                            
                            if (cmdline != null) {
                                String lower = cmdline.toLowerCase();
                                for (String name : vanguardNames) {
                                    if (lower.contains(name)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {}
        
        return false;
    }
    
    private static boolean detectSuspiciousProcess() {
        // Şüpheli process'leri kontrol et
        String[] suspiciousNames = {
            "frida", "gdb", "strace", "ltrace", "magisk", "supersu"
        };
        
        try {
            File procDir = new File("/proc");
            File[] processes = procDir.listFiles();
            
            if (processes != null) {
                for (File proc : processes) {
                    if (proc.isDirectory() && proc.getName().matches("\\d+")) {
                        File cmdlineFile = new File(proc, "cmdline");
                        if (cmdlineFile.exists()) {
                            BufferedReader reader = new BufferedReader(new FileReader(cmdlineFile));
                            String cmdline = reader.readLine();
                            reader.close();
                            
                            if (cmdline != null) {
                                String lower = cmdline.toLowerCase();
                                for (String name : suspiciousNames) {
                                    if (lower.contains(name)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {}
        
        return false;
    }
    
    private static boolean detectMemoryScan() {
        // /proc/self/maps'te şüpheli entry var mı kontrol et
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/self/maps"));
            String line;
            int suspiciousCount = 0;
            
            while ((line = reader.readLine()) != null) {
                if (line.contains("rwx") && !line.contains("/system/")) {
                    suspiciousCount++;
                }
            }
            reader.close();
            
            return suspiciousCount > 10;
        } catch (Exception e) {}
        
        return false;
    }
    
    private static void slowDown() {
        // Okumayı yavaşlat
        try {
            Thread.sleep(500 + new Random().nextInt(1000));
        } catch (InterruptedException e) {}
    }
    
    private static void emergencyShutdown() {
        // Acil kapanma - her şeyi temizle
        protectionActive = false;
        
        // Process'i öldür
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
    
    // Vanguard'ı yanıltmak için sahte process oluştur
    public static void createFakeProcesses() {
        new Thread(() -> {
            try {
                while (true) {
                    // Sahte thread'ler oluştur
                    Thread fakeThread = new Thread(() -> {
                        try {
                            Thread.sleep(Long.MAX_VALUE);
                        } catch (InterruptedException e) {}
                    });
                    fakeThread.setName("system_server_worker");
                    fakeThread.start();
                    
                    Thread.sleep(5000);
                }
            } catch (InterruptedException e) {}
        }).start();
    }
    
    // Overlay'i gizle
    public static void hideOverlay() {
        // FLAG_SECURE kullan
        // Ekran görüntüsünden gizle
    }
    
    // Log temizleme
    public static void clearLogs() {
        try {
            Runtime.getRuntime().exec("logcat -c");
        } catch (IOException e) {}
    }
    
    // Rastgele gecikme ile okuma
    public static long safeRead(int pid, long address) {
        // Rastgele gecikme
        int delay = 50 + new Random().nextInt(150);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {}
        
        return safeReadLong(pid, address);
    }
    
    // Vanguard'ın hook'ladığı syscall'ları tespit et
    public static boolean detectSyscallHooks() {
        // /proc/self/syscall kontrolü
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/self/syscall"));
            String syscall = reader.readLine();
            reader.close();
            
            // Normalde syscall numarası belirli aralıkta olmalı
            // Şüpheli değerler hook olduğunu gösterir
            return syscall == null || syscall.contains("-1");
        } catch (Exception e) {}
        
        return true;
    }
    
    // Tam koruma başlat
    public static void startFullProtection() {
        // Tüm korumaları başlat
        hideProcessName();
        hideThreads();
        cleanProcMaps();
        hideMemoryTrace();
        createFakeProcesses();
        startProtection();
        clearLogs();
    }
    }
