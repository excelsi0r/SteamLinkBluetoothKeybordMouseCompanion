#include "bluetooth/bluetooth_headers.h"
#include "input/input_headers.h"
#include "signal/signal_headers.h"
#include "protocol/protocol_headers.h"

int main(int argc, char const *argv[])
{
    //variables
    int bt_error, in_error;
    __sighandler_t sg_error;
    int client, pid;

    //get PID
    int temp_pid = getpid();
    printf("My PID is: %d\n", temp_pid);

    //config bluetooth
    Bluetooth_config * bt_config = malloc(sizeof(Bluetooth_config));
    bt_error = init_bluetooth(bt_config);
    if(bt_error)
    {
        printf("Failed to configure Bluetooth module: %d\n", bt_error);
        return -1;
    }

    //TODO config input
    Input_config * in_config = malloc(sizeof(Input_config));
    in_error = init_input(in_config);
    if(in_error == -1)
    {
        printf("Failed to configure Input module: %d\n", in_error);
        return -1;
    }

    //config signal handling
    sg_error = init_signal();
    if(sg_error == SIG_ERR)
    {
        printf("Failed to configure Signal module");
        return -1;
    }
    
    //TODO CYCLES HERE
    while(!stop)
    {
        client = accept_client(bt_config);

        if(client > -1)
        {
            pid = fork();
            if(pid == 0)
            {
                //child
                break;
            }
        }
    }

    //Custom behavior children
    if(pid == 0)
    {
        //child
        //allocationg variables
        Event * event = malloc(sizeof(Event));
        char buf[1024] = {0};
        int bytes_read;

        ba2str(&bt_config->rem_addr.rc_bdaddr, buf );
        memset(buf, 0, sizeof(buf));

        // read data from the client
        while(!stop)
        {
            //Constant read from child
            bytes_read = read(client, buf, sizeof(buf));

            if(bytes_read > 0)
            {
                parse(buf ,event);
                
                if(event->valid && event->mouse_ev)
                {
                    //write event
                    emit(in_config->file, EV_REL, REL_X, event->mouse_x);
                    emit(in_config->file, EV_REL, REL_Y, event->mouse_y);
                    emit(in_config->file, EV_SYN, SYN_REPORT, 0);
                }
                else if(event->valid && event->key_ev)
                {
                    emit(in_config->file, EV_KEY, event->key, event->key_action);
                    emit(in_config->file, EV_SYN, SYN_REPORT, 0);    
                }
                
                printf("Received [%s]\n", buf);
            }
        }

        close(client);
        free(event);

        printf("Closing Child\n");

        return 0;
    }

    close_bluetooth(bt_config);
    close_input(in_config);
    free(bt_config);
    free(in_config);

    printf("Closing Parent\n");
    
    return 0;
}
