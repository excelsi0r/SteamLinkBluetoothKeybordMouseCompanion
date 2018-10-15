#include "bluetooth/bluetooth_headers.h"
#include "input/input_headers.h"
#include "signal/signal_headers.h"
#include "protocol/protocol_headers.h"

int main(int argc, char const *argv[])
{
    //variables
    int bt_error, in_error, file_error;
    __sighandler_t sg_error;
    int client, pid;

    //get PID
    int temp_pid = getpid();
    syslog(LOG_NOTICE, "My PID is: %d\n", temp_pid);

    file_error = test_input();
    if(file_error == 1)
    {
        syslog(LOG_NOTICE, "File /var/run/sdp not available");
        return -1;
    }

    //config bluetooth
    Bluetooth_config * bt_config = malloc(sizeof(Bluetooth_config));
    bt_error = init_bluetooth(bt_config);
    if(bt_error)
    {
         syslog(LOG_NOTICE, "Failed to configure Bluetooth module: %d\n", bt_error);
        return -1;
    }

    Input_config * in_config = malloc(sizeof(Input_config));
    in_error = init_input(in_config);
    if(in_error == -1)
    {
        syslog(LOG_NOTICE, "Failed to configure Input module: %d\n", in_error);
        return -1;
    }

    //config signal handling
    sg_error = init_signal();
    if(sg_error == SIG_ERR)
    {
        syslog(LOG_NOTICE,"Failed to configure Signal module");
        return -1;
    }
    
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

        //printf("Attempt Bluetooth accept\n");
    }

    //Custom behavior children
    if(pid == 0)
    {
        //child
        //allocationg variables
        char buf[1024] = {0};
        Event * event = malloc(sizeof(Event));

        ba2str(&bt_config->rem_addr.rc_bdaddr, buf);
        memset(buf, 0, sizeof(buf));

        // read data from the client
        while(!stop)
        {
            if(receive_event(buf, client, event, in_config) == -1)
                break;
        }

        free(event);
        close(client);
    
        syslog(LOG_NOTICE,"Closing Client\n");

        return 0;
    }

    close_bluetooth(bt_config);
    close_input(in_config);
    free(bt_config);
    free(in_config);

    printf("Closing Server\n");
    
    return 0;
}
