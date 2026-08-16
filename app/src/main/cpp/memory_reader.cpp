package com.valorant.cheat;

public class MemoryReader {
    static {
        System.loadLibrary("native_esp");
    }
    
    public static native long readLong(long address);
    public static native float readFloat(long address);
    public static native int findGameProcess(String packageName);
    public static native void attachToProcess(int pid);
    public static native void detachProcess();
    public static native long findGWorld();
    public static native long getActorArray(long gworld);
    public static native int getActorCount(long gworld);
    public static native Vector3[] getPlayerPositions(long actorArray, int actorCount);
}
