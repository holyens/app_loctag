//
// Created by shwei on 2020/12/19.
//

#ifndef LOCTAG_NEX_INJECT_H
#define LOCTAG_NEX_INJECT_H
#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>

#ifndef	PAD
#define	_PADLINE(line)	pad ## line
#define	_XSTR(line)	_PADLINE(line)
#define	PAD		_XSTR(__LINE__)
#endif

struct ip_header {
    uint8_t ver : 4;
    uint8_t hdr_len : 4;
    uint8_t service;
    uint16_t len; //be
    uint16_t identify;
    uint16_t PAD;
    uint8_t PAD;
    uint8_t protocol;  // udp:17, tcp:6
    uint16_t chk_sum;
    uint8_t src_ip[4];
    uint8_t dst_ip[4];
} __attribute__((packed)) ;

struct udp_header {
    uint16_t src_port;
    uint16_t dst_port;
    uint16_t len;
    uint16_t chk_sum;
} __attribute__((packed)) ;

struct beacon_frame {
    uint8_t ver_type_subtype;
    uint8_t flags;
    uint16_t duration;
    uint8_t dst_addr[6];
    uint8_t src_addr[6];
    uint8_t bssid[6];
    uint16_t frag_seq_num;
    uint8_t timestamp[8];
    uint16_t interval;
    uint16_t capa;
    uint8_t ssid_tag_num;
    uint8_t ssid_len;
    char ssid[6];
} __attribute__((packed));

struct data_frame {
    uint8_t ver_type_subtype;
    uint8_t flags;
    uint16_t duration;
    uint8_t dst_addr[6];
    uint8_t src_addr[6];
    uint8_t bssid[6];
    uint16_t frag_seq_num;
    uint16_t qos_ctrl;
    uint8_t llc_dsap;
    uint8_t llc_ssap;
    uint8_t llc_ctrl;
    uint8_t llc_org_code[3];
    uint16_t llc_type;
    uint8_t payload[34];
} __attribute__((packed));


struct nex_inject_radiotap {
    uint8_t it_version;
    uint8_t it_pad;
    uint16_t it_len;
    uint32_t it_present;
    uint32_t it_present_nex;
    uint8_t vendor_oui[3];
    uint8_t vendor_sub_namespace;
    uint16_t vendor_skip_length;
    uint16_t PAD;
    uint32_t txdelay;
    uint32_t txrepetitions_num;
    uint32_t txrepetitions_period;
    uint32_t ratespec;
} __attribute__((packed));

#define BIT(x) (1<<x)

#ifdef __cplusplus
}
#endif
#endif //LOCTAG_NEX_INJECT_H
