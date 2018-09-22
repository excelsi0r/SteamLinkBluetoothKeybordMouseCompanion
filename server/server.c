#include "bluetooth/bluetooth_headers.h"
#include "input/input_headers.h"
#include "signal/signal_headers.h"

int main(int argc, char const *argv[])
{
    //errors
    int bt_error, in_error;
    __sighandler_t sg_error;

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


    //close bluetooth
    close_bluetooth(bt_config);
    close_input(in_config);

    return 0;
}
