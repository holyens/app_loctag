//
// Created by shwei on 2020/12/7.
//

#ifndef LOCTAG_NEX_DOT11RT_H
#define LOCTAG_NEX_DOT11RT_H
#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>
#include "radiotap_iter.h"

#define MAC2STR(a) (a)[0], (a)[1], (a)[2], (a)[3], (a)[4], (a)[5]
#define MACSTR "%02x:%02x:%02x:%02x:%02x:%02x"

struct nexmon_radiotap_header {
    struct ieee80211_radiotap_header header;
    uint32_t tsf_l;
    uint32_t tsf_h;
    uint8_t flags;
    uint8_t data_rate;
    uint16_t chan_freq;
    uint16_t chan_flags;
    int8_t dbm_antsignal;
    int8_t dbm_antnoise;
    int8_t mcs[3];
    int8_t PAD;
    uint8_t vendor_oui[3];
    uint8_t vendor_sub_namespace;
    uint16_t vendor_skip_length;
    uint8_t plcp_data[6];

} __attribute__((packed));

struct ieee80211_hdr {
    uint8_t ve: 2;
    uint8_t type: 2;
    uint8_t subtype: 4;
    uint8_t flags;
	uint16_t duration_id;
	uint8_t addr1[6];
	uint8_t addr2[6];
	uint8_t addr3[6];
	uint16_t seq_ctrl;
	uint16_t addr4[6];
} __attribute__ ((packed));

struct tagged_parameter {
     uint8_t num;
     uint8_t len;
     char buf[];
 };

extern const char *frame_type[4];
extern const char *frame_subtype[4][16];

#ifdef __cplusplus
}
#endif
#endif //LOCTAG_NEX_DOT11RT_H
