//
// Created by shwei on 2020/12/19.
//
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <errno.h>
#include <sys/time.h>
#include <sys/ioctl.h>
#include <arpa/inet.h>
#include <linux/if_ether.h>
#include <net/if.h>
#include <netpacket/packet.h>
#include <argp-extern.h>

#include "mcutils.h"
#include "nex_inject.h"

#define MAX_FRAME_SIZE 4096

static struct nex_inject_radiotap nex_rt = {
    .it_version = 0,
    .it_pad = 0,
    .it_len = sizeof(struct nex_inject_radiotap),
    .it_present = BIT(30)|BIT(31), //  0x00028004//
    .it_present_nex = BIT(0)|BIT(1)|BIT(2),
    .vendor_oui = {'N', 'E', 'X'},  // 0x004e4558
    .vendor_sub_namespace = 0,
    .vendor_skip_length = 18,
    .txdelay = 0,
    .txrepetitions_num = 1,
    .txrepetitions_period = 0,
    .ratespec = 0xc1000000, // HT-MCS-0
};

/***********
  参数解析
************/
static char            *ifname = "wlan0";
static char            *filepath = "";
static int             log_level = 2;

const char *argp_program_version = "V20.12.19";
const char *argp_program_bug_address = "<shwei@tju.edu.cn>";

static char doc[] = "injectutil -- a program for frame injection.";

static struct argp_option options[] = {
	{"interface", 'I', "STRING", 0, "Set interface name (default: wlan0)"},
	{"num", 'n', "INT", 0, "Set the number of packets will be send (default: 1)"},
    {"period", 'p', "INT", OPTION_ARG_OPTIONAL, "Set period (ms) (default: 0)"},
	{"delay", 'd', "INT", 0, "Wait before starting capture (ms)  (default: 0)"},
	{"ratespec", 'r', "INT", 0, "ratespec (default: 0xc1000000)"},
	{"hexfile", 'f', "STRING", OPTION_ARG_OPTIONAL, "read a frame from file"},
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
			nex_rt.txrepetitions_num = strtoul(arg, NULL, 10);
            break;
        case 'p':
			nex_rt.txrepetitions_period = strtoul(arg, NULL, 10);
            break;
		case 'd':
		    nex_rt.txdelay = strtoul(arg, NULL, 10);
            break;
		case 'r':
			nex_rt.ratespec = strtoul(arg, NULL, 0);
            break;
		case 'f':
			filepath = arg;
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

#define log(level, format, ...) (void)(log_level>=level && printf(format, ##__VA_ARGS__))

int32_t create_raw_socket(const char* p_iface)
{
    /* new raw socket */
    int32_t t_socket=socket(PF_PACKET,SOCK_RAW,htons(ETH_P_ALL));
    if(t_socket<0)
    {
        perror("<create_raw_socket> socket(PF_PACKET,SOCK_RAW,htons(ETH_P_ALL)) failed!");
        return -1;
    }
    /* get the index of the interface */
    struct ifreq t_ifr;
    memset(&t_ifr,0,sizeof(t_ifr));
    strncpy(t_ifr.ifr_name,p_iface,sizeof(t_ifr.ifr_name)-1);
    if(ioctl(t_socket,SIOCGIFINDEX,&t_ifr)<0)
    {
        perror("<create_raw_socket> ioctl(SIOCGIFINDEX) failed!");
        return -1;
    }
    /* bind the raw socket to the interface */
    struct sockaddr_ll t_sll;
    memset(&t_sll,0,sizeof(t_sll));
    t_sll.sll_family=AF_PACKET;
    t_sll.sll_ifindex=t_ifr.ifr_ifindex;
    t_sll.sll_protocol=htons(ETH_P_ALL);
    if(bind(t_socket,(struct sockaddr*)&t_sll,sizeof(t_sll))<0)
    {
        perror("<create_raw_socket> bind(ETH_P_ALL) failed!");
        return -1;
    }
    /* open promisc */
    struct packet_mreq t_mr;
    memset(&t_mr,0,sizeof(t_mr));
    t_mr.mr_ifindex=t_sll.sll_ifindex;
    t_mr.mr_type=PACKET_MR_PROMISC;
    if(setsockopt(t_socket,SOL_PACKET,PACKET_ADD_MEMBERSHIP,&t_mr,sizeof(t_mr))<0)
    {
        perror("<create_raw_socket> setsockopt(PACKET_MR_PROMISC) failed!");
        return -1;
    }
    return t_socket;
}

int32_t main(int argc, char *argv[])
{
    int32_t t_socket;
    uint8_t t_buffer[MAX_FRAME_SIZE];
    uint8_t *p;

    argp_parse(&argp, argc, argv, 0, 0, 0);

    t_socket = create_raw_socket(ifname);
    p = t_buffer;
    // radiotap
    memcpy(p, &nex_rt, sizeof(nex_rt));
    p += sizeof(nex_rt);
    // frame
    int frame_size = readFile(p, MAX_FRAME_SIZE-(p-t_buffer), filepath, 0);
    if (frame_size<0)
        exit(-1);
    p += frame_size;

    int32_t t_size = write(t_socket, t_buffer, p-t_buffer);
    if(t_size<0) {
        perror("<main> write() failed!");
    }
    log(1, "delay=%d number=%d period=%d ratespec=0x%08x size=%d\n", nex_rt.txdelay, \
            nex_rt.txrepetitions_num, nex_rt.txrepetitions_period, nex_rt.ratespec, t_size);
    if(log_level>=3)
        hexDump (" The hexdump of packet", t_buffer, p-t_buffer);
    fflush(stdout);

    return 0;
}
