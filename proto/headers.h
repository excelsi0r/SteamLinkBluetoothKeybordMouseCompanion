#ifndef HEADERS_H_
#define HEADERS_H_

typedef struct
{
    int valid;
    int mouse_ev;
    int mouse_x;
    int mouse_y;
    int key_ev;
    int key;
    int key_action;
} Event;

#endif