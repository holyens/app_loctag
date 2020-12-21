//
// Created by shwei on 2020/12/7.
//

#include "nex_dot11rt.h"

const char *frame_type[4] = {
    "mgt", "ctl", "data", "ext"
};
const char *frame_subtype[4][16] = {
    {
        "Association Request",
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

