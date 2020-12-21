//
// Created by shwei on 2020/12/6.
//

/*
 * @Author: Shengen Wei
 * @Github: https://github.com/holyens
 * @Date: 2020-05-08 20:53:35
 * @LastEditTime: 2020-11-19 23:19:17
 * @LastEditors: Do not edit
 * @Description:
 */
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <arpa/inet.h>
#include <fcntl.h>
#include <unistd.h>
#include <endian.h>
#include <errno.h>
#include <string.h>
#include <pcap/pcap.h>
#include <argp-extern.h>
#include "radiotap_iter.h"
#include "nex_dot11rt.h"
#include "nex_csi4339.h"
#include "mcutils.h"

#define DEFAULT_CSI_DST_PORT 5500
#define DEFALUT_LOCAL_PORT 5501
#define DEFALUT_FWDTO_PORT 5502
//#define FWD_BUF_SIZE 4096
//#define DEFAUTL_FWDFROM_IFNAME "br0"
//#define DEFAUTL_FILE_NAME "./packet-example.bin"
enum MODE{STANDALONE=1<<0, FORWARDING=1<<1 , MIXED=(1<<0)|(1<<1) };

/***********
  参数解析
************/
static char            *ifname = "wlan0";
static int             num_packets = 1;
static int             pkt_type = 0;
static char            *filter_exp = "";
static char            *filepath = "";
static int             mode = STANDALONE;
static int             delay = 0;
static int             log_level = 2;

const char *argp_program_version = "V20.12.13";
const char *argp_program_bug_address = "<shwei@tju.edu.cn>";

static char doc[] = "csiutil -- a program to receive and forward raw packet.";

static struct argp_option options[] = {
	{"interface", 'I', "STRING", 0, "Set interface name (default: wlan0)"},
	{"num", 'n', "INT", 0, "Set the number of packets be processed"},
    {"type", 't', "INT", OPTION_ARG_OPTIONAL, "Set packet type (0: unknown, 1: dot11rt, 2: nex_csi)"},
	{"filter", 'f', "STRING", OPTION_ARG_OPTIONAL, "Set pcap bpf filter. e.g. -f\"dst port 5500\" -l0"},
    {"file", 'o', "STRING", OPTION_ARG_OPTIONAL, "Set output file name"},
	{"mode", 'm', "INT", OPTION_ARG_OPTIONAL, "Set mode mask(bit-0: standalone, bit-1: forwarding)"},
	{"delay", 'd', "INT", 0, "Seconds to wait before starting capture (default: 0)"},
	{"log-level", 'l', "INT", 0, "set log level (default: 2)"},
    { 0 }
};

static error_t
parse_opt(int key, char *arg, struct argp_state *state)
{
    switch (key) {
        case 'I':
            ifname = arg;
            break;
        case 'n':
			num_packets = strtol(arg, NULL, 0);
            break;
        case 't':
			pkt_type = strtol(arg, NULL, 0);
            break;
		case 'f':
			filter_exp = arg;
            break;
		case 'o':
			filepath = arg;
            break;
		case 'm':
			mode = strtol(arg, NULL, 0);
            break;
		case 'd':
			delay = strtol(arg, NULL, 0);
            break;
		case 'l':
			log_level = strtol(arg, NULL, 0);
            break;
        default:
            return ARGP_ERR_UNKNOWN;
    }
    return 0;
}
static struct argp argp = { options, parse_opt, 0, doc };

// 全局变量

static pcap_t *pcap_handle;
pcap_dumper_t *output_file;
static int sock_fd;
static struct sockaddr_in fwdto_addr;

#define log(level, format, ...) (void)(log_level>=level && printf(format, ##__VA_ARGS__))

int create_udp_send_socket(unsigned short bind_port)
{
    int sockfd;
    struct sockaddr_in local_addr;
    // Creating socket file descriptor
    if ( (sockfd = socket(AF_INET, SOCK_DGRAM, 0)) < 0 ) {
        perror("<create_udp_send_socket> socket(AF_INET, SOCK_DGRAM, 0) failed!");
        return -1;
    }
    memset(&local_addr, 0, sizeof(local_addr));
    local_addr.sin_family = AF_INET;
    local_addr.sin_addr.s_addr = inet_addr("127.0.0.1");
    local_addr.sin_port = htons(bind_port);
    // Bind the socket with the server address
    if ( bind(sockfd, (const struct sockaddr *)&local_addr, sizeof(local_addr)) < 0 ) {
        perror("<create_udp_send_socket> bind(sockfd, ...) failed!");
        return -1;
    }
    return sockfd;
}

void process_dot11rt(pcap_t *pcap_handle, const struct pcap_pkthdr *header, const u_char *packet)
{
    int ssid_len;

    if (output_file != NULL) {
        pcap_dump((u_char *)output_file, header, packet);
    }

    struct nexmon_radiotap_header *rhdr = (struct nexmon_radiotap_header *)packet;
    struct ieee80211_hdr *hdr = (struct ieee80211_hdr *)(packet + rhdr->header.it_len);
    struct tagged_parameter *ssid_set = (struct tagged_parameter *)(packet + rhdr->header.it_len + 0x24);

    if (header->len<sizeof(struct nexmon_radiotap_header) || rhdr->header.it_present != 0x4008006f) {
        printf("broke radiotap, it_present=0x%08x\n", rhdr->header.it_present);
        return;
    }
    log(1, " "MACSTR"->"MACSTR" rate=%4.1lf chan=%d dbm=%d/%d ", \
        MAC2STR(hdr->addr2), MAC2STR(hdr->addr1), rhdr->data_rate/2.0, rhdr->chan_freq, rhdr->dbm_antsignal, rhdr->dbm_antnoise);
    ssid_len = (hdr->type==0 && hdr->subtype==8 && ssid_set->num==0)? ssid_set->len: 0;
    log(1, "[%s/%s] %.*s\n", frame_type[hdr->type], frame_subtype[hdr->type][hdr->subtype], ssid_len, ssid_set->buf);

	return;
}

void process_csi(pcap_t *pcap_handle, const struct pcap_pkthdr *header, const u_char *packet)
{
    if (output_file != NULL) {
        pcap_dump((u_char *)output_file, header, packet);
    }

    struct net_headers *nhdr = (struct net_headers *)packet;
    struct csi_header_4339 *chdr = (struct csi_header_4339 *)(packet + sizeof(struct net_headers));
    int csi_data_size = header->len - sizeof(struct net_headers) - sizeof(struct csi_header_4339);

    if (csi_data_size < 0 || chdr->magic != 0x11111111) {
        printf("broke csi packet %d\n", csi_data_size);
        return;
    } else if (!(csi_data_size==256||csi_data_size==512||csi_data_size==1024)) {
        printf("incorrect csi data size\n");
        return;
    }

    log(1, "> seq=%05d core=%d ss=%d chanspec=0x%04x size=%d\n", \
                chdr->seq_num, chdr->core_num, chdr->ss_num, chdr->chanspec, csi_data_size);
    log(2, "  src_mac=%02x:%02x:%02x:%02x:%02x:%02x\n", chdr->src_mac[0], chdr->src_mac[1], \
            chdr->src_mac[2], chdr->src_mac[3], chdr->src_mac[4], chdr->src_mac[5]);
    (void)(log_level>=3 && hexDump("  csi dump", packet, header->len));

	return;
}

void got_packet(u_char *args, const struct pcap_pkthdr *header, const u_char *packet)
{
    int send_size = 0;
    switch (pkt_type) {
        case 0:
            if (mode & FORWARDING)
                send_size = sendto(sock_fd, packet, header->len, MSG_CONFIRM, \
                        (const struct sockaddr *) &fwdto_addr, sizeof(fwdto_addr));
            break;  // don not process

        case 1:
            if (mode & STANDALONE)
                process_dot11rt(pcap_handle, header, packet);
            if (mode & FORWARDING)
                send_size = sendto(sock_fd, packet, header->len, MSG_CONFIRM, \
                            (const struct sockaddr *) &fwdto_addr, sizeof(fwdto_addr));
            break;

        case 2:
            if (mode & STANDALONE)
                process_csi(pcap_handle, header, packet);
            if (mode & FORWARDING)
                send_size = sendto(sock_fd, packet+sizeof(struct net_headers), (header->len)-sizeof(struct net_headers), MSG_CONFIRM, \
                            (const struct sockaddr *) &fwdto_addr, sizeof(fwdto_addr));
            break;

        default:
            ;
    }
}

int main(int argc, char *argv[])
{
    bpf_u_int32 netp;
    char errbuf[PCAP_ERRBUF_SIZE];	/* Error string */
    struct bpf_program fp;		/* The compiled filter */

    argp_parse(&argp, argc, argv, 0, 0, 0);

    /* Open the session in promiscuous mode */
    pcap_handle = pcap_open_live(ifname, BUFSIZ, 1, 100, errbuf);
    if (pcap_handle == NULL) {
        fprintf(stderr, "Couldn't open device %s: %s\n", ifname, errbuf);
        return(2);
    }
    /* Compile and apply the filter */
    if (pcap_compile(pcap_handle, &fp, filter_exp, 0, netp) == -1) {
        fprintf(stderr, "Couldn't parse filter %s: %s\n", filter_exp, pcap_geterr(pcap_handle));
        return(2);
    }
    if (pcap_setfilter(pcap_handle, &fp) == -1) {
        fprintf(stderr, "Couldn't install filter %s: %s\n", filter_exp, pcap_geterr(pcap_handle));
        return(2);
    }

    /* print capture info */
	log(2, "Filter expression: %s\n", filter_exp);

    if (mode & FORWARDING) {
        sock_fd = create_udp_send_socket(DEFALUT_LOCAL_PORT);
        memset(&fwdto_addr, 0, sizeof(fwdto_addr));
        fwdto_addr.sin_family = AF_INET;
        fwdto_addr.sin_addr.s_addr = inet_addr("127.0.0.1");
        fwdto_addr.sin_port = htons(DEFALUT_FWDTO_PORT);
        /* now we can set our callback function */
        sleep(delay);
    }
    if ( strlen(filepath)>0 ) {
        output_file = pcap_dump_open(pcap_handle, filepath);
        if (output_file == NULL) {
            printf("Fail to save captured packet.\n");
            return -1;
        }
    }
	pcap_loop(pcap_handle, num_packets, got_packet, NULL);
	if (mode & FORWARDING) {
        const char stopMsg[1] = {0xc5};
        int send_size = sendto(sock_fd, stopMsg, sizeof(stopMsg), MSG_CONFIRM, \
                            (const struct sockaddr *) &(fwdto_addr), sizeof(fwdto_addr));
        close(sock_fd);
    }
	/* cleanup */
	pcap_dump_close(output_file);
	pcap_freecode(&fp);
	pcap_close(pcap_handle);

	log(0, "Capture complete.\n");

    return(0);
}
