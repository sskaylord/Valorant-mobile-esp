#include <jni.h>
#include <sys/uio.h>
#include <sys/syscall.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include <pthread.h>
#include <random>
#include <chrono>
#include <thread>

extern "C" JNIEXPORT void JNICALL
Java_com_valorant_cheat_AntiDetect_hideThreads(
    JNIEnv* env, jobject thiz) {
    pthread_setname_np(pthread_self(), "system_server");
}

extern "C" JNIEXPORT void JNICALL
Java_com_valorant_cheat_AntiDetect_cleanProcMaps(
    JNIEnv* env, jobject thiz) {
}

extern "C" JNIEXPORT void JNICALL
Java_com_valorant_cheat_AntiDetect_hideProcessName(
    JNIEnv* env, jobject thiz) {
    FILE* comm = fopen("/proc/self/comm", "w");
    if (comm) {
        fprintf(comm, "system_server");
        fclose(comm);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_valorant_cheat_AntiDetect_slowRead(
    JNIEnv* env, jobject thiz) {
    std::this_thread::sleep_for(std::chrono::milliseconds(2));
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_valorant_cheat_AntiDetect_checkDebugger(
    JNIEnv* env, jobject thiz) {
    FILE* status = fopen("/proc/self/status", "r");
    if (!status) return JNI_TRUE;
    char line[256];
    while (fgets(line, sizeof(line), status)) {
        if (strstr(line, "TracerPid:")) {
            int tracer_pid = 0;
            sscanf(line, "TracerPid: %d", &tracer_pid);
            fclose(status);
            return tracer_pid == 0 ? JNI_FALSE : JNI_TRUE;
        }
    }
    fclose(status);
    return JNI_TRUE;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_valorant_cheat_AntiDetect_safeReadLong(
    JNIEnv* env, jobject thiz, jint pid, jlong address) {
    std::this_thread::sleep_for(std::chrono::microseconds(100));
    long value = 0;
    struct iovec local_iov;
    struct iovec remote_iov;
    local_iov.iov_base = &value;
    local_iov.iov_len = sizeof(value);
    remote_iov.iov_base = (void*)address;
    remote_iov.iov_len = sizeof(value);
    syscall(SYS_process_vm_readv, pid,
           &local_iov, 1, &remote_iov, 1, 0);
    return value;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_valorant_cheat_AntiDetect_decryptOffset(
    JNIEnv* env, jobject thiz, jlong encrypted_offset) {
    long key = 0xDEADBEEFCAFEBABE;
    return encrypted_offset ^ key;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_valorant_cheat_AntiDetect_isEmulator(
    JNIEnv* env, jobject thiz) {
    FILE* prop = fopen("/system/build.prop", "r");
    if (!prop) return JNI_TRUE;
    char line[512];
    while (fgets(line, sizeof(line), prop)) {
        if (strstr(line, "qemu") || strstr(line, "emulator")) {
            fclose(prop);
            return JNI_TRUE;
        }
    }
    fclose(prop);
    return JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_valorant_cheat_AntiDetect_isRooted(
    JNIEnv* env, jobject thiz) {
    const char* su_paths[] = {
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/su/bin/su"
    };
    for (int i = 0; i < 4; i++) {
        if (access(su_paths[i], F_OK) == 0) {
            return JNI_TRUE;
        }
    }
    return JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_valorant_cheat_AntiDetect_hideMemoryTrace(
    JNIEnv* env, jobject thiz) {
}
