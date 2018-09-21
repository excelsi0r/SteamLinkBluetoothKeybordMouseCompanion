#ifndef BLUETOOTH_HEADERS_H
#define BLUETOOTH_HEADERS_H

#include "../common/common.h"

#define PORT 11

typedef struct
{
    int socket;
    struct sockaddr_rc rem_addr;
    socklen_t opt;
    sdp_session_t * session;
} Bluetooth_config;

int init_bluetooth(Bluetooth_config * bt_config);
int close_bluetooth(Bluetooth_config * bt_config);
sdp_session_t *register_service(uint8_t rfcomm_channel);

#endif