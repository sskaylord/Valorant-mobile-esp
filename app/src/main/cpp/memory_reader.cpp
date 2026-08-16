#include <jni.h>
#include <sys/uio.h>
#include <sys/syscall.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include <vector>
#include <string>

struct FVector {
    float X, Y, Z;
};

static pid_t game_pid = -1;

extern "C" JNIEXPORT jlong JNICALL
Java_com_valorant_cheat_MemoryReader_readLong(
    JNIEnv* env, jobject thiz, jlong address) {
    if (game_pid <= 0) return 0;
    long value = 0;
    struct iovec local_iov;
    struct iovec remote_iov;
    local_iov.iov_base = &value;
    local_iov.iov_len = sizeof(value);
    remote_iov.iov_base = (void*)address;
    remote_iov.iov_len = sizeof(value);
    ssize_t bytes = syscall(SYS_process_vm_readv, game_pid,
                           &local_iov, 1, &remote_iov, 1, 0);
    if (bytes != sizeof(value)) return 0;
    return value;
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_valorant_cheat_MemoryReader_readFloat(
    JNIEnv* env, jobject thiz, jlong address) {
    if (game_pid <= 0) return 0;
    float value = 0;
    struct iovec local_iov;
    struct iovec remote_iov;
    local_iov.iov_base = &value;
    local_iov.iov_len = sizeof(value);
    remote_iov.iov_base = (void*)address;
    remote_iov.iov_len = sizeof(value);
    ssize_t bytes = syscall(SYS_process_vm_readv, game_pid,
                           &local_iov, 1, &remote_iov, 1, 0);
    if (bytes != sizeof(value)) return 0;
    return value;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_valorant_cheat_MemoryReader_findGameProcess(
    JNIEnv* env, jobject thiz, jstring packageName) {
    const char* pkg = env->GetStringUTFChars(packageName, nullptr);
    char path[256];
    char cmdline[256];
    for (int pid = 1; pid < 20000; pid++) {
        sprintf(path, "/proc/%d/cmdline", pid);
        FILE* f = fopen(path, "r");
        if (f) {
            if (fgets(cmdline, sizeof(cmdline), f)) {
                if (strstr(cmdline, pkg) || strstr(cmdline, "codev")) {
                    fclose(f);
                    env->ReleaseStringUTFChars(packageName, pkg);
                    return pid;
                }
            }
            fclose(f);
        }
    }
    env->ReleaseStringUTFChars(packageName, pkg);
    return -1;
}

extern "C" JNIEXPORT void JNICALL
Java_com_valorant_cheat_MemoryReader_attachToProcess(
    JNIEnv* env, jobject thiz, jint pid) {
    game_pid = pid;
}

extern "C" JNIEXPORT void JNICALL
Java_com_valorant_cheat_MemoryReader_detachProcess(
    JNIEnv* env, jobject thiz) {
    game_pid = -1;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_valorant_cheat_MemoryReader_findGWorld(
    JNIEnv* env, jobject thiz) {
    if (game_pid <= 0) return 0;
    char maps_path[256];
    sprintf(maps_path, "/proc/%d/maps", game_pid);
    FILE* maps = fopen(maps_path, "r");
    if (!maps) return 0;
    char line[512];
    uintptr_t ue4_base = 0;
    while (fgets(line, sizeof(line), maps)) {
        if (strstr(line, "libUE4.so") || strstr(line, "libil2cpp.so")) {
            sscanf(line, "%lx", &ue4_base);
            break;
        }
    }
    fclose(maps);
    if (!ue4_base) return 0;
    uintptr_t gworld_ptr = ue4_base + 0x8A00000;
    long gworld = 0;
    struct iovec local_iov;
    struct iovec remote_iov;
    local_iov.iov_base = &gworld;
    local_iov.iov_len = sizeof(gworld);
    remote_iov.iov_base = (void*)gworld_ptr;
    remote_iov.iov_len = sizeof(gworld);
    syscall(SYS_process_vm_readv, game_pid,
           &local_iov, 1, &remote_iov, 1, 0);
    return gworld;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_valorant_cheat_MemoryReader_getActorArray(
    JNIEnv* env, jobject thiz, jlong gworld) {
    if (game_pid <= 0 || !gworld) return 0;
    long persistent_level = 0;
    struct iovec local_iov;
    struct iovec remote_iov;
    local_iov.iov_base = &persistent_level;
    local_iov.iov_len = sizeof(persistent_level);
    remote_iov.iov_base = (void*)(gworld + 0x30);
    remote_iov.iov_len = sizeof(persistent_level);
    syscall(SYS_process_vm_readv, game_pid,
           &local_iov, 1, &remote_iov, 1, 0);
    if (!persistent_level) return 0;
    long actor_array = 0;
    local_iov.iov_base = &actor_array;
    local_iov.iov_len = sizeof(actor_array);
    remote_iov.iov_base = (void*)(persistent_level + 0x98);
    remote_iov.iov_len = sizeof(actor_array);
    syscall(SYS_process_vm_readv, game_pid,
           &local_iov, 1, &remote_iov, 1, 0);
    return actor_array;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_valorant_cheat_MemoryReader_getActorCount(
    JNIEnv* env, jobject thiz, jlong gworld) {
    if (game_pid <= 0 || !gworld) return 0;
    long persistent_level = 0;
    int actor_count = 0;
    struct iovec local_iov;
    struct iovec remote_iov;
    local_iov.iov_base = &persistent_level;
    local_iov.iov_len = sizeof(persistent_level);
    remote_iov.iov_base = (void*)(gworld + 0x30);
    remote_iov.iov_len = sizeof(persistent_level);
    syscall(SYS_process_vm_readv, game_pid,
           &local_iov, 1, &remote_iov, 1, 0);
    if (!persistent_level) return 0;
    local_iov.iov_base = &actor_count;
    local_iov.iov_len = sizeof(actor_count);
    remote_iov.iov_base = (void*)(persistent_level + 0xA0);
    remote_iov.iov_len = sizeof(actor_count);
    syscall(SYS_process_vm_readv, game_pid,
           &local_iov, 1, &remote_iov, 1, 0);
    return actor_count;
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_valorant_cheat_MemoryReader_getPlayerPositions(
    JNIEnv* env, jobject thiz, jlong actor_array, jint actor_count) {
    if (game_pid <= 0 || !actor_array || actor_count <= 0) return nullptr;
    jclass vectorClass = env->FindClass("com/valorant/cheat/Vector3");
    jmethodID constructor = env->GetMethodID(vectorClass, "<init>", "(FFF)V");
    jobjectArray positions = env->NewObjectArray(actor_count, vectorClass, nullptr);
    struct iovec local_iov;
    struct iovec remote_iov;
    for (int i = 0; i < actor_count && i < 100; i++) {
        long actor = 0;
        local_iov.iov_base = &actor;
        local_iov.iov_len = sizeof(actor);
        remote_iov.iov_base = (void*)(actor_array + i * 8);
        remote_iov.iov_len = sizeof(actor);
        syscall(SYS_process_vm_readv, game_pid,
               &local_iov, 1, &remote_iov, 1, 0);
        if (!actor) continue;
        long root_component = 0;
        local_iov.iov_base = &root_component;
        local_iov.iov_len = sizeof(root_component);
        remote_iov.iov_base = (void*)(actor + 0x180);
        remote_iov.iov_len = sizeof(root_component);
        syscall(SYS_process_vm_readv, game_pid,
               &local_iov, 1, &remote_iov, 1, 0);
        if (!root_component) continue;
        FVector position;
        local_iov.iov_base = &position;
        local_iov.iov_len = sizeof(position);
        remote_iov.iov_base = (void*)(root_component + 0x1E0);
        remote_iov.iov_len = sizeof(position);
        syscall(SYS_process_vm_readv, game_pid,
               &local_iov, 1, &remote_iov, 1, 0);
        jobject posObj = env->NewObject(vectorClass, constructor,
                                       position.X, position.Y, position.Z);
        env->SetObjectArrayElement(positions, i, posObj);
    }
    return positions;
}
