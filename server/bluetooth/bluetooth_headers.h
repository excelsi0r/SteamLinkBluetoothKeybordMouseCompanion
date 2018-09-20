#ifndef BLUETOOTH_HEADERS_H
#define BLUETOOTH_HEADERS_H

#include <bluetooth/bluetooth.h>
#include <bluetooth/rfcomm.h>
#include <bluetooth/sdp.h>
#include <bluetooth/sdp_lib.h>

#define PORT 11

typedef struct
{
    int socket;
    struct sockaddr_rc rem_addr;
    socklen_t opt;
} Bluetooth_config;

int init_bluetooth();
sdp_session_t *register_service(uint8_t rfcomm_channel);

#endif