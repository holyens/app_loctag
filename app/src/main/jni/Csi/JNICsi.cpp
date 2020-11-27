//
// Created by shwei on 2020/11/22.
//
#include <errno.h>
#include <string.h>
#include <stdlib.h>
#include <pcap/pcap.h>
#include <android/log.h>

#include "radiotap.h"
#include "top_tjunet_loctag_jni_CsiCollect.h"

static int fcshdr = 0;

#define MAC2STR(a) (a)[0], (a)[1], (a)[2], (a)[3], (a)[4], (a)[5]
#define MACSTR "%02x:%02x:%02x:%02x:%02x:%02x"

struct nexmon_radiotap_header {
    struct ieee80211_radiotap_header header;
    unsigned int tsf_l;
    unsigned int tsf_h;
    char flags;
    unsigned char data_rate;
    unsigned short chan_freq;
    unsigned short chan_flags;
    int8_t dbm_antsignal;
    int8_t dbm_antnoise;
    char mcs[3];
    char PAD;
    unsigned char vendor_oui[3];
    unsigned char vendor_sub_namespace;
    unsigned short vendor_skip_length;
    unsigned char plcp_data[6];

} __attribute__((packed));

struct ieee80211_hdr {
    unsigned char ve: 2;
    unsigned char type: 2;
    unsigned char subtype: 4;
    unsigned char flags;
	unsigned short duration_id;
	unsigned char addr1[6];
	unsigned char addr2[6];
	unsigned char addr3[6];
	unsigned short seq_ctrl;
	unsigned short addr4[6];
} __attribute__ ((packed));

struct tagged_parameter {
     unsigned char num;
     unsigned char len;
     char buf[];
 };

const char *frame_type[4] = {
    "mgt", "ctl", "data", "ext"
};
const char *frame_subtype[4][16] = {
    {
        "00 mgt 0000 Association Request",
        "Association Response",
        "Reassociation Request",
        "Reassociation Response",
        "Probe Request",
        "Probe Response",
        "Timing Advertisement",
        "Reserved",
        "Beacon",
        "ATIM",
        "Disassociation",
        "Authentication",
        "Deauthentication",
        "Action",
        "Action No Ack",
        "Reserved"
    },
    {
        "Reserved",
        "Reserved",
        "Reserved",
        "Reserved",
        "Beamforming Report Poll",
        "VHT NDP Announcement",
        "Control Frame Extension",
        "Control Wrapper",
        "Block Ack Request (BlockAckReq)",
        "Block Ack (BlockAck)",
        "PS-Poll",
        "RTS",
        "CTS",
        "Ack",
        "CF-End",
        "CF-End +CF-Ack"
    },
    {
        "Data",
        "Data+CF-Ack",
        "Data+CF-Poll",
        "Data+CF-Ack +CF-Poll",
        "Null(no data)",
        "CF-Ack(no data)",
        "CF-Poll(no data)",
        "CF-Ack+CF-Poll(no data)",
        "QoS Data",
        "QoS Data+CF-Ack",
        "QoS Data+CF-Poll",
        "QoS Data+CF-Ack +CF-Poll",
        "QoS Null(no data)",
        "Reserved",
        "QoS CF-Poll(no data)",
        "QoS CF-Ack+CF-Poll (no data)"
    },
    {
        "DMG Beacon"
    }
};
/*
int save_packet(pcap_t *pcap_handle, const struct pcap_pkthdr *header,
        const u_char *packet, const char *file_name)
{
	pcap_dumper_t *output_file;

    output_file = pcap_dump_open(pcap_handle, file_name);
    if (output_file == NULL) {
        printf("Fail to save captured packet.\n");
        return -1;
    }

    pcap_dump((u_char *)output_file, header, packet);
    pcap_dump_close(output_file);

    return 0;
}
*/
struct JNIEnvClass {
    JNIEnv * env;
    jclass jc;
    jobject jUserObj;
};

void gotDot11RtPacket(u_char *args, const struct pcap_pkthdr *header, const u_char *packet)
{
    int err;
    char buf[64];
    static int i = 0;
    int ssid_len;
    // char *save_file_name = "wicap_capture.pcap";
    __android_log_print(ANDROID_LOG_VERBOSE, "jni-debug", "%d", __LINE__);
    struct JNIEnvClass *jniEnvClassPtr = (struct JNIEnvClass *)args;
    JNIEnv * env = jniEnvClassPtr->env;
    // save_packet((pcap_t *)args, header, packet, save_file_name);
    jclass cDot11Rt = env->FindClass("ILtop/tjunet/loctag/jni/CsiCollect$Dot11Rt");
    jobject oDot11Rt = env->NewObject(cDot11Rt, env->GetMethodID(cDot11Rt, "<init>", "()V"));

    struct nexmon_radiotap_header *rhdr = (struct nexmon_radiotap_header *)packet;
    struct ieee80211_hdr *hdr = (struct ieee80211_hdr *)(packet + rhdr->header.it_len);
    struct tagged_parameter *ssid_set = (struct tagged_parameter *)(packet + rhdr->header.it_len + 0x24);

    if (header->len<sizeof(struct nexmon_radiotap_header) || rhdr->header.it_present != 0x4008006f) {
        // printf("broke radiotap, it_present=0x%08x\n", rhdr->header.it_present);
        env->SetIntField(oDot11Rt, env->GetFieldID(cDot11Rt, "status", "I"), -1);
    } else {
        sprintf(buf, ""MACSTR, MAC2STR(hdr->addr2));
        env->SetObjectField(oDot11Rt, env->GetFieldID(cDot11Rt, "txMac", "Ljava/lang/String;"), env->NewStringUTF(buf));
        sprintf(buf, ""MACSTR, MAC2STR(hdr->addr1));
        env->SetObjectField(oDot11Rt, env->GetFieldID(cDot11Rt, "rxMac", "Ljava/lang/String;"), env->NewStringUTF(buf));
        env->SetDoubleField(oDot11Rt, env->GetFieldID(cDot11Rt, "rate", "D"), rhdr->data_rate/2.0);
        env->SetIntField(oDot11Rt, env->GetFieldID(cDot11Rt, "cFreq", "I"), rhdr->chan_freq);
        env->SetIntField(oDot11Rt, env->GetFieldID(cDot11Rt, "rssi", "I"), rhdr->dbm_antsignal);
        env->SetIntField(oDot11Rt, env->GetFieldID(cDot11Rt, "noise", "I"), rhdr->dbm_antnoise);
        env->SetObjectField(oDot11Rt, env->GetFieldID(cDot11Rt, "type", "Ljava/lang/String;"), \
            env->NewStringUTF(frame_type[hdr->type]));
        env->SetObjectField(oDot11Rt, env->GetFieldID(cDot11Rt, "subtype", "Ljava/lang/String;"), \
            env->NewStringUTF(frame_subtype[hdr->type][hdr->subtype]));
        if (hdr->type==0 && hdr->subtype==8 && ssid_set->num==0)
            sprintf(buf, "%.*s", ssid_set->len, ssid_set->buf);
            env->SetObjectField(oDot11Rt, env->GetFieldID(cDot11Rt, "SSID", "Ljava/lang/String;"), \
                env->NewStringUTF(buf));
        env->SetIntField(oDot11Rt, env->GetFieldID(cDot11Rt, "status", "I"), 0);
    }

	jclass cOnDot11RtReceivedCallback = env->GetObjectClass(jniEnvClassPtr->jUserObj);
    jmethodID midOnReceived = env->GetMethodID(cOnDot11RtReceivedCallback, "onReceived", "(Ltop/tjunet/loctag/jni/CsiCollect$Dot11Rt;)V");
    env->CallVoidMethod(jniEnvClassPtr->jUserObj, midOnReceived, oDot11Rt);
	return;
}

static pcap_t *handle;			/* Session handle */
static struct bpf_program fp;		/* The compiled filter */

JNIEXPORT jint JNICALL Java_top_tjunet_loctag_jni_CsiCollect_open
  (JNIEnv *env, jclass jc, jstring strDev, jstring strFilterExp) {

        const char *dev = env->GetStringUTFChars(strDev, NULL);	/* The device to sniff on */
        const char *filter_exp = env->GetStringUTFChars(strFilterExp, NULL);	/* The filter expression */

        bpf_u_int32 netp;
        char errbuf[PCAP_ERRBUF_SIZE];	/* Error string */

        /* check for capture device name on command-line */
    	if (NULL == dev || NULL == filter_exp)
    		return (-1);

        /* Open the session in promiscuous mode */
        handle = pcap_open_live(dev, BUFSIZ, 1, 100, errbuf);
        if (handle == NULL) {
            __android_log_print(ANDROID_LOG_VERBOSE, "jni-debug", "%s", errbuf);
            return(-2);
        }
        /* Compile and apply the filter */
        if (pcap_compile(handle, &fp, filter_exp, 0, netp) == -1) {
            __android_log_print(ANDROID_LOG_VERBOSE, "jni-debug", "%s", errbuf);
            return(-3);
        }
        if (pcap_setfilter(handle, &fp) == -1) {
            __android_log_print(ANDROID_LOG_VERBOSE, "jni-debug", "%s", errbuf);
            return(-4);
        }
        env->ReleaseStringUTFChars(strDev, dev);  // release resources
        env->ReleaseStringUTFChars(strFilterExp, filter_exp);  // release resources
        __android_log_print(ANDROID_LOG_VERBOSE, "jni-debug", "%d", __LINE__);
        return 0;
  }

JNIEXPORT jint JNICALL Java_top_tjunet_loctag_jni_CsiCollect_dot11RtLoop
  (JNIEnv *env, jclass jc, jint num_packets, jobject jniOnDot11RtReceivedCallback) {
    /* now we can set our callback function */
    struct JNIEnvClass jniEnvClass = {env, jc, jniOnDot11RtReceivedCallback};
    __android_log_print(ANDROID_LOG_VERBOSE, "jni-debug", "%d", __LINE__);
	pcap_loop(handle, num_packets, gotDot11RtPacket, (u_char *)&jniEnvClass);
	return 0;
  }

JNIEXPORT jint JNICALL Java_top_tjunet_loctag_jni_CsiCollect_shutdown
  (JNIEnv *, jclass) {
	/* cleanup */
	pcap_freecode(&fp);
	pcap_close(handle);
	return 0;
  }

// Methods
static void dataCallback(JNIEnv * env, jclass c, char * name, jint index, char * value) {
  jmethodID midStr = env->GetStaticMethodID(c, "dataCallback", "(Ljava/lang/String;ILjava/lang/String;)V");
  jstring string = env->NewStringUTF(name);
  env->CallStaticVoidMethod(c, midStr, string, index, env->NewStringUTF(value));
}

JNIEXPORT jint JNICALL Java_top_tjunet_loctag_ui_capture_CaptureFragment_ndkDoSometing
  (JNIEnv *env, jclass c, jint x) {

    dataCallback(env, c, "Greet", (jint)(92), "Hello from native code");
    return (jint)(92);
  }