package com.valorant.cheat;

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
}
