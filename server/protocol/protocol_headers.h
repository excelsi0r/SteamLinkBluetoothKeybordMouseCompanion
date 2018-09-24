#ifndef PROTOCOL_HEADERS_H
#define PROTOCOL_HEADERS_H

#include "../common/common.h"

#define KEYB 1
#define MOUS 2
#define MHWL 3

typedef struct
{
    int valid;
    int mouse_ev;
    double mouse_x;
    double mouse_y;
	int mousewheel_ev;
	double mouse_h;
	double mouse_v;
    int key_ev;
    int key;
    int key_action;
} Event;

void parse(char buf[], Event * event);

#endif