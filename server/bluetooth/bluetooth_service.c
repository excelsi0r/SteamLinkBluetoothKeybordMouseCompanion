#include "bluetooth_headers.h"

int accept_client(Bluetooth_config * bt_config)
{
    fd_set rfds;
    struct timeval tv;
    int retval;
    int client;

    FD_ZERO(&rfds);
    FD_SET(bt_config->socket, &rfds);

    /* Wait up to 1 second. */
    tv.tv_sec = MAX_TIMEOUT;
    tv.tv_usec = 0;

    retval = select(bt_config->socket + 1, &rfds, NULL, NULL, &tv);

    if (retval == -1)
        printf("Failed to configure select() Bluetooth\n");
    else if (retval)
    {
        client = accept(bt_config->socket, (struct sockaddr *) &bt_config->rem_addr, &bt_config->opt);
        fcntl(client, F_SETFL, O_NONBLOCK);
        return client;
    }

    return -1;
}

