#include "bluetooth_headers.h"

int accept_client(Bluetooth_config * bt_config)
{
    int client;

    client = accept(bt_config->socket, (struct sockaddr *) &bt_config->rem_addr, &bt_config->opt);
    fcntl(client, F_SETFL, O_NONBLOCK);

    return client;
}

