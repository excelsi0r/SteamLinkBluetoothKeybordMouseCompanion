#include "input_headers.h"

int init_input(Input_config * in_config)
{
    return in_config->file = open("/dev/uinput", O_WRONLY | O_NONBLOCK);
}

int close_input(Input_config * in_config)
{
    return close(in_config->file);
}

void emit(int fd, int type, int code, int val)
{
   struct input_event ie;

   ie.type = type;
   ie.code = code;
   ie.value = val;
   ie.time.tv_sec = 0;
   ie.time.tv_usec = 0;

   write(fd, &ie, sizeof(ie));
}