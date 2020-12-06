#include <android/log.h>
#include "libandroid_shm.h"

#define ASHMEM_DEVICE  "/dev/ashmem"

//ret= 0 创建成功；ret=-1，失败；
//注：ret =1，共享内存已经存在，但是目前这个没用，暂时放这
int create_shared_memory(const char* name, U64 size, int node, char*& addr, U64& shm_id){
    U64 fd = open(ASHMEM_DEVICE, O_RDWR);
    if (fd < 0) {
        return -1;
    }
    U64 len = ioctl(fd, ASHMEM_GET_SIZE, NULL);
    if (len > 0) {
        addr = (char*)mmap(NULL, size , PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
        shm_id = fd;
        return 1;
    } else {
        int ret = ioctl(fd, ASHMEM_SET_NAME, name);
        if (ret < 0) {
            close(fd);
            return -1;
        }
        ret = ioctl(fd, ASHMEM_SET_SIZE, size);
        if (ret < 0) {
             close(fd);
            return -1;
        }
        addr = (char*)mmap(NULL, size , PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
        shm_id = fd;
    }
    return 0;
}

int open_shared_memory(const char* name, int node, char*& addr, U64& shm_id){
    U64 size = ioctl(shm_id, ASHMEM_GET_SIZE, NULL);
    if (size > 0) {
        addr = (char*)mmap(NULL, size , PROT_READ | PROT_WRITE, MAP_SHARED, shm_id, 0);
    } else {
        return -1;
    }
    return 0;
}

int close_shared_memory(U64& shm_id, char*& addr){
    U64 size = ioctl(shm_id, ASHMEM_GET_SIZE, NULL);
    if(size <0){
        return -1;
    }
    int ret = munmap((void*)addr, size);
    if(ret == -1){
        return -1;
    }
    ret = close(shm_id);
    if(ret == -1){
        return -1;
    }
    return 0;
}

JNIEXPORT jint JNICALL Java_top_tjunet_loctag_jni_CsiCollect_createShmFromJni
  (JNIEnv *env, jclass jc) {
    char* buf;
    U64 ufd = 0;
    __android_log_print(ANDROID_LOG_VERBOSE, "jni-debug", "%d", __LINE__);
    int ret = create_shared_memory("loctag", 1024, -1, buf, ufd);
    __android_log_print(ANDROID_LOG_VERBOSE, "jni-debug", "%d", __LINE__);
    strcpy(buf, "shared_test你好");
    __android_log_print(ANDROID_LOG_VERBOSE, "jni-debug", "%d", __LINE__);
    return ufd;
  }

/*
 * Class:     top_tjunet_loctag_jni_CsiCollect
 * Method:    getDot11RtPacket
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_top_tjunet_loctag_jni_CsiCollect_getDot11RtPacket
  (JNIEnv *env, jclass jc, jint shmFd) {
    U64  ufd = (U64)shmFd;
    char* buf;
    __android_log_print(ANDROID_LOG_VERBOSE, "jni-debug", "%d", __LINE__);
    open_shared_memory("loctag", -1, buf, ufd);
    __android_log_print(ANDROID_LOG_VERBOSE, "jni-debug", "%d", __LINE__);
    char c[40];
    sprintf(c,"getDot11Rt %d %s", (int)ufd, buf);
    __android_log_print(ANDROID_LOG_VERBOSE, "jni-debug", "%d", __LINE__);
    return  env->NewStringUTF(c);
  }

/*
 * Class:     top_tjunet_loctag_jni_CsiCollect
 * Method:    closeShmFromJni
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_top_tjunet_loctag_jni_CsiCollect_closeShmFromJni
  (JNIEnv *env, jclass jc, jint shmFd) {
    U64  ufd = (U64)shmFd;
    char* buf;
    open_shared_memory("loctag", -1, buf, ufd);
    close_shared_memory(ufd, buf);
    return 0;
  }