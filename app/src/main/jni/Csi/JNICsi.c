//
// Created by shwei on 2020/11/22.
//
#include <errno.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <android/log.h>

#include <bcmwifi_channels.h>
#include "nex_dot11rt.h"
#include "nex_csi4339.h"
#include "top_tjunet_loctag_jni_CsiUtils.h"

static int fcshdr = 0;

JNIEXPORT jobject JNICALL Java_top_tjunet_loctag_jni_CsiUtils_buffToDot11Rt
  (JNIEnv *env, jclass jc, jbyteArray baBuff, jint iOffset, jint iLength) {

    char buf[64];
    jclass cDot11Rt = (*env)->FindClass(env, "top/tjunet/loctag/jni/Dot11Rt");
    jobject oDot11Rt = (*env)->NewObject(env, cDot11Rt, (*env)->GetMethodID(env, cDot11Rt, "<init>", "()V"));
    jbyte* b = (jbyte *)(*env)->GetByteArrayElements(env, baBuff, NULL);
    struct nexmon_radiotap_header *rhdr = (struct nexmon_radiotap_header *)(b + iOffset);
    struct ieee80211_hdr *hdr = (struct ieee80211_hdr *)(b + iOffset + rhdr->header.it_len);
    struct tagged_parameter *ssid_set = (struct tagged_parameter *)(b + iOffset + rhdr->header.it_len + 0x24);
    if (iLength<sizeof(struct nexmon_radiotap_header) || rhdr->header.it_present != 0x4008006f) {
        // printf("broke radiotap, it_present=0x%08x\n", rhdr->header.it_present);
        (*env)->SetIntField(env, oDot11Rt, (*env)->GetFieldID(env, cDot11Rt, "status", "I"), 1);
    } else {
        sprintf(buf, ""MACSTR, MAC2STR(hdr->addr2));
        (*env)->SetObjectField(env, oDot11Rt, (*env)->GetFieldID(env, cDot11Rt, "txMac", "Ljava/lang/String;"), (*env)->NewStringUTF(env, buf));
        sprintf(buf, ""MACSTR, MAC2STR(hdr->addr1));
        (*env)->SetObjectField(env, oDot11Rt, (*env)->GetFieldID(env, cDot11Rt, "rxMac", "Ljava/lang/String;"), (*env)->NewStringUTF(env, buf));
        (*env)->SetDoubleField(env, oDot11Rt, (*env)->GetFieldID(env, cDot11Rt, "rate", "D"), rhdr->data_rate/2.0);
        (*env)->SetIntField(env, oDot11Rt, (*env)->GetFieldID(env, cDot11Rt, "cFreq", "I"), rhdr->chan_freq);
        (*env)->SetIntField(env, oDot11Rt, (*env)->GetFieldID(env, cDot11Rt, "rssi", "I"), rhdr->dbm_antsignal);
        (*env)->SetIntField(env, oDot11Rt, (*env)->GetFieldID(env, cDot11Rt, "noise", "I"), rhdr->dbm_antnoise);
        (*env)->SetIntField(env, oDot11Rt, (*env)->GetFieldID(env, cDot11Rt, "type", "I"), hdr->type);
        (*env)->SetIntField(env, oDot11Rt, (*env)->GetFieldID(env, cDot11Rt, "subtype", "I"), hdr->subtype);

        if (hdr->type==0 && hdr->subtype==8 && ssid_set->num==0)
            sprintf(buf, "%.*s", ssid_set->len, ssid_set->buf);
        else
            buf[0] = '\0';
        (*env)->SetObjectField(env, oDot11Rt, (*env)->GetFieldID(env, cDot11Rt, "SSID", "Ljava/lang/String;"), \
                (*env)->NewStringUTF(env, buf));
        (*env)->SetIntField(env, oDot11Rt, (*env)->GetFieldID(env, cDot11Rt, "status", "I"), 0);
    }
    (*env)->ReleaseByteArrayElements(env, baBuff, b, 0);
    return oDot11Rt;
}

JNIEXPORT jobject JNICALL Java_top_tjunet_loctag_jni_CsiUtils_buffToCsi4339
  (JNIEnv *env, jclass jc, jbyteArray baBuff, jint iOffset, jint iLength) {
    char buf[64];
    jclass cCsi4399 = (*env)->FindClass(env, "top/tjunet/loctag/jni/Csi4339");
    jobject oCsi4399 = (*env)->NewObject(env, cCsi4399, (*env)->GetMethodID(env, cCsi4399, "<init>", "()V"));
    jbyte* b = (jbyte *)(*env)->GetByteArrayElements(env, baBuff, NULL);

    struct csi_header_4339 *chdr = (struct csi_header_4339 *)(b + iOffset);
    int csi_data_size = iLength - sizeof(struct csi_header_4339);
    if (csi_data_size < 0 || chdr->magic != 0x11111111) {
        (*env)->SetIntField(env, oCsi4399, (*env)->GetFieldID(env, cCsi4399, "status", "I"), 1);
    } else if (!(csi_data_size==256||csi_data_size==512||csi_data_size==1024)) {
        (*env)->SetIntField(env, oCsi4399, (*env)->GetFieldID(env, cCsi4399, "status", "I"), 2);
    } else {
        int N = csi_data_size/4;
        sprintf(buf, ""MACSTR, MAC2STR(chdr->src_mac));
        (*env)->SetObjectField(env, oCsi4399, (*env)->GetFieldID(env, cCsi4399, "srcMac", "Ljava/lang/String;"), (*env)->NewStringUTF(env, buf));
        (*env)->SetIntField(env, oCsi4399, (*env)->GetFieldID(env, cCsi4399, "seq", "I"), chdr->seq_num);
        (*env)->SetIntField(env, oCsi4399, (*env)->GetFieldID(env, cCsi4399, "coreNum", "I"), chdr->core_num);
        (*env)->SetIntField(env, oCsi4399, (*env)->GetFieldID(env, cCsi4399, "ssNum", "I"), chdr->ss_num);
        (*env)->SetIntField(env, oCsi4399, (*env)->GetFieldID(env, cCsi4399, "chanspec", "I"), chdr->chanspec);
        (*env)->SetIntField(env, oCsi4399, (*env)->GetFieldID(env, cCsi4399, "chipVer", "I"), chdr->chip_ver);
        jclass cComplex = (*env)->FindClass(env, "top/tjunet/loctag/jni/Complex");
        jobjectArray jComplexArray = (*env)->NewObjectArray(env, N, cComplex, NULL);
        int i;
        for (i=0; i<N; i++) {
            (*env)->SetObjectArrayElement(env, jComplexArray, i, \
                (*env)->NewObject(env, cComplex, (*env)->GetMethodID(env, cComplex, "<init>", "(DD)V"), \
                (double)(chdr->data[i].real), (double)(chdr->data[i].imag)));
        }
        (*env)->SetObjectField(env, oCsi4399, (*env)->GetFieldID(env, cCsi4399, "csi", "[Ltop/tjunet/loctag/jni/Complex;"), jComplexArray);
        (*env)->SetIntField(env, oCsi4399, (*env)->GetFieldID(env, cCsi4399, "status", "I"), 0);
    }

    (*env)->ReleaseByteArrayElements(env, baBuff, b, 0);
    return oCsi4399;
}

JNIEXPORT jint JNICALL Java_top_tjunet_loctag_jni_CsiUtils_strToChanspec
  (JNIEnv *env, jclass jc, jstring str) {
    const char *chanspecStr = (*env)->GetStringUTFChars(env, str, NULL);
    if (NULL == chanspecStr) return 0;
    int chanspec = wf_chspec_aton(chanspecStr);
    (*env)->ReleaseStringUTFChars(env, str, chanspecStr);
    return chanspec;
  }

/*
 * Class:     top_tjunet_loctag_jni_CsiUtils
 * Method:    chspec_ntoa
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_top_tjunet_loctag_jni_CsiUtils_chanspecToStr
  (JNIEnv *env, jclass jc, jint chanspec) {
    char chanspecStr[CHANSPEC_STR_LEN];
    return (*env)->NewStringUTF(env, wf_chspec_ntoa(chanspec, chanspecStr));
  }