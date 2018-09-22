#include "input_headers.h"

int init_input(Input_config * in_config)
{
    return in_config->file = open("/dev/uinput", O_WRONLY | O_NONBLOCK);
}

int close_input(Input_config * in_config)
{
    return close(in_config->file);
}


