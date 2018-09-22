#include "../common/common.h"

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

void parse(char buf[], int byte_read, Event * event);
void emit(int fd, int type, int code, int val);