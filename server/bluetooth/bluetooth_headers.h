#ifndef BLUETOOTH_HEADERS_H
#define BLUETOOTH_HEADERS_H

#include "../common/common.h"

#define PORT 11

typedef struct
{
    int socket;
    struct sockaddr_rc rem_addr;
    struct sockaddr_l2 rem_addr_l2;
    socklen_t opt;
    sdp_session_t * session;
} Bluetooth_config;

int init_bluetooth(Bluetooth_config * bt_config);
int init_bluetooth_l2cap(Bluetooth_config * bt_config);
int close_bluetooth(Bluetooth_config * bt_config);
int close_session(Bluetooth_config * bt_config);
sdp_session_t *register_service(uint8_t rfcomm_channel);

int accept_client(Bluetooth_config * bt_config);
int accept_client_l2(Bluetooth_config * bt_config);

#endif