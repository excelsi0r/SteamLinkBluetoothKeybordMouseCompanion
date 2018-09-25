#ifndef INPUT_HEADERS_H
#define INPUT_HEADERS_H

#include "../common/common.h"
#include "../protocol/protocol_headers.h"

#define INPUT1_KEY_LOWER_BOUND KEY_ESC
#define INPUT1_KEY_UPPER_BOUND KEY_MICMUTE

#define INPUT2_KEY_LOWER_BOUND BTN_MISC
#define INPUT2_KEY_UPPER_BOUND KEY_MAX

#define INPUT3_REL_UPPER_BOUND REL_X
#define INPUT3_REL_LOWER_BOUND REL_MAX

typedef struct
{
    int file;
} Input_config;

int init_input(Input_config * in_config);
int close_input(Input_config * in_config);
void emit(int fd, int type, int code, int val);

void config_group_key_1(int fd);
void config_group_btn_2(int fd);
void config_group_rel_3(int fd);
void config_virtual_driver(int fd);

void receive_event(char * buf, int client, Event * event, Input_config * in_config);

#endif