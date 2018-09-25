#include "input_headers.h"

void receive_event(char * buf, int client, Event * event, Input_config * in_config)
{
    //Constant read from child
    int bytes_read = read(client, buf, sizeof(buf));

    if(bytes_read > 0)
    {
        parse(buf ,event);
        
        if(event->valid && event->mouse_ev)
        {
            emit(in_config->file, EV_REL, REL_X, event->mouse_x);
            emit(in_config->file, EV_REL, REL_Y, event->mouse_y);
            emit(in_config->file, EV_SYN, SYN_REPORT, 0);
        }
        else if(event->valid && event->key_ev)
        {
            emit(in_config->file, EV_KEY, event->key, event->key_action);
            emit(in_config->file, EV_SYN, SYN_REPORT, 0);    
        }
        else if(event->valid && event->mousewheel_ev)
        {
            emit(in_config->file, EV_REL, REL_WHEEL, event->mouse_v);
            emit(in_config->file, EV_REL, REL_HWHEEL, event->mouse_h);
            emit(in_config->file, EV_SYN, SYN_REPORT, 0);
        }

        printf("Received [%s]\n", buf);
    }
}