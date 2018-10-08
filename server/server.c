#include "bluetooth/bluetooth_headers.h"
#include "input/input_headers.h"
#include "signal/signal_headers.h"
#include "protocol/protocol_headers.h"

int main(int argc, char const *argv[])
{
    //variables
    int bt_error, bt_error_l2, in_error;
    __sighandler_t sg_error;
    int client, pid, pidSocket;

    //get PID
    int temp_pid = getpid();
    printf("My PID is: %d\n", temp_pid);

    //config bluetooth rfcomm
    Bluetooth_config * bt_config = malloc(sizeof(Bluetooth_config));
    bt_error = init_bluetooth(bt_config);
    if(bt_error)
    {
        printf("Failed to configure Bluetooth RFCOMM module: %d\n", bt_error);
        return -1;
    }

    //config bluetooth l2cap
    Bluetooth_config * bt_config_l2 = malloc(sizeof(Bluetooth_config));
    bt_error_l2 = init_bluetooth_l2cap(bt_config_l2);
    if(bt_error_l2)
    {
        printf("Failed to configure Bluetooth L2CAP module: %d\n", bt_error_l2);
        return -1;
    }

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
    
    pidSocket = fork();

    if(pidSocket > 0) //rfcomm
    {
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
        
            printf("Closing Child RFCOM\n"); fflush(stdout);

            return 0;
        }

        close_bluetooth(bt_config);
        close_session(bt_config);
        free(bt_config);

        printf("Closing Parent RFCOMM\n"); fflush(stdout);

        //OMIT RETURN HERE SO THAT HE CLOSES MUTUAL CONFIG
    }
    else if(pidSocket == 0) //l2cap
    {  
        while(!stop)
        {
            client = accept_client_l2(bt_config_l2);

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
            char buf[1024] = {0};
            Event * event = malloc(sizeof(Event));

            ba2str(&bt_config->rem_addr_l2.l2_bdaddr, buf);
            memset(buf, 0, sizeof(buf));

            // read data from the client
            while(!stop)
            {
                if(receive_event(buf, client, event, in_config) == -1)
                    break;
            }

            free(event);
            close(client);
        
            printf("Closing Child L2CAP\n"); fflush(stdout);

            return 0;
        }

        close_bluetooth(bt_config_l2);
        free(bt_config_l2);
        printf("Closing Parent L2CAP\n"); fflush(stdout);

        return 0;
    }

    free(in_config);
    close_input(in_config);

    return 0;
}
