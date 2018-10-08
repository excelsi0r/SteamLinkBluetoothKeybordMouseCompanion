#include "input_headers.h"

int receive_event(char * buf, int client, Event * event, Input_config * in_config)
{
    fd_set rfds;
    struct timeval tv;
    int retval;

    FD_ZERO(&rfds);
    FD_SET(client, &rfds);

    tv.tv_sec = MAX_TIMEOUT;
    tv.tv_usec = 0;
    
    retval = select(client + 1, &rfds, NULL, NULL, &tv);

    if (retval == -1)
        printf("Failed to configure select() Input\n");
    else if (retval)
    {
        //Constant read from child
        int bytes_read = read(client, buf, 10);

        if(bytes_read > 0)
        {
            parse(buf ,event);

            //printf("Received [%s], #bytes_read: [%d], valid [%d], keyb [%d], mouse [%d], mwhel [%d],\n", buf, bytes_read, event->valid, event->key_ev, event->mouse_ev, event->mousewheel_ev);
            
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
        }
        else if(bytes_read <= 0)
        {
            return -1;
        }
    }

    return 0;
}