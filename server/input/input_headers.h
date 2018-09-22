#ifndef INPUT_HEADERS_H
#define INPUT_HEADERS_H

#include "../common/common.h"

typedef struct
{
    int file;
} Input_config;

int init_input(Input_config * in_config);
int close_input(Input_config * in_config);

#endif