//
// Created by shwei on 2020/12/13.
//

#ifndef LOCTAG_NEX_CSI4339_H
#define LOCTAG_NEX_CSI4339_H

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>

#ifndef	PAD
#define	_PADLINE(line)	pad ## line
#define	_XSTR(line)	_PADLINE(line)
#define	PAD		_XSTR(__LINE__)
#endif

#define MAC2STR(a) (a)[0], (a)[1], (a)[2], (a)[3], (a)[4], (a)[5]
#define MACSTR "%02x:%02x:%02x:%02x:%02x:%02x"

struct mac_header { // 14 bytes
    uint8_t dst_mac[6];
    uint8_t src_mac[6];
    uint16_t type; //be
} __attribute__((packed)) ;

struct ip_header { // 20 bytes
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

struct udp_header { // 8 bytes
    uint16_t src_port;
    uint16_t dst_port;
    uint16_t len;
    uint16_t chk_sum;
} __attribute__((packed)) ;

struct net_headers {
    struct mac_header mac_header;
    struct ip_header ip_header ;
    struct udp_header udp_header;
} __attribute__((packed)) ;

struct csi_elem_4339 {
    int16_t real;
    int16_t imag;
} __attribute__((packed)) ;

struct csi_header_4339 { // 18 bytes
    uint32_t magic;
    uint8_t src_mac[6];
    uint16_t seq_num;
    uint16_t  : 8;
    uint16_t core_num : 3;    // Only here is big endian
    uint16_t ss_num : 3;
    uint16_t  : 2;
    uint16_t chanspec;
    uint16_t chip_ver;
    struct csi_elem_4339 data[];
} __attribute__((packed)) ;

#ifdef __cplusplus
}
#endif

#endif //LOCTAG_NEX_CSI4339_H
